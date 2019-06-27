/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

import com.connexta.multiintstore.callbacks.FinishedCallback;
import com.connexta.multiintstore.callbacks.MetadataCallback;
import com.connexta.multiintstore.callbacks.ProductCallback;
import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.models.Product;
import com.connexta.multiintstore.services.api.Dao;
import com.connexta.multiintstore.services.api.DuplicateIdException;
import com.connexta.multiintstore.services.api.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageManager.class);
  private static final String INDEXED_PRODUCT_METADATA_CALLBACK_TYPE = "cst";

  private final Dao<Product, String> productDao;
  private final Dao<IndexedProductMetadata, String> cstDao;
  private final DataRetriever retriever;

  @Autowired
  public StorageManager(
      Dao<Product, String> productDao,
      Dao<IndexedProductMetadata, String> cstDao,
      DataRetriever retriever) {
    this.productDao = productDao;
    this.cstDao = cstDao;
    this.retriever = retriever;
  }

  /**
   * @throws RetrievalClientException if there is a 400 status code when sending the request from
   *     the callback
   * @throws RetrievalServerException if there was a 500 status code when sending the request from
   *     the callback
   * @throws com.connexta.multiintstore.services.api.DuplicateIdException if there was an Id that
   *     already exists
   * @throws com.connexta.multiintstore.services.api.StorageException if there was an error storing
   *     the callback
   */
  public void handleGeneralCallback(Object callback, String ingestId)
      throws StorageException, DuplicateIdException, RetrievalServerException,
          RetrievalClientException {

    if (callback instanceof ProductCallback) {
      handleCallback((ProductCallback) callback);
    }
    if (callback instanceof FinishedCallback) {
      handleCallback((FinishedCallback) callback);
    }
    if (callback instanceof MetadataCallback) {
      handleCallback((MetadataCallback) callback, ingestId);
    }
  }

  private void handleCallback(ProductCallback callback) {
    LOGGER.info("Product");
    //  TODO :: Check Markings
    //  TODO :: Store Product
  }

  private void handleCallback(MetadataCallback callback, String ingestId)
      throws StorageException, DuplicateIdException, RetrievalServerException,
          RetrievalClientException {

    LOGGER.info("Metadata");
    //  TODO :: Check Markings

    if (callback.getType().equals(INDEXED_PRODUCT_METADATA_CALLBACK_TYPE)) {
      String contents =
          retriever.getMetadata(
              callback.getLocation().toString(), callback.getMimeType(), String.class);

      final IndexedProductMetadata indexedProductMetadata =
          new IndexedProductMetadata(ingestId, contents);
      cstDao.save(indexedProductMetadata);
    } else {
      LOGGER.info(
          "Received non-"
              + INDEXED_PRODUCT_METADATA_CALLBACK_TYPE
              + " metadata, which is not yet supported");
      // TODO
    }
  }

  private void handleCallback(FinishedCallback callback) {
    LOGGER.info("Finished");
    //  TODO :: Remove from Temp-Store
  }
}
