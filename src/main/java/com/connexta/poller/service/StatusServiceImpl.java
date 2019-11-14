/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.service;

import static com.dyngr.core.AttemptResults.continueFor;
import static com.dyngr.core.AttemptResults.finishWith;
import static com.dyngr.core.AttemptResults.justContinue;

import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * The class's responsibilities are to manage the settings for polling and kick off new polling
 * tasks. Other responsibilities include settings the wait and retry behavior.
 */
@Slf4j
@AllArgsConstructor
public class StatusServiceImpl implements StatusService {

  private final int secondsBetweenRetries;
  private final int secondsToLive;
  ExecutorService executorService;
  private final RestTemplate restTemplate;

  public void submit(URI uri) {
    builder().polling(make(uri)).build().start();
  }

  private AttemptMaker<StatusResponse> make(URI uri) {
    return () -> {
      StatusResponse statusResponse = null;
      try {
        ResponseEntity<StatusResponse> entity =
            restTemplate.getForEntity(uri, StatusResponse.class);
        statusResponse = entity.getBody();

      } catch (HttpStatusCodeException e) {
        // TODO Handle (4XX) and (5XX) responses

      } catch (ResourceAccessException e) {
        // ResourceAccessException thrown if host is not available, unreachable, or not
        // listening on the port. Exit this attempt, but try again later.
        continueFor(e);
      }

      // TODO:  If the transform job is done, stop polling
      if (statusResponse != null && "complete".equalsIgnoreCase(statusResponse.getStatus())) {

        // Stop polling
        return finishWith(statusResponse);
      }

      // Job not done, keep polling
      return justContinue();
    };
  }

  private PollerBuilder builder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(secondsBetweenRetries, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(secondsToLive, TimeUnit.SECONDS));
  }
}
