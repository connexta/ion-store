/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import com.connexta.store.exceptions.DatasetNotFoundException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.StoreException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface StorageAdaptor {

  /**
   * Stores the {@code file} in a blob store using the given {@code key}.
   *
   * @throws IOException if the file can't be read
   * @throws StoreException if there is an error when attempting to store
   */
  void store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      final String mediaType,
      @NotNull final InputStream inputStream,
      @NotBlank final String key,
      Map<String, String> metadata)
      throws StoreException;

  /**
   * Retrieves the data in a blob store using the given key.
   *
   * @param key the key used to reference the stored object
   */
  @NotNull
  StorageAdaptorRetrieveResponse retrieve(@NotBlank final String key) throws RetrieveException;

  /**
   * Updates the {@link StoreStatus} of {@code dataset} for a given {@code datasetId}
   *
   * @param key the Id of a dataset
   * @param storeStatus the new {@link StoreStatus} for a dataset
   * @throws DatasetNotFoundException if the key does not exist
   */
  void updateStatus(@NotBlank String key, @NotBlank String storeStatus)
      throws DatasetNotFoundException;
}
