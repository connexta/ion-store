/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.adaptors;

import com.connexta.ingest.service.api.IngestRequest;
import java.io.IOException;

public interface Adaptor {

  void upload(IngestRequest ingestRequest, String key) throws IOException;
}
