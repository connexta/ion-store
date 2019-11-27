/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.poller;

import com.connexta.store.config.TransformConfiguration;
import com.connexta.store.controllers.StoreController;
import com.connexta.store.rest.models.AddMetadataRequest;
import com.connexta.store.rest.models.MetadataInfo;
import com.connexta.store.rest.models.QuarantineRequest;
import com.connexta.transformation.rest.models.MetadataInformation;
import com.connexta.transformation.rest.models.Status;
import com.connexta.transformation.rest.models.TransformationPollResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/** Class capable of polling the transformation poll endpoint. */
@Slf4j
public class TransformStatusPoller {

  private final WebClient storeWebClient;

  private final WebClient transformWebClient;

  private final TransformStatusTask transformStatusTask;

  /** Creates a new TransformStatusPoller. */
  public TransformStatusPoller(
      TransformStatusTask transformStatusTask,
      WebClient transformWebClient,
      WebClient storeWebClient) {
    this.transformStatusTask = transformStatusTask;
    this.transformWebClient = transformWebClient;
    this.storeWebClient = storeWebClient;
  }

  public boolean run() {
    return handleTask(transformStatusTask);
  }

  /** @return {@code true} if the task should be retried, otherwise {@code false} */
  private boolean handleTask(TransformStatusTask transformStatusTask) {
    final String transformPollUrl = transformStatusTask.getTransformStatusUrl().toString();
    final String datasetId = transformStatusTask.getDatasetId();

    // this could be an ErrorMessage too, need to account for that
    ClientResponse response = transformWebClient.get().uri(transformPollUrl).exchange().block();

    if (response != null) {
      final HttpStatus httpStatus = response.statusCode();

      if (HttpStatus.OK.equals(httpStatus)) {
        ResponseEntity<TransformationPollResponse> transformationPollResponse =
            response.toEntity(TransformationPollResponse.class).block();
        if (transformationPollResponse != null) {
          if (transformationPollResponse.getBody() == null) {
            log.debug(
                "No body available in the successful transform status response for status {}. This URL will be retried.",
                transformPollUrl);
            return true;
          }

          return handlePollResponse(datasetId, transformationPollResponse.getBody());
        } else {
          log.debug(
              "No response available in the successful transform status response for status {}. This URL will be retried.",
              transformPollUrl);
          return true;
        }
      } else if (HttpStatus.NOT_FOUND.equals(httpStatus)) {
        log.info(
            "Transform URL status for {} does not exist anymore, removing from task queue.",
            transformPollUrl);
        return false;
      } else {
        // for the rest of responses, we will just retry
        return true;
      }
    } else {
      log.debug(
          "Did not receive a response from transformation status {}. This URL will be retried.",
          transformPollUrl);
      return true;
    }
  }

  private boolean handlePollResponse(
      String datasetId, TransformationPollResponse transformationPollResponse) {
    final Status transformationStatus = transformationPollResponse.getTransformationStatus();
    switch (transformationStatus) {
      case DONE:
        return addMetadata(datasetId, transformationPollResponse.getMetadataInformations());
      case FAILED:
        logFailedTransformation(datasetId, transformationPollResponse.getMetadataInformations());
        return quarantine(datasetId, transformationPollResponse.getMetadataInformations());
      case IN_PROGRESS:
        log.debug("Transformation in progress for Dataset {}", datasetId);
        return true;
      default:
        throw new IllegalArgumentException(
            String.format("Invalid transform status received: %s", transformationStatus));
    }
  }

  private void logFailedTransformation(
      String datasetId, List<MetadataInformation> metadataInformations) {
    log.debug("Metadata transformation failed for dataset {}", datasetId);
    for (MetadataInformation metadataInformation : metadataInformations) {
      log.debug("Metadata: {}", metadataInformation);
    }
  }

