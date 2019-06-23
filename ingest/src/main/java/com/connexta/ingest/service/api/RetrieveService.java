/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.api;

import com.connexta.multiintstore.storageadaptor.RetrieveResponse;
import java.io.IOException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public interface RetrieveService {

  @NotNull
  RetrieveResponse retrieve(@NotEmpty String ingestId) throws IOException;
}
