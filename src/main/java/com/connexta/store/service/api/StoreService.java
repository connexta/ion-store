/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.api;

import com.connexta.store.adaptors.FileRetrieveResponse;
import com.connexta.store.exceptions.IndexDatasetException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.StoreException;
import com.connexta.store.exceptions.TransformException;
import java.io.InputStream;
import java.net.URI;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public interface StoreService {

  void ingest(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mimeType,
      @NotNull final InputStream inputStream,
      @NotBlank final String fileName,
      @NotNull @Min(1L) @Max(10737418240L) final Long metacardFileSize,
      @NotNull final InputStream metacardInputStream)
      throws StoreException, TransformException;

  @NotNull
  URI createDataset(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mediaType,
      @NotBlank final String fileName,
      @NotNull final InputStream fileInputStream)
      throws StoreException;

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * FileRetrieveResponse}.
   */
  @NotNull
  FileRetrieveResponse retrieveFile(
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws RetrieveException;

  /** The caller is responsible for closing the returned {@link java.io.InputStream}. */
  @NotNull
  InputStream retrieveIrm(
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws RetrieveException;

  /** The caller is responsible for closing the returned {@link java.io.InputStream}. */
  @NotNull
  InputStream retrieveMetacard(@NotBlank final String datasetId) throws RetrieveException;

  void addIrm(
      @NotNull final InputStream irmInputStream,
      @NotNull @Min(1L) @Max(10737418240L) final long fileSize,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws IndexDatasetException;
}
