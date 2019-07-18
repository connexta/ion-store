/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.adaptors;

import com.connexta.multiintstore.common.exceptions.StorageException;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public interface StorageAdaptor {

  /**
   * Stores the {@code file} in a blob store using the given {@code key}.
   *
   * @throws IOException if the file can't be read
   * @throws StorageException if there is an error when attempting to store
   */
  void store(
      @NotEmpty final String mimeType,
      @NotNull final InputStream inputStream,
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotEmpty final String fileName,
      @NotEmpty final String key)
      throws StorageException;

  /**
   * Retrieves the data in a blob store using the given key.
   *
   * @param key the key used to reference the stored object
   * @throws StorageException
   */
  //  @NotNull
  //  RetrieveResponse retrieve(@NotEmpty final String key) throws StorageException;
}
