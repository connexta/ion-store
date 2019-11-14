/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import com.connexta.poller.service.StatusService;
import com.connexta.store.adaptors.FileRetrieveResponse;
import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.adaptors.StorageAdaptorRetrieveResponse;
import com.connexta.store.clients.IndexDatasetClient;
import com.connexta.store.controllers.StoreController;
import com.connexta.store.exceptions.IndexDatasetException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.StoreException;
import com.connexta.store.service.api.StoreService;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@AllArgsConstructor
public class StoreServiceImpl implements StoreService {

  private static final String FILE_NAME_METADATA_KEY = "Filename";

  @NotBlank private final URI retrieveUri;
  @NotNull private final StorageAdaptor fileStorageAdaptor;
  @NotNull private final StorageAdaptor irmStorageAdaptor;
  @NotNull private final IndexDatasetClient indexDatasetClient;
  @NotNull private final StatusService statusService;

  @Override
  @NotNull
  public URI createDataset(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mediaType,
      @NotBlank final String fileName,
      @NotNull final InputStream fileInputStream)
      throws StoreException {
    final String datasetId = UUID.randomUUID().toString().replace("-", "");
    fileStorageAdaptor.store(
        fileSize, mediaType, fileInputStream, datasetId, Map.of(FILE_NAME_METADATA_KEY, fileName));
    return UriComponentsBuilder.fromUri(retrieveUri)
        .path(StoreController.RETRIEVE_FILE_URL_TEMPLATE)
        .build(datasetId);
  }

  @Override
  public @NotNull FileRetrieveResponse retrieveFile(
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws RetrieveException {
    final StorageAdaptorRetrieveResponse storageAdaptorRetrieveResponse =
        fileStorageAdaptor.retrieve(datasetId);

    final String fileName =
        storageAdaptorRetrieveResponse.getMetadata().get(FILE_NAME_METADATA_KEY);
    if (StringUtils.isEmpty(fileName)) {
      throw new RetrieveException(
          String.format(
              "Expected S3 object to have a non-null metadata value for %s",
              FILE_NAME_METADATA_KEY));
    }

    return new FileRetrieveResponse(
        storageAdaptorRetrieveResponse.getMediaType(),
        storageAdaptorRetrieveResponse.getInputStream(),
        fileName);
  }

  @Override
  public @NotNull InputStream retrieveIrm(@NotBlank String datasetId) throws RetrieveException {
    return irmStorageAdaptor.retrieve(datasetId).getInputStream();
  }

  @Override
  public void addIrm(
      @NotNull final InputStream irmInputStream,
      @NotNull @Min(1L) @Max(10737418240L) final long fileSize,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws IndexDatasetException {
    // TODO check that the dataset exists
    irmStorageAdaptor.store(
        fileSize, StoreController.IRM_MEDIA_TYPE_VALUE, irmInputStream, datasetId, Map.of());
    log.info("Successfully stored IRM for datasetId={}", datasetId);
    // TODO Improve how this is injected
    indexDatasetClient.indexDataset(
        datasetId,
        UriComponentsBuilder.fromUri(retrieveUri)
            .path(StoreController.RETRIEVE_IRM_URL_TEMPLATE)
            .build(datasetId));
    log.info("Successfully indexed datasetId={}", datasetId);
  }
}
