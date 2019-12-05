/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import static com.connexta.store.adaptors.StoreStatus.STAGED;
import static com.connexta.store.adaptors.StoreStatus.STORED;

import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.adaptors.StorageAdaptorRetrieveResponse;
import com.connexta.store.clients.IndexClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.controllers.StoreController;
import com.connexta.store.exceptions.DatasetNotFoundException;
import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.StoreException;
import com.connexta.store.exceptions.common.DetailedResponseStatusException;
import com.connexta.store.poller.TransformStatusTask;
import com.connexta.store.rest.models.MetadataInfo;
import com.connexta.store.service.api.IonData;
import com.connexta.store.service.api.StoreService;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@AllArgsConstructor
public class StoreServiceImpl implements StoreService {

  public static final String FILE_NAME_METADATA_KEY = "Filename";
  private static final String METACARD_TYPE = "metacard";
  private static final String IRM_TYPE = "irm";
  private static final String FILE_TYPE = "file";

  @NotBlank private final URI storeUrl;
  @NotNull private final StorageAdaptor fileStorageAdaptor;
  @NotNull private final StorageAdaptor irmStorageAdaptor;
  @NotNull private final StorageAdaptor metacardStorageAdaptor;
  @NotNull private final IndexClient indexClient;
  @NotNull private final TransformClient transformClient;
  @NotNull private final BlockingQueue<TransformStatusTask> transformStatusQueue;
  private final WebClient transformWebClient;

  @Override
  public void ingest(
      long fileSize,
      final String mimeType,
      final InputStream inputStream,
      final String fileName,
      long metacardFileSize,
      final InputStream metacardInputStream) {
    final String datasetId = UUID.randomUUID().toString();

    fileStorageAdaptor.store(
        fileSize, mimeType, inputStream, datasetId, Map.of(FILE_NAME_METADATA_KEY, fileName));
    fileStorageAdaptor.updateStatus(datasetId, STAGED);

    URL stagingLocation;
    try {
      stagingLocation =
          UriComponentsBuilder.fromUri(storeUrl)
              .path(StoreController.RETRIEVE_DATA_URL_TEMPLATE)
              .build(datasetId, FILE_TYPE)
              .toURL();
    } catch (MalformedURLException e) {
      throw new StoreException("Unable to construct file retrieve URL", e);
    }

    metacardStorageAdaptor.store(
        metacardFileSize,
        MediaType.APPLICATION_XML_VALUE,
        metacardInputStream,
        datasetId,
        Map.of());
    metacardStorageAdaptor.updateStatus(datasetId, STAGED);
    final URL metacardLocation;
    try {
      metacardLocation =
          UriComponentsBuilder.fromUri(storeUrl)
              .path(StoreController.RETRIEVE_DATA_URL_TEMPLATE)
              .build(datasetId, METACARD_TYPE)
              .toURL();
    } catch (MalformedURLException e) {
      throw new StoreException("Unable to construct metacard retrieve URL", e);
    }

    URL transformStatusUrl =
        transformClient.requestTransform(
            datasetId, stagingLocation, stagingLocation, metacardLocation);
    transformStatusQueue.add(new TransformStatusTask(datasetId, transformStatusUrl));
  }

  @Override
  public IonData getData(String datasetId, String dataType) {
    switch (dataType) {
      case METACARD_TYPE:
        verifyStoreStatus(datasetId, metacardStorageAdaptor);
        InputStream metacardInputStream =
            metacardStorageAdaptor.retrieve(datasetId).getInputStream();
        return new IonData(
            MediaType.APPLICATION_XML_VALUE, metacardInputStream, "metacard-" + datasetId + ".xml");
      case IRM_TYPE:
        verifyStoreStatus(datasetId, irmStorageAdaptor);
        InputStream irmInputStream = irmStorageAdaptor.retrieve(datasetId).getInputStream();
        return new IonData("application/dni-tdf+xml", irmInputStream, "irm-" + datasetId + ".xml");
      case FILE_TYPE:
        verifyStoreStatus(datasetId, fileStorageAdaptor);
        final StorageAdaptorRetrieveResponse storageAdaptorRetrieveResponse =
            fileStorageAdaptor.retrieve(datasetId);
        final String fileName =
            storageAdaptorRetrieveResponse.getMetadata().get(FILE_NAME_METADATA_KEY);

        if (fileName == null || fileName.isEmpty()) {
          throw new RetrieveException(
              String.format(
                  "Expected S3 object to have a non-empty metadata value for %s",
                  FILE_NAME_METADATA_KEY));
        }

        return new IonData(
            storageAdaptorRetrieveResponse.getMediaType().toString(),
            storageAdaptorRetrieveResponse.getInputStream(),
            fileName);
      default:
        throw new IllegalArgumentException(
            String.format("Received unsupported dataType {%s}", dataType));
    }
  }

