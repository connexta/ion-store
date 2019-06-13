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
import com.connexta.transformation.rest.models.TransformResponse;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class IngestServiceImpl implements IngestService {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestServiceImpl.class);
  private final S3Adaptor s3Adaptor;
  private final TransformClient transformClient;

  @Override
  public void ingest(
      String acceptVersion,
      Long fileSize,
      String mimeType,
      MultipartFile file,
      String title,
      String fileName)
      throws IOException {
    final String ingestId = UUID.randomUUID().toString().replace("-", "");
    s3Adaptor.upload(
        new IngestRequest(acceptVersion, fileSize, mimeType, file, title, fileName), ingestId);

    final String url = new URL("TODO/retrieve/" + ingestId).toString();
    LOGGER.info("{} has been successfully stored in S3 and can be downloaded at {}", fileName, url);

    final TransformRequest transformRequest = new TransformRequest();
    transformRequest.setBytes(fileSize);
    transformRequest.setCallbackUrl("TODO/store/" + ingestId);
    transformRequest.setId("1"); // TODO This should be removed from the API
    transformRequest.setMimeType(mimeType);
    transformRequest.setProductLocation("prod"); // TODO This should be removed from the API
    transformRequest.setStagedLocation(url);

    final TransformResponse transformResponse = transformClient.requestTransform(transformRequest);

    LOGGER.warn("Completed transform request, response is {}", transformResponse);
  }

  @Override
  public ResponseEntity<Resource> retrieve(final String ingestId) {
    final ResponseEntity<Resource> resourceResponseEntity = s3Adaptor.retrieve(ingestId);
    LOGGER.info("Resource \"{}\" has been successfully retrieved from S3", ingestId);
    return resourceResponseEntity;
  }
}
