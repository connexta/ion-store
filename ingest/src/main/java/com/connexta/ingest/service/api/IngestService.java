/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.api;

import com.connexta.ingest.exceptions.StoreException;
import com.connexta.ingest.exceptions.TransformException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/** Provides clients with a way to ingest Products into ION for processing and storage */
public interface IngestService {

  void ingest(
      final Long fileSize,
      @NotEmpty final String mimeType,
      @NotNull final MultipartFile file,
      @NotEmpty final String fileName)
      throws StoreException, TransformException;
}
