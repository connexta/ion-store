/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import java.net.URL;
import java.util.UUID;

public interface IndexClient {

  void indexDataset(UUID datasetId, URL fileUrl, URL irmUrl, URL metacardUrl);
}
