/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import com.connexta.store.adaptors.RetrieveResponse;
import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.clients.IndexClient;
import com.connexta.store.common.exceptions.StoreException;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreServiceImpl implements StoreService {

  @NotBlank private final String retrieveEndpoint;
  @NotNull private final StorageAdaptor storageAdaptor;
  @NotNull private final IndexClient indexClient;

  public StoreServiceImpl(
      @NotBlank final String retrieveEndpoint,
      @NotNull final StorageAdaptor storageAdaptor,
      @NotNull final IndexClient indexClient) {
    this.retrieveEndpoint = retrieveEndpoint;
    this.storageAdaptor = storageAdaptor;
    this.indexClient = indexClient;
  }

  @Override
  public @NotNull URI createProduct(
      @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @NotBlank String mediaType,
      @NotBlank String fileName,
      @NotNull InputStream inputStream)
      throws StoreException, URISyntaxException, InterruptedException {
    Thread sleepyGuy = new Thread();
    sleepyGuy.sleep(1000);
    final String key = UUID.randomUUID().toString().replace("-", "");
    storageAdaptor.store(fileSize, mediaType, fileName, inputStream, key);
    return new URI(retrieveEndpoint + key);
  }

  @Override
  public @NotNull RetrieveResponse retrieveProduct(@NotBlank String id) throws StoreException {
    return storageAdaptor.retrieve(id);
  }

  @Override
  public void indexProduct(
      @NotNull final InputStream cstInputStream,
      @NotNull @Min(1L) @Max(10737418240L) final long fileSize,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String productId)
      throws StoreException {
    // TODO check that the product exists
    indexClient.index(cstInputStream, fileSize, productId);
  }
}
