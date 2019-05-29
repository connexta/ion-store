/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.impl;

import com.connexta.ingest.service.api.IngestRequest;
import com.connexta.ingest.service.api.IngestResponse;
import com.connexta.ingest.service.api.IngestService;
import org.springframework.stereotype.Service;

@Service
public class IngestServiceImpl implements IngestService {
  @Override
  public IngestResponse ingest(IngestRequest request) {
    return null;
  }
}
