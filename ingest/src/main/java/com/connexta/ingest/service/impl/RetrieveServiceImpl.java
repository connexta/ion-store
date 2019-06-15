/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.impl;

import com.connexta.ingest.adaptors.S3StorageAdaptor;
import com.connexta.ingest.service.api.RetrieveResponse;
import com.connexta.ingest.service.api.RetrieveService;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

@Service
public class RetrieveServiceImpl implements RetrieveService {

  @NotNull private final S3StorageAdaptor s3Adaptor;

  public RetrieveServiceImpl(@NotNull final S3StorageAdaptor s3Adaptor) {
    this.s3Adaptor = s3Adaptor;
  }

  @Override
  public RetrieveResponse retrieve(final String ingestId) throws IOException {
    return s3Adaptor.retrieve(ingestId);
  }
}
