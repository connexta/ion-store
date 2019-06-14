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
import com.connexta.ingest.transform.TransformClient;
import com.connexta.transformation.rest.models.TransformRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class IngestServiceImpl implements IngestService {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestServiceImpl.class);
  private final S3Adaptor s3Adaptor;
  private final TransformClient transformClient;

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
    TransformRequest transformRequest = new TransformRequest();
    transformRequest.setBytes(20L);
    transformRequest.setCallbackUrl("http://blah/blah");
    transformRequest.setId("1");
    transformRequest.setMimeType("nitf");
    transformRequest.setProductLocation("prod");
    transformRequest.setStagedLocation("stage");

    UUID ingestId = s3Adaptor.upload(ingestRequest);

    transformClient.requestTransform(transformRequest);

    return ingestId;
  }
}
