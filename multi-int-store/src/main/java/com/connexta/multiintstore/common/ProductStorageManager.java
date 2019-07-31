/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

import com.connexta.multiintstore.adaptors.RetrieveResponse;
import com.connexta.multiintstore.adaptors.StorageAdaptor;
import com.connexta.multiintstore.common.exceptions.StorageException;
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
public class ProductStorageManager {

  @NotBlank private final String retrieveEndpoint;
  @NotNull private final StorageAdaptor storageAdaptor;

  public ProductStorageManager(
      @NotBlank final String retrieveEndpoint, @NotNull final StorageAdaptor storageAdaptor) {
    this.retrieveEndpoint = retrieveEndpoint;
    this.storageAdaptor = storageAdaptor;
  }

  public URI storeProduct(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mediaType,
      @NotBlank final String fileName,
      @NotNull final InputStream inputStream)
      throws StorageException, URISyntaxException {
    // TODO: Validate Accept-Version
    final String key = UUID.randomUUID().toString().replace("-", "");

    storageAdaptor.store(fileSize, mediaType, fileName, inputStream, key);
    return new URI(retrieveEndpoint + key);
  }

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * RetrieveResponse}.
   */
  public RetrieveResponse retrieveProduct(@NotBlank String id) throws StorageException {
    return storageAdaptor.retrieve(id);
  }
}
