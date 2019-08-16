/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.api;

import com.connexta.store.adaptors.RetrieveResponse;
import com.connexta.store.common.exceptions.StoreException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface StoreService {

  @NotNull
  URI createProduct(
      @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @NotBlank String mediaType,
      @NotBlank String fileName,
      @NotNull InputStream inputStream)
      throws StoreException, URISyntaxException;

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * RetrieveResponse}.
   */
  @NotNull
  RetrieveResponse retrieveProduct(@NotBlank String id) throws StoreException;
}