  /** @return {@code true} if the add metadata should be retried, {@code false} otherwise */
  private boolean addMetadata(String datasetId, List<MetadataInformation> metadataInformations) {
    AddMetadataRequest addMetadataRequest = new AddMetadataRequest();
    for (MetadataInformation metadataInformation : metadataInformations) {
      addMetadataRequest.addMetadataInfo(
          new MetadataInfo()
              .metadataType(metadataInformation.getMetadataType())
              .location(metadataInformation.getLocation()));
    }

    ClientResponse response =
        storeWebClient
            .put()
            .uri(
                UriComponentsBuilder.fromPath(StoreController.ADD_METADATA_URL_TEMPLATE)
                    .build(datasetId)
                    .toASCIIString())
            .body(BodyInserters.fromValue(addMetadataRequest))
            .exchange()
            .block();

    if (response != null) {
      final HttpStatus httpStatus = response.statusCode();
      if (HttpStatus.OK.equals(httpStatus)) {
        deleteTransformationStatus(datasetId);
        return false;
      } else if (HttpStatus.NOT_FOUND.equals(httpStatus)) {
        log.info("Failed to add metadata to dataset {} because it no longer exists.", datasetId);
        deleteTransformationStatus(datasetId);
        return false;
      } else if (HttpStatus.BAD_REQUEST.equals(httpStatus)) {
        log.warn(
            "Bad addMetadata request sent to store for dataset {}. Request will not be retried. \n{}",
            datasetId,
            addMetadataRequest);
        return false;
      } else {
        return true;
      }
    } else {
      log.debug("No response received from add metadata request for dataset {}.", datasetId);
      return true;
    }
  }

  /** @return {@code true} if the quarantine should be retried, {@code false} otherwise */
  private boolean quarantine(String datasetId, List<MetadataInformation> metadataInformations) {
    QuarantineRequest quarantineRequest = new QuarantineRequest();
    for (MetadataInformation metadataInformation : metadataInformations) {
      quarantineRequest.addMetadataInfo(
          new MetadataInfo()
              .metadataType(metadataInformation.getMetadataType())
              .location(metadataInformation.getLocation()));
    }

    ClientResponse response =
        storeWebClient
            .put()
            .uri(
                UriComponentsBuilder.fromPath(StoreController.QUARANTINE_URL_TEMPALTE)
                    .build(datasetId))
            .body(BodyInserters.fromValue(quarantineRequest))
            .exchange()
            .block();

    if (response != null) {
      final HttpStatus httpStatus = response.statusCode();
      if (HttpStatus.OK.equals(httpStatus)) {
        deleteTransformationStatus(datasetId);
        return false;
      } else if (HttpStatus.NOT_FOUND.equals(httpStatus)) {
        log.debug("Failed to quarantine dataset {} because it no longer exists.", datasetId);
        deleteTransformationStatus(datasetId);
        return false;
      } else if (HttpStatus.BAD_REQUEST.equals(httpStatus)) {
        log.warn(
            "Bad quarantine request sent to store for dataset {}. Request will not be retried. \n{}",
            datasetId,
            quarantineRequest);
        return false;
      } else {
        return true;
      }
    } else {
      log.debug("No response received from quarantine request for dataset {}.", datasetId);
      return true;
    }
  }

  /**
   * This method is best effort. Whether or not this method fails to remove the transformation
   * status from transform, we will not let it affect whether or not the TransformStatusTask is
   * tried again.
   */
  private void deleteTransformationStatus(String datasetId) {
    ClientResponse response =
        transformWebClient
            .delete()
            .uri(
                UriComponentsBuilder.fromPath(
                        TransformConfiguration.TRANSFORM_STATUS_DELETE_TEMPLATE)
                    .build(datasetId)
                    .toASCIIString())
            .exchange()
            .block();

    if (response != null) {
      final HttpStatus httpStatus = response.statusCode();
      log.debug(
          "Received status code {} from transform status delete request. If this failed, retries are not currently supported.",
          httpStatus);
    } else {
      log.debug(
          "No response received when trying to delete transformation status for dataset {}. Retrying the transform status delete is currently not supported.",
          datasetId);
    }
  }
}
