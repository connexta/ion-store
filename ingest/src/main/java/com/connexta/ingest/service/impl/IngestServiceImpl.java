/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.impl;

import com.connexta.ingest.service.api.IngestService;
import com.connexta.ingest.transform.TransformClient;
import com.connexta.multiintstore.storageadaptor.StorageAdaptor;
import com.connexta.transformation.rest.models.TransformRequest;
import com.connexta.transformation.rest.models.TransformResponse;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestServiceImpl implements IngestService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IngestServiceImpl.class);

  @NotNull private final StorageAdaptor storageAdaptor;
  @NotNull private final TransformClient transformClient;
  @NotEmpty private final String callbackEndpoint;
  @NotEmpty private final String retrieveEndpoint;

  public IngestServiceImpl(
      @NotNull final StorageAdaptor storageAdaptor,
      @NotNull final TransformClient transformClient,
      @NotEmpty @Value("${endpointUrl.ingest.callback}") final String callbackEndpoint,
      @NotEmpty @Value("${endpointUrl.ingest.retrieve}") final String retrieveEndpoint) {
    this.storageAdaptor = storageAdaptor;
    this.transformClient = transformClient;
    this.callbackEndpoint = callbackEndpoint;
    this.retrieveEndpoint = retrieveEndpoint;
    LOGGER.info("Multi-Int-Store Callback URL: {}", callbackEndpoint);
    LOGGER.info("Retrieve URL: {}", retrieveEndpoint);
  }

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
    storageAdaptor.store(mimeType, file, fileName, ingestId);

    // TODO get this URL programmatically
    final String url = new URL(retrieveEndpoint + ingestId).toString();
    LOGGER.info("{} has been successfully stored in S3 and can be downloaded at {}", fileName, url);

    final TransformRequest transformRequest = new TransformRequest();
    transformRequest.setBytes(fileSize);
    transformRequest.setCallbackUrl(callbackEndpoint + ingestId);
    transformRequest.setId("1"); // TODO This should be removed from the API
    transformRequest.setMimeType(mimeType);
    transformRequest.setProductLocation("prod"); // TODO This should be removed from the API
    transformRequest.setStagedLocation(url);

    final TransformResponse transformResponse = transformClient.requestTransform(transformRequest);

    LOGGER.warn("Completed transform request, response is {}", transformResponse);
  }
}
