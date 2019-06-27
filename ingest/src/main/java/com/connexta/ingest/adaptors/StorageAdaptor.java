/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.adaptors;

import com.connexta.ingest.exceptions.StorageException;
import com.connexta.ingest.service.api.RetrieveResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface StorageAdaptor {

  /**
   * Stores the {@code file} in a in a blob store using the given {@code key}.
   *
   * @throws IOException if the file can't be read
   * @throws StorageException if there is an error when attempting to store
   */
  void store(
      final String mimeType,
      final MultipartFile file,
      final Long fileSize,
      final String fileName,
      final String key)
      throws IOException, StorageException;

  /**
   * Retrieves the data in a blob store using the given key.
   *
   * @param key the key used to reference the stored object
   * @return a {@link RetrieveResponse} containing the object for the given key
   * @throws IOException if the file content cannot be read
   */
  RetrieveResponse retrieve(String key) throws IOException;
}
