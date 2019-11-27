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
import com.connexta.store.exceptions.common.DetailedResponseStatusException;
import com.connexta.store.rest.models.AddMetadataRequest;
import com.connexta.store.rest.models.ErrorMessage;
import com.connexta.store.rest.models.QuarantineRequest;
import com.connexta.store.rest.spring.StoreApi;
import com.connexta.store.service.api.IonData;
import com.connexta.store.service.api.StoreService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.UUID;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@AllArgsConstructor
@RestController
public class StoreController implements StoreApi, IngestApi {

  public static final String RETRIEVE_DATA_URL_TEMPLATE = "/dataset/{datasetId}/{dataType}";
  public static final String QUARANTINE_URL_TEMPALTE = "/dataset/{datasetId}/quarantine";
  public static final String ADD_METADATA_URL_TEMPLATE = "/dataset/{datasetId}";
  public static final String IRM_MEDIA_TYPE_VALUE = "application/dni-tdf+xml";

  @NotNull private StoreService storeService;
  @NotBlank private String storeApiVersion;
  private static final String ACCEPT_VERSION_HEADER_NAME = "Accept-Version";

  @Override
  public ResponseEntity<Void> quarantine(
      final String acceptVersion, final UUID datasetId, QuarantineRequest quarantineRequest) {
    storeService.quarantine(datasetId.toString());
    return ResponseEntity.accepted().build();
  }

  @Override
  public ResponseEntity<Void> addMetadata(
      final String acceptVersion, final UUID datasetId, AddMetadataRequest addMetadataRequest) {
    if (!StringUtils.equals(acceptVersion, storeApiVersion)) {
      throw new UnsupportedOperationException(
          String.format(
              "%s was \"%s\", but only \"%s\" is currently supported.",
              ACCEPT_VERSION_HEADER_NAME, acceptVersion, storeApiVersion));
    }

    try {
      storeService.addMetadata(datasetId.toString(), addMetadataRequest.getMetadataInfos());
    } catch (IOException e) {
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving the Metadata.");
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
    if (!StringUtils.equals(acceptVersion, storeApiVersion)) {
      throw new UnsupportedOperationException(
          String.format(
              "%s was \"%s\", but only \"%s\" is currently supported.",
              ACCEPT_VERSION_HEADER_NAME, acceptVersion, storeApiVersion));
    }

    MultipartFileValidator.validate(file);

    if (lastModified == null) {
      throw new DetailedResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format("%s is missing.", LAST_MODIFIED));
    }

    String fileName = file.getOriginalFilename();
    log.info("Ingest request received fileName={}", fileName);
    InputStream inputStream;
    try {
      inputStream = file.getInputStream();

    } catch (IOException e) {
      throw new ValidationException("Unable to open file attachment.", e);
    }

    InputStream metacardInputStream;
    try {
      metacardInputStream = metacard.getInputStream();
    } catch (IOException e) {
      throw new DetailedResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unable to open metacard attachment.");
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

  @ApiOperation(
      value = "Retrieve a Data (file or metadata) from a Dataset.",
      nickname = "retrieveData",
      response = Resource.class,
      tags = {"store, retrieve, irm, metacard, file"})
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Successfully retrieved the data.",
            response = Resource.class),
        @ApiResponse(
            code = 401,
            message = "The client could not be authenticated.",
            response = ErrorMessage.class),
        @ApiResponse(
            code = 400,
            message =
                "The client message could not be understood by the server due to invalid format or syntax.",
            response = ErrorMessage.class),
        @ApiResponse(
            code = 403,
            message = "The client does not have permission.",
            response = ErrorMessage.class),
        @ApiResponse(
            code = 404,
            message =
                "The Dataset identified by the id does not exist, or the data type does not exist within the Dataset.",
            response = ErrorMessage.class),
        @ApiResponse(
            code = 501,
            message = "The requested API version is not supported and therefore not implemented.",
            response = ErrorMessage.class)
      })
  @GetMapping(
      value = StoreController.RETRIEVE_DATA_URL_TEMPLATE,
      produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Resource> retrieveData(
      @Pattern(
              regexp =
                  "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}")
          @Size(min = 36, max = 36)
          @ApiParam(value = "The ID of the Dataset to retrieve the Data from.", required = true)
          @PathVariable("datasetId")
          String datasetId,
      @ApiParam(value = "The type of the Data.", required = true) @PathVariable("dataType")
          String dataType) {
    IonData ionData;
    try {
      ionData = storeService.getData(datasetId, dataType);
    } catch (IOException e) {
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving the requested Data.");
    }

    ResponseEntity.BodyBuilder bodyBuilder =
        ResponseEntity.ok().contentType(MediaType.parseMediaType(ionData.getMediaType()));

    final String fileName = ionData.getFileName();
    if (fileName != null && !fileName.isBlank()) {
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentDisposition(
          ContentDisposition.builder("attachment").filename(fileName).build());

      bodyBuilder.headers(httpHeaders);
    }

    return bodyBuilder.body(new InputStreamResource(ionData.getInputStream()));
  }
}
