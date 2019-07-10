/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.api;

import com.connexta.ingest.exceptions.StoreException;
import com.connexta.ingest.exceptions.TransformException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/** Provides clients with a way to ingest Products into ION for processing and storage */
public interface IngestService {

  void ingest(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotEmpty final String mimeType,
      @NotNull final InputStream inputStream,
      @NotEmpty final String fileName)
      throws StoreException, TransformException;
}
