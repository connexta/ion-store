/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.impl;

import com.connexta.ingest.service.api.IngestResponse;

public class IngestResponseImpl implements IngestResponse {

  @Override
  public boolean isStarted() {
    return true;
  }
}
