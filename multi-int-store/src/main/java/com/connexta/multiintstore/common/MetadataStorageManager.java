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
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class MetadataStorageManager {

  private static final String INDEXED_PRODUCT_METADATA_CALLBACK_TYPE = "cst";

  private final Dao<IndexedProductMetadata, String> cstDao;

  public MetadataStorageManager(@NotNull final Dao<IndexedProductMetadata, String> cstDao) {
    this.cstDao = cstDao;
  }

  public void storeMetadata(
      String acceptVersion,
      String productId,
      String metadataType,
      String mimeType,
      MultipartFile file,
      String fileName)
      throws IOException, StorageException {
    if (metadataType.equals(INDEXED_PRODUCT_METADATA_CALLBACK_TYPE)) {
      final IndexedProductMetadata indexedProductMetadata =
          new IndexedProductMetadata(productId, new String(file.getBytes(), "UTF-8"));
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
