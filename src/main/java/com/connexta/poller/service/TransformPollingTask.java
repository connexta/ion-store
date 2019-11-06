/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.service;

import com.connexta.poller.api.PollingTask;
import com.dyngr.core.AttemptResult;
import java.net.URI;
import org.springframework.web.client.RestTemplate;

/**
 * This polling task implements the logic to contact the transform status endpoint and receive
 * status information about a specific job. The polling task will complete when the transform job
 * reaches the complete or failed status. The polling task will also end if the PollingService
 * exceeds the services maximum number of number of retries or reaches other service-specific
 * thresholds.
 *
 * @param <T>
 */
public class TransformPollingTask<T> implements PollingTask<T> {

  private final URI jobStatusUri;
  private final RestTemplate restTemplate;

  public TransformPollingTask(URI jobStatusUri, RestTemplate restTemplate) {
    this.jobStatusUri = jobStatusUri;
    this.restTemplate = restTemplate;
  }

  @Override
  public AttemptResult<T> process() {
    return null;
  }
}
