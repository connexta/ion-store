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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetadataStorageManager {

  private static final String INDEXED_PRODUCT_METADATA_CALLBACK_TYPE = "cst";

  private final Dao<IndexedProductMetadata, String> cstDao;

  public MetadataStorageManager(@NotNull final Dao<IndexedProductMetadata, String> cstDao) {
    this.cstDao = cstDao;
  }

  public void storeMetadata(
      @NotEmpty String acceptVersion,
      @NotEmpty String productId,
      @NotEmpty String metadataType,
      @NotEmpty String mimeType,
      @NotNull InputStream inputStream,
      @NotEmpty String fileName)
      throws IOException, StorageException {
    if (metadataType.equals(INDEXED_PRODUCT_METADATA_CALLBACK_TYPE)) {
      final IndexedProductMetadata indexedProductMetadata =
          new IndexedProductMetadata(productId, IOUtils.toString(inputStream, "UTF-8"));
      log.info("Attempting to store {} with name \"{}\" in Solr", metadataType, fileName);
      cstDao.save(indexedProductMetadata);
    } else {
      log.info(
          "Received non-"
              + INDEXED_PRODUCT_METADATA_CALLBACK_TYPE
              + " metadata, which is not yet supported");
    }
  }
}
