/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import com.connexta.store.adaptors.MetacardStorageAdaptor;
import com.connexta.store.clients.StoreClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.exceptions.StoreException;
import com.connexta.store.exceptions.StoreMetacardException;
import com.connexta.store.exceptions.TransformException;
import com.connexta.store.service.api.IngestService;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class IngestServiceImpl implements IngestService {

  @NotNull private final StoreClient storeClient;
  @NotNull private final MetacardStorageAdaptor metacardStorageAdaptor;
  @NotBlank private final String retrieveEndpoint;
  @NotNull private final TransformClient transformClient;

  @Override
  public void ingest(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mimeType,
      @NotNull final InputStream inputStream,
      @NotBlank final String fileName,
      @NotNull @Min(1L) @Max(10737418240L) final Long metacardFileSize,
      @NotNull final InputStream metacardInputStream)
      throws StoreException, TransformException, StoreMetacardException {
    final URI location = storeClient.store(fileSize, mimeType, inputStream, fileName);

    final String key = UUID.randomUUID().toString().replace("-", "");
    // TODO verify mimetype of metacard
    metacardStorageAdaptor.store(metacardFileSize, metacardInputStream, key);
    final URI metacardLocation;
    try {
      metacardLocation = new URI(retrieveEndpoint + key);
    } catch (URISyntaxException e) {
      throw new StoreMetacardException("Unable to construct retrieve URI", e);
    }

    transformClient.requestTransform(location, mimeType, metacardLocation);
    log.info("Successfully submitted a transform request for {}", fileName);
  }

  // TODO test this method
  @Override
  public InputStream retrieveMetacard(@NotBlank String id) {
    return metacardStorageAdaptor.retrieve(id);
  }
}
