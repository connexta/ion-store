/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.impl;

import com.connexta.ingest.adaptors.S3Adaptor;
import com.connexta.ingest.service.api.IngestRequest;
import com.connexta.ingest.service.api.IngestService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestServiceImpl implements IngestService {

  private final S3Adaptor s3Adaptor;

  public IngestServiceImpl(S3Adaptor s3Adaptor) {
    this.s3Adaptor = s3Adaptor;
  }

  @Override
  public UUID ingest(
      String acceptVersion,
      Long fileSize,
      String mimeType,
      MultipartFile file,
      String title,
      String fileName) {

    IngestRequest ingestRequest =
        new IngestRequest(acceptVersion, fileSize, mimeType, file, title, fileName);
    return s3Adaptor.upload(ingestRequest);
  }
}
