/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import com.connexta.store.adaptors.RetrieveResponse;
import com.connexta.store.common.exceptions.StoreException;
import com.connexta.store.rest.spring.StoreApi;
import com.connexta.store.service.api.StoreService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestController
@RequestMapping("/mis")
public class StoreController implements StoreApi {

  @NotNull private final StoreService storeService;

  public StoreController(@NotNull final StoreService storeService) {
    this.storeService = storeService;
  }

  /**
   * TODO Use {@link org.springframework.web.server.ResponseStatusException} instead of catching
   * {@link Exception}s
   */
  @Override
  public ResponseEntity<Void> storeProduct(
      final String acceptVersion, @Valid final MultipartFile file) {
    // TODO validate Accept-Version

    final Long fileSize = file.getSize();
    // TODO validate that fileSize is (0 GB, 10 GB]

    final String mediaType = file.getContentType();
    // TODO verify that mediaType is not blank and is a valid Content Type

    final String fileName = file.getOriginalFilename();
    // TODO verify that fileName is not blank

    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for storeProduct request with for a file with mediaType={} and fileName={}",
          mediaType,
          fileName,
          e);
      return ResponseEntity.badRequest().build();
    }

    final URI location;
    try {
      location = storeService.createProduct(fileSize, mediaType, fileName, inputStream);
    } catch (StoreException | URISyntaxException e) {
      log.warn(
          "Unable to complete storeProduct request for a file with mediaType={} and fileName={}",
          mediaType,
          fileName,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.created(location).build();
  }

  @Override
  public ResponseEntity<Void> addMetadata(
      final String acceptVersion,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String productId,
      @Pattern(regexp = "^[0-9a-zA-Z\\-]+$") @Size(min = 1, max = 32) final String metadataType,
      @Valid final MultipartFile file) {
    // TODO validate params

    if (!StringUtils.equals(metadataType, "cst")) {
      log.warn("Metadata type {} is not yet supported", metadataType);
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    final Long fileSize = file.getSize();
    // TODO validate that fileSize is (0 GB, 10 GB]

    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn("Unable to read file for PUT CST request for id={}", productId, e);
      return ResponseEntity.badRequest().build();
    }

    try {
      storeService.indexProduct(inputStream, fileSize, productId);
    } catch (final StoreException e) {
      log.warn(
          "Unable to complete store metadata request with for metadataType=cst and productId={}",
          productId,
          e);
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Resource> retrieveProduct(
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String productId) {
    InputStream inputStream = null;
    try {
      // TODO return 404 if key doesn't exist
      final RetrieveResponse retrieveResponse = storeService.retrieveProduct(productId);
      log.info("Successfully retrieved id={}", productId);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentDisposition(
          ContentDisposition.builder("attachment")
              .filename(retrieveResponse.getFileName())
              .build());
      inputStream = retrieveResponse.getInputStream();
      return ResponseEntity.ok()
          .contentType(retrieveResponse.getMediaType())
          .headers(httpHeaders)
          .body(new InputStreamResource(inputStream));
    } catch (StoreException | RuntimeException e) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ioe) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", productId, ioe);
        }
      }

      log.warn("Unable to retrieve {}", productId, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Throwable t) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", productId, e);
        }
      }
      throw t;
    }
  }

  // TODO replace this with better error handling
  @ControllerAdvice
  private class ConstraintViolationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
        @NotNull final ConstraintViolationException e, @NotNull final WebRequest request) {
      final String message = e.getMessage();
      final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
      log.warn("Request is invalid: {}. Returning {}.", message, httpStatus, e);
      return handleExceptionInternal(e, message, new HttpHeaders(), httpStatus, request);
    }
  }
}
