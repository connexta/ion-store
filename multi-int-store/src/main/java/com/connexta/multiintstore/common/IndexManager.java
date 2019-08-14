/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

import com.connexta.multiintstore.adaptors.StorageAdaptor;
import com.connexta.multiintstore.common.exceptions.StorageException;
import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.services.api.Dao;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

@Slf4j
public class IndexManager {

  private final Dao<IndexedProductMetadata, String> cstDao;
  private final StorageAdaptor storageAdaptor;

  public IndexManager(
      @NotNull final Dao<IndexedProductMetadata, String> cstDao,
      @NotNull final StorageAdaptor storageAdaptor) {
    this.cstDao = cstDao;
    this.storageAdaptor = storageAdaptor;
  }

  public void index(
      @NotBlank final String productId,
      @NotBlank final String mediaType,
      @NotNull final InputStream inputStream)
      throws UnsupportedOperationException, StorageException {
    // TODO verify Media Type for CST

    if (storageAdaptor.objectExists(productId)) {
      final IndexedProductMetadata indexedProductMetadata;
      try {
        indexedProductMetadata =
            new IndexedProductMetadata(
                productId, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new StorageException("Unable to convert metadata to String", e);
      }

      log.info("Attempting to index product id {}", productId);
      cstDao.save(indexedProductMetadata);
    } else {
      throw new StorageException(
          String.format(
              "Unable to index because a product with key \"%s\" does not exist", productId));
    }
  }
}
