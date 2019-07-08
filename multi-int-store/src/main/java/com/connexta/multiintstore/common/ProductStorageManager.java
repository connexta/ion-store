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
import java.net.URL;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ProductStorageManager {

  @NotEmpty private final String retrieveEndpoint;
  @NotNull private final StorageAdaptor storageAdaptor;

  public ProductStorageManager(
      @NotEmpty @Value("${endpointUrl.retrieve}") final String retrieveEndpoint,
      @NotNull final StorageAdaptor storageAdaptor) {
    this.retrieveEndpoint = retrieveEndpoint;
    this.storageAdaptor = storageAdaptor;
  }

  public URL storeProduct(
      String acceptVersion, Long fileSize, String mimeType, MultipartFile file, String fileName)
      throws IOException, StorageException {
    // TODO: Validate Accept-Version
    final String key = UUID.randomUUID().toString().replace("-", "");

    storageAdaptor.store(mimeType, file, fileSize, fileName, key);
    return new URL(retrieveEndpoint + key);
  }

  public RetrieveResponse retrieveProduct(String id) throws StorageException {
    return storageAdaptor.retrieve(id);
  }
}
