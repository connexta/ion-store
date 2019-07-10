/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.impl;

import com.connexta.ingest.client.StoreClient;
import com.connexta.ingest.client.TransformClient;
import com.connexta.ingest.exceptions.StoreException;
import com.connexta.ingest.exceptions.TransformException;
import com.connexta.ingest.service.api.IngestService;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IngestServiceImpl implements IngestService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IngestServiceImpl.class);

  @NotNull private final StoreClient storeClient;
  @NotNull private final TransformClient transformClient;

  public IngestServiceImpl(
      @NotNull final StoreClient storeClient, @NotNull final TransformClient transformClient) {
    this.storeClient = storeClient;
    this.transformClient = transformClient;
  }

  @Override
  public void ingest(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotEmpty final String mimeType,
      @NotNull final InputStream inputStream,
      @NotEmpty final String fileName)
      throws StoreException, TransformException {
    final String location = storeClient.store(fileSize, mimeType, inputStream, fileName).toString();
    LOGGER.info("{} has been successfully stored and can be downloaded at {}", fileName, location);

    transformClient.requestTransform(fileSize, mimeType, location);
    LOGGER.info("Successfully submitted a transform request for {}", fileName);
  }
}
