/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import com.connexta.store.adaptors.RetrieveResponse;
import com.connexta.store.adaptors.StorageAdaptor;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreServiceImpl implements StoreService {

  @NotBlank private final String retrieveEndpoint;
  @NotNull private final StorageAdaptor storageAdaptor;

  public StoreServiceImpl(
      @NotBlank final String retrieveEndpoint, @NotNull final StorageAdaptor storageAdaptor) {
    this.retrieveEndpoint = retrieveEndpoint;
    this.storageAdaptor = storageAdaptor;
  }

  @Override
  public @NotNull URI createProduct(
      @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @NotBlank String mediaType,
      @NotBlank String fileName,
      @NotNull InputStream inputStream)
      throws StoreException, URISyntaxException {
    // TODO: Validate Accept-Version
    final String key = UUID.randomUUID().toString().replace("-", "");

    storageAdaptor.store(fileSize, mediaType, fileName, inputStream, key);

    return new URI(retrieveEndpoint + key);
  }

  @Override
  public @NotNull RetrieveResponse retrieveProduct(@NotBlank String id) throws StoreException {
    return storageAdaptor.retrieve(id);
  }
}
