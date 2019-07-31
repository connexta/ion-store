/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

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
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MetadataStorageManager {

  private static final String SUPPORTED_METADATA_TYPE = "cst";

  private final Dao<IndexedProductMetadata, String> cstDao;

  public MetadataStorageManager(@NotNull final Dao<IndexedProductMetadata, String> cstDao) {
    this.cstDao = cstDao;
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

    // TODO verify Media Typeype for CST

    storeCst(productId, inputStream);
  }

  private void storeCst(@NotBlank final String productId, @NotNull final InputStream inputStream)
      throws StorageException {
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
  }
}
