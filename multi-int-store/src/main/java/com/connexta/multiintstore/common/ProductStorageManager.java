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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductStorageManager {

  @NotEmpty private final String retrieveEndpoint;
  @NotNull private final StorageAdaptor storageAdaptor;

  public ProductStorageManager(
      @NotEmpty final String retrieveEndpoint, @NotNull final StorageAdaptor storageAdaptor) {
    this.retrieveEndpoint = retrieveEndpoint;
    this.storageAdaptor = storageAdaptor;
  }

  public URI storeProduct(
      @NotEmpty String acceptVersion,
      @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @NotEmpty String mimeType,
      @NotNull InputStream inputStream,
      @NotEmpty String fileName)
      throws IOException, StorageException, URISyntaxException {
    // TODO: Validate Accept-Version
    final String key = UUID.randomUUID().toString().replace("-", "");

    storageAdaptor.store(mimeType, inputStream, fileSize, fileName, key);
    return new URI(retrieveEndpoint + key);
  }

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * RetrieveResponse}.
   */
  public RetrieveResponse retrieveProduct(@NotEmpty String id) throws StorageException {
    return storageAdaptor.retrieve(id);
  }
}
