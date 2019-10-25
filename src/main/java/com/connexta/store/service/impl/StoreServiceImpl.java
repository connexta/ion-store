/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import com.connexta.store.adaptors.RetrieveResponse;
import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.clients.IndexDatasetClient;
import com.connexta.store.exceptions.CreateDatasetException;
import com.connexta.store.exceptions.IndexMetadataException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.service.api.StoreService;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class StoreServiceImpl implements StoreService {

  @NotBlank private final String retrieveEndpoint;
  @NotNull private final StorageAdaptor storageAdaptor;
  @NotNull private final IndexDatasetClient indexDatasetClient;

  @Override
  public @NotNull URI createDataset(
      @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @NotBlank String mediaType,
      @NotBlank String fileName,
      @NotNull InputStream fileInputStream)
      throws CreateDatasetException, URISyntaxException {
    final String key = UUID.randomUUID().toString().replace("-", "");
    storageAdaptor.store(fileSize, mediaType, fileName, fileInputStream, key);
    return new URI(retrieveEndpoint + key);
  }

  @Override
  public @NotNull RetrieveResponse retrieveFile(
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws RetrieveException {
    return storageAdaptor.retrieve(datasetId);
  }

  @Override
  public void addMetadata(
      @NotNull final InputStream irmInputStream,
      @NotNull @Min(1L) @Max(10737418240L) final long fileSize,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws IndexMetadataException {
    // TODO check that the dataset exists
    // TODO store IRM
    indexDatasetClient.indexDataset(irmInputStream, fileSize, datasetId);
  }
}
