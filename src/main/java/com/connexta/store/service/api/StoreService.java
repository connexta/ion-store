/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.api;

import com.connexta.store.adaptors.RetrieveResponse;
import com.connexta.store.exceptions.CreateDatasetException;
import com.connexta.store.exceptions.IndexMetadataException;
import com.connexta.store.exceptions.RetrieveException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public interface StoreService {

  @NotNull
  URI createDataset(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mediaType,
      @NotBlank final String fileName,
      @NotNull final InputStream fileInputStream)
      throws CreateDatasetException, URISyntaxException;

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * RetrieveResponse}.
   */
  @NotNull
  RetrieveResponse retrieveFile(
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws RetrieveException;

  void addMetadata(
      @NotNull final InputStream irmInputStream,
      @NotNull @Min(1L) @Max(10737418240L) final long fileSize,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws IndexMetadataException;
}
