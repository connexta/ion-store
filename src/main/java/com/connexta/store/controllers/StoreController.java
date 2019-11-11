/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.http.ResponseEntity.ok;

import com.connexta.ingest.rest.spring.IngestApi;
import com.connexta.store.adaptors.FileRetrieveResponse;
import com.connexta.store.exceptions.StoreException;
import com.connexta.store.rest.models.ErrorMessage;
import com.connexta.store.rest.spring.StoreApi;
import com.connexta.store.service.api.StoreService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebInputException;

@Slf4j
@AllArgsConstructor
@RestController
public class StoreController implements StoreApi, IngestApi {

  public static final String ACCEPT_VERSION_HEADER_NAME = "Accept-Version";
  public static final String SUPPORTED_METADATA_TYPE = "irm";
  public static final String CREATE_DATASET_URL_TEMPLATE = "/dataset";
  public static final String ADD_METADATA_URL_TEMPLATE = "/dataset/{datasetId}/{metadataType}";
  public static final String RETRIEVE_FILE_URL_TEMPLATE = "/dataset/{datasetId}";
  public static final String RETRIEVE_IRM_URL_TEMPLATE = "/dataset/{datasetId}/irm";
  public static final MediaType IRM_MEDIA_TYPE = new MediaType("application", "dni-tdf+xml");
  public static final String IRM_MEDIA_TYPE_VALUE = "application/dni-tdf+xml";
  public static final MediaType METACARD_MEDIA_TYPE = MediaType.APPLICATION_XML;
  @NotNull private final StoreService storeService;
  @NotBlank private final String storeApiVersion;

  @Override
  public Optional<NativeWebRequest> getRequest() {
    return Optional.empty();
  }

  @Override
  public ResponseEntity<Void> addMetadata(
      final String acceptVersion,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId,
      @Pattern(regexp = "^[0-9a-zA-Z\\-]+$") @Size(min = 1, max = 32) final String metadataType,
      @Valid final MultipartFile file) {
    final String expectedAcceptVersion = storeApiVersion;
    if (!StringUtils.equals(acceptVersion, expectedAcceptVersion)) {
      throw new UnsupportedOperationException(
          String.format(
              "%s was \"%s\", but only \"%s\" is currently supported.",
              ACCEPT_VERSION_HEADER_NAME, acceptVersion, expectedAcceptVersion));
    }

    // TODO Validate other params.

    if (!StringUtils.equals(metadataType, SUPPORTED_METADATA_TYPE)) {
      throw new UnsupportedOperationException(
          String.format("Metadata type %s is not yet supported", metadataType));
    }

    final Long fileSize = file.getSize();
    // TODO validate that fileSize is (0 GB, 10 GB]

    // TODO Verify InputStream is closed in tests
    try (final InputStream inputStream = file.getInputStream()) {
      storeService.addIrm(inputStream, fileSize, datasetId);
    } catch (IOException e) {
      throw new ValidationException(
          String.format(
              String.format(
                  "Unable to read file for addMetadata request for metadataType=%s, id=%s",
                  metadataType, datasetId),
              e));
    }

    return ok().build();
  }

  @Override
  public ResponseEntity<Void> ingest(
      String acceptVersion,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime lastModified,
      MultipartFile file,
      String correlationId,
      MultipartFile metacard) {
    if (lastModified == null || lastModified.toString().isBlank()) {
      throw new ServerWebInputException(
          String.format("%s is missing or blank", LAST_MODIFIED)); // TODO Replace this exception
    }
    String fileName = file.getOriginalFilename();
    log.info("Ingest request received fileName={}", fileName);
    InputStream inputStream;
    InputStream metacardInputStream;
    try {
      inputStream = file.getInputStream();
      metacardInputStream = metacard.getInputStream();
    } catch (IOException e) {
      throw new ValidationException("Could not open attachment"); // TODO Replace this exception
    }
    storeService.ingest(
        file.getSize(),
        file.getContentType(),
        inputStream,
        fileName,
        metacard.getSize(),
        metacardInputStream);

    return ResponseEntity.accepted().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Resource> retrieveMetacard(
      @ApiParam(required = true) @PathVariable("id") final String id) {
    InputStream inputStream = null;
    try {
      // TODO return 404 if key doesn't exist
      inputStream = storeService.retrieveMetacard(id);
      log.info("Successfully retrieved metacard id={}", id);
      return ResponseEntity.ok()
          .contentType(METACARD_MEDIA_TYPE)
          .body(new InputStreamResource(inputStream));
    } catch (RuntimeException e) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ioe) {
          log.warn("Unable to close InputStream when retrieving metacard id={}", id, ioe);
        }
      }

      log.warn("Unable to retrieve metacard id={}", id, e);
      throw new StoreException(String.format("Unable to retrieve metacard: %s", e.getMessage()), e);
    } catch (Throwable t) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving metacard id={}", id, e);
        }
      }
      throw t;
    }
  }

  @ApiOperation(
      value = "Get a file for a dataset.",
      nickname = "retrieveFile",
      response = Resource.class,
      tags = {"store"})
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Get File", response = Resource.class),
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
      value = RETRIEVE_FILE_URL_TEMPLATE,
      produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.GET)
  public ResponseEntity<Resource> retrieveFile(
      @Pattern(regexp = "^[0-9a-zA-Z]+$")
          @Size(min = 32, max = 32)
          @ApiParam(value = "The ID of the dataset. ", required = true)
          @PathVariable("datasetId")
          final String datasetId) {
    InputStream inputStream = null;
    try {
      final FileRetrieveResponse fileRetrieveResponse = storeService.retrieveFile(datasetId);
      log.info("Successfully retrieved file for datasetId={}", datasetId);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentDisposition(
          ContentDisposition.builder("attachment")
              .filename(fileRetrieveResponse.getFileName())
              .build());
      inputStream = fileRetrieveResponse.getInputStream();
      return ResponseEntity.ok()
          .contentType(fileRetrieveResponse.getMediaType())
          .headers(httpHeaders)
          .body(new InputStreamResource(inputStream));
    } catch (Throwable t) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn(
              "Unable to close InputStream when retrieving file for datasetId={}", datasetId, e);
        }
      }
      throw t;
    }
  }

  @ApiOperation(
      value = "Get a IRM for a dataset.",
      nickname = "retrieveIrm",
      response = Resource.class,
      tags = {"store"})
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Get IRM", response = Resource.class),
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
      value = RETRIEVE_IRM_URL_TEMPLATE,
      produces = {IRM_MEDIA_TYPE_VALUE, MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.GET)
  public ResponseEntity<Resource> retrieveIrm(
      @Pattern(regexp = "^[0-9a-zA-Z]+$")
          @Size(min = 32, max = 32)
          @ApiParam(value = "The ID of the dataset. ", required = true)
          @PathVariable("datasetId")
          final String datasetId) {
    InputStream inputStream = null;
    try {
      inputStream = storeService.retrieveIrm(datasetId);
      log.info("Successfully retrieved irm for datasetId={}", datasetId);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentDisposition(
          ContentDisposition.builder("attachment").filename("irm-" + datasetId + ".xml").build());
      return ResponseEntity.ok()
          .contentType(IRM_MEDIA_TYPE)
          .headers(httpHeaders)
          .body(new InputStreamResource(inputStream));
    } catch (Throwable t) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn(
              "Unable to close InputStream when retrieving irm for datasetId={}", datasetId, e);
        }
      }
      throw t;
    }
  }
}
