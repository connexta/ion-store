/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.api;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/** Provides clients with a way to ingest Products into ION for processing and storage */
public interface IngestService {

  void ingest(
      String acceptVersion,
      Long fileSize,
      String mimeType,
      MultipartFile file,
      String title,
      String fileName)
      throws IOException;
}