  @Override
  public void addMetadata(String datasetId, List<MetadataInfo> metadataInfos) throws IOException {
    for (MetadataInfo metadataInfo : metadataInfos) {
      final String dataType = metadataInfo.getMetadataType();

      ResponseEntity<Resource> metadataResource;
      try {
        metadataResource =
            transformWebClient
                .get()
                .uri(metadataInfo.getLocation().toURI())
                .retrieve()
                .toEntity(Resource.class)
                .block();
      } catch (URISyntaxException e) {
        throw new DetailedResponseStatusException(
            HttpStatus.BAD_REQUEST, "Received invalid metadata URL received.");
      }
      Resource resource = validateMetadataResource(metadataResource);
      unstage(datasetId);

      switch (dataType) {
        case METACARD_TYPE:
          metacardStorageAdaptor.store(
              resource.contentLength(),
              MediaType.APPLICATION_XML.toString(),
              resource.getInputStream(),
              datasetId,
              Map.of());
          metacardStorageAdaptor.updateStatus(datasetId, STORED);
          break;
        case IRM_TYPE:
          irmStorageAdaptor.store(
              resource.contentLength(),
              StoreController.IRM_MEDIA_TYPE_VALUE,
              resource.getInputStream(),
              datasetId,
              Map.of());
          irmStorageAdaptor.updateStatus(datasetId, STORED);
          break;
        default:
          throw new IllegalArgumentException(
              String.format("Received unsupported dataType {%s}", dataType));
      }
    }

    indexClient.indexDataset(
        UUID.fromString(datasetId),
        UriComponentsBuilder.fromUri(storeUrl)
            .path(StoreController.RETRIEVE_DATA_URL_TEMPLATE)
            .build(datasetId, FILE_TYPE)
            .toURL(),
        UriComponentsBuilder.fromUri(storeUrl)
            .path(StoreController.RETRIEVE_DATA_URL_TEMPLATE)
            .build(datasetId, IRM_TYPE)
            .toURL(),
        UriComponentsBuilder.fromUri(storeUrl)
            .path(StoreController.RETRIEVE_DATA_URL_TEMPLATE)
            .build(datasetId, METACARD_TYPE)
            .toURL());
  }

  @Override
  public void quarantine(final String datasetId) throws QuarantineException {
    for (StorageAdaptor adaptor :
        List.of(fileStorageAdaptor, irmStorageAdaptor, metacardStorageAdaptor)) {
      adaptor.delete(datasetId);
    }
  }

  @Override
  public void unstage(final String datasetId) throws DatasetNotFoundException, QuarantineException {
    fileStorageAdaptor.updateStatus(datasetId, STORED);
    metacardStorageAdaptor.delete(datasetId);
    log.info("Unstaged file and deleted staged metacard for datasetId={{}}", datasetId);
  }

  private void verifyStoreStatus(String datasetId, StorageAdaptor adaptor) {
    String status = adaptor.getStatus(datasetId);
    // Todo: This should also fail for "staged" statuses once staged retrieval can be locked down to
    // only the Transformation Service
    if (!(STORED.equals(status) || STAGED.equals(status))) {
      log.info("Retrieval failed. Status for datasetId={{}} is not \"stored\".", datasetId);
      throw new RetrieveException(
          String.format("Dataset for Id={%s} has not been stored", datasetId));
    }
  }

  @NotNull
  private Resource validateMetadataResource(ResponseEntity<Resource> metadataResource) {
    if (metadataResource == null) {
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "No response received for Metadata.");
    }

    Resource resource = metadataResource.getBody();
    if (resource == null) {
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "No resource received in Metadata request.");
    }
    return resource;
  }
}
