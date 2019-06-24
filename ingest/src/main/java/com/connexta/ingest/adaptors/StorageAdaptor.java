/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.adaptors;

import com.connexta.ingest.exceptions.StorageException;
import com.connexta.ingest.service.api.RetrieveResponse;
import com.connexta.ingest.service.api.StoreRequest;
import java.io.IOException;

public interface StorageAdaptor {

  /**
   * Stores the data in a {@link StoreRequest} in a blob store using the given key.
   *
   * @param storeRequest the object that contains the file and metadata
   * @param key the key used to reference the stored object
   * @throws IOException if the file in the {@link StoreRequest} can't be read
   * @throws StorageException if there is an error when attempting to store
   */
  void store(StoreRequest storeRequest, String key) throws IOException, StorageException;

  /**
   * Retrieves the data in a blob store using the given key.
   *
   * @param key the key used to reference the stored object
   * @return a {@link RetrieveResponse} containing the object for the given key
   * @throws IOException if the file content cannot be read
   */
  RetrieveResponse retrieve(String key) throws IOException;
}
