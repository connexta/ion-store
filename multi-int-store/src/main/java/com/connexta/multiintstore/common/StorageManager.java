/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

import com.connexta.multiintstore.common.adaptors.S3StorageAdaptor;
import com.connexta.multiintstore.common.exceptions.StorageException;
import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.services.api.Dao;
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
public class StorageManager {

  private static final String INDEXED_PRODUCT_METADATA_CALLBACK_TYPE = "cst";

  @NotEmpty private final String retrieveEndpoint;

  @NotNull private final S3StorageAdaptor s3;
  @NotNull private final Dao<IndexedProductMetadata, String> cstDao;

  public StorageManager(
      @NotEmpty @Value("${endpointUrl.retrieve}") final String retrieveEndpoint,
      @NotNull final S3StorageAdaptor s3,
      @NotNull final Dao<IndexedProductMetadata, String> cstDao) {
    this.retrieveEndpoint = retrieveEndpoint;
    this.s3 = s3;
    this.cstDao = cstDao;
  }

  public URL storeProduct(
      String acceptVersion, Long fileSize, String mimeType, MultipartFile file, String fileName)
      throws IOException, StorageException {

    // TODO: Validate Accept-Version
    final String key = UUID.randomUUID().toString().replace("-", "");

    // Store in S3
    s3.store(mimeType, file, fileSize, fileName, key);
    // return product location
    return new URL(retrieveEndpoint + key);
  }

  public void storeMetdata(
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
