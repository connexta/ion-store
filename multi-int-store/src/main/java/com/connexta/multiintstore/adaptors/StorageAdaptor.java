/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.adaptors;

import com.connexta.multiintstore.common.exceptions.StorageException;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface StorageAdaptor {

  /**
   * Stores the {@code file} in a blob store using the given {@code key}.
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
   * @throws StorageException
   */
  RetrieveResponse retrieve(String key) throws StorageException;
}
