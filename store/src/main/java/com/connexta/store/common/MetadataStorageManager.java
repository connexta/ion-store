/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.common;

import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.common.exceptions.StorageException;
import com.connexta.store.models.IndexedProductMetadata;
import com.connexta.store.services.api.Dao;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MetadataStorageManager {

  private static final String SUPPORTED_METADATA_TYPE = "cst";

  private final Dao<IndexedProductMetadata, String> cstDao;
  private final StorageAdaptor storageAdaptor;

  public MetadataStorageManager(
      @NotNull final Dao<IndexedProductMetadata, String> cstDao,
      @NotNull StorageAdaptor storageAdaptor) {
    this.cstDao = cstDao;
    this.storageAdaptor = storageAdaptor;
  }

  public void storeMetadata(
      @NotBlank final String productId,
      @NotBlank final String metadataType,
      @NotBlank final String mediaType,
      @NotNull final InputStream inputStream)
      throws UnsupportedOperationException, StorageException {
    if (!StringUtils.equals(SUPPORTED_METADATA_TYPE, metadataType)) {
      final String message = metadataType + " is not a supported metadata type";
      log.warn(message);
      throw new UnsupportedOperationException(message);
    }

    // TODO verify Media Type for CST

    storeCst(productId, inputStream);
  }

  private void storeCst(@NotBlank final String productId, @NotNull final InputStream inputStream)
      throws StorageException {
    if (storageAdaptor.objectExists(productId)) {
      final IndexedProductMetadata indexedProductMetadata;
      try {
        indexedProductMetadata =
            new IndexedProductMetadata(
                productId, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new StorageException("Unable to convert metadata to String", e);
      }

      log.info(
          "Attempting to store {} metadata for product id {}", SUPPORTED_METADATA_TYPE, productId);
      cstDao.save(indexedProductMetadata);
    } else {
      throw new StorageException(
          String.format(
              "Unable to store Metadata because a product with key \"%s\" does not exist",
              productId));
    }
  }
}
