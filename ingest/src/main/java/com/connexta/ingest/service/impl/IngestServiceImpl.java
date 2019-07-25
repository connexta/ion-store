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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IngestServiceImpl implements IngestService {

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
      @NotBlank final String mimeType,
      @NotNull final InputStream inputStream,
      @NotBlank final String fileName)
      throws StoreException, TransformException {
    final String location = storeClient.store(fileSize, mimeType, inputStream, fileName).toString();
    log.info("{} has been successfully stored and can be downloaded at {}", fileName, location);

    transformClient.requestTransform(fileSize, mimeType, location);
    log.info("Successfully submitted a transform request for {}", fileName);
  }
}
