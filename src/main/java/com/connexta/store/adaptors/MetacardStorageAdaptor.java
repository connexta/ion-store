/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import com.connexta.store.exceptions.StoreMetacardException;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface MetacardStorageAdaptor {

  /**
   * Stores the {@code file} in a blob store using the given {@code key}.
   *
   * @throws IOException if the file can't be read
   * @throws StoreMetacardException if there is an error when attempting to store
   */
  void store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotNull final InputStream inputStream,
      @NotBlank final String key)
      throws StoreMetacardException;

  /**
   * Retrieves the data in a blob store using the given key.
   *
   * @param key the key used to reference the stored object
   * @throws StoreMetacardException
   */
  @NotNull
  InputStream retrieve(@NotBlank final String key) throws StoreMetacardException;
}
