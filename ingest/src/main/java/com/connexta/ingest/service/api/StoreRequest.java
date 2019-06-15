/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

/** Java object representation of the ingest request */
@Getter
@AllArgsConstructor
public class StoreRequest {
  private final String acceptVersion;
  private final Long fileSize;
  private final String mimeType;
  private final MultipartFile file;
  private final String title;
  private final String fileName;
}
