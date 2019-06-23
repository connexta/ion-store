/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.retrieve.impl;

import com.connexta.multiintstore.retrieve.RetrieveService;
import com.connexta.multiintstore.storageadaptor.RetrieveResponse;
import com.connexta.multiintstore.storageadaptor.StorageAdaptor;
import java.io.IOException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class RetrieveServiceImpl implements RetrieveService {

  @NotNull private final StorageAdaptor storageAdaptor;

  public RetrieveServiceImpl(@NotNull final StorageAdaptor storageAdaptor) {
    this.storageAdaptor = storageAdaptor;
  }

  @Override
  @NotNull
  public RetrieveResponse retrieve(@NotEmpty final String ingestId) throws IOException {
    return storageAdaptor.retrieve(ingestId);
  }
}
