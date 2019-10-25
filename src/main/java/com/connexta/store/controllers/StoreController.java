/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static org.springframework.http.ResponseEntity.ok;

import com.connexta.store.adaptors.RetrieveResponse;
import com.connexta.store.exceptions.CreateProductException;
import com.connexta.store.exceptions.IndexMetadataException;
import com.connexta.store.exceptions.UnsupportedMetadataException;
import com.connexta.store.rest.models.ErrorMessage;
import com.connexta.store.rest.spring.StoreApi;
import com.connexta.store.service.api.StoreService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/mis")
public class StoreController implements StoreApi {

  public static final String ACCEPT_VERSION_HEADER_NAME = "Accept-Version";

  @NotNull private final StoreService storeService;

  @NotBlank private final String storeApiVersion;

  /**
   * TODO Use {@link org.springframework.web.server.ResponseStatusException} instead of catching
   * {@link Exception}s
   */
  @Override
  public ResponseEntity<Void> storeProduct(
      final String acceptVersion, @Valid final MultipartFile file) {
    final String expectedAcceptVersion = storeApiVersion;
    if (!StringUtils.equals(acceptVersion, expectedAcceptVersion)) {
      throw new UnsupportedOperationException(
          String.format(
              "Expected Accept-Version to be \"%s\" but was \"%s\". Only \"%s\" is currently supported.",
              expectedAcceptVersion, acceptVersion, expectedAcceptVersion));
    }

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
      throw new CreateProductException(
          String.format(
              "Unable to read file for storeProduct request with a file with mediaType=%s and fileName=%s",
              mediaType, fileName),
          e);
    }

    final URI location;
    try {
      location = storeService.createProduct(fileSize, mediaType, fileName, inputStream);
    } catch (URISyntaxException e) {
      throw new CreateProductException(
          String.format(
              "Unable to complete storeProduct request for a file with mediaType=%s and fileName=%s",
              mediaType, fileName),
          e);
    }

    return ResponseEntity.created(location).build();
  }

  @Override
  public ResponseEntity<Void> addMetadata(
      final String acceptVersion,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String productId,
      @Pattern(regexp = "^[0-9a-zA-Z\\-]+$") @Size(min = 1, max = 32) final String metadataType,
      @Valid final MultipartFile file) {
    final String expectedAcceptVersion = storeApiVersion;
    if (!StringUtils.equals(acceptVersion, expectedAcceptVersion)) {
      throw new UnsupportedOperationException(
          String.format(
              "Expected Accept-Version to be \"%s\" but was \"%s\". Only \"%s\" is currently supported.",
              expectedAcceptVersion, acceptVersion, expectedAcceptVersion));
    }

    // TODO Validate other params.

    if (!StringUtils.equals(metadataType, "cst")) {
      throw new UnsupportedMetadataException(
          HttpStatus.NOT_IMPLEMENTED,
          String.format("Metadata type %s is not yet supported", metadataType));
    }

    final Long fileSize = file.getSize();
    // TODO validate that fileSize is (0 GB, 10 GB]

    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      throw new IndexMetadataException(
          String.format("Unable to read file for PUT CST request for id=%s", productId), e);
    }
    storeService.indexProduct(inputStream, fileSize, productId);
    return ok().build();
  }

  @ApiOperation(
      value = "Get a Product.",
      nickname = "retrieveProduct",
      notes = "Clients send a Product ID to retrieve the Product as a file.",
      response = Resource.class,
      tags = {"store"})
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Get Product", response = Resource.class),
        @ApiResponse(
            code = 401,
            message = "The client could not be authenticated. ",
            response = ErrorMessage.class),
        @ApiResponse(
            code = 400,
            message =
                "The client message could not be understood by the server due to invalid format or syntax. ",
            response = ErrorMessage.class),
        @ApiResponse(
            code = 403,
            message = "The client does not have permission. ",
            response = ErrorMessage.class),
        @ApiResponse(
            code = 501,
            message = "The requested API version is not supported and therefore not implemented. ",
            response = ErrorMessage.class)
      })
  @RequestMapping(
      value = "/product/{productId}",
      produces = {"application/octet-stream", "application/json"},
      method = RequestMethod.GET)
  public ResponseEntity<Resource> retrieveProduct(
      @Pattern(regexp = "^[0-9a-zA-Z]+$")
          @Size(min = 32, max = 32)
          @ApiParam(value = "The ID of the Product. ", required = true)
          @PathVariable("productId")
          final String productId) {
    InputStream inputStream = null;
    try {
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
}
