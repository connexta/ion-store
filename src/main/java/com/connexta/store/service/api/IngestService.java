/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.api;

import com.connexta.store.exceptions.StoreException;
import com.connexta.store.exceptions.StoreMetacardException;
import com.connexta.store.exceptions.TransformException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** Provides clients with a way to ingest Products for processing and storage */
public interface IngestService {

  void ingest(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mimeType,
      @NotNull final InputStream inputStream,
      @NotBlank final String fileName,
      @NotNull @Min(1L) @Max(10737418240L) final Long metacardFileSize,
      @NotNull final InputStream metacardInputStream)
      throws StoreException, TransformException, StoreMetacardException;

  InputStream retrieveMetacard(@NotBlank final String id);
}
