/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.adaptors;

import com.connexta.ingest.service.api.RetrieveResponse;
import com.connexta.ingest.service.api.StoreRequest;
import java.io.IOException;

public interface StorageAdaptor {

  void store(StoreRequest storeRequest, String key) throws IOException;

  RetrieveResponse retrieve(String key) throws IOException;
}
