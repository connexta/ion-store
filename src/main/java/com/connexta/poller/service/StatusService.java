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
import com.dyngr.core.AttemptResult;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * This is class is meant to be instantiated as a Bean, Component, or Service. The class's
 * responsibilities are to manage the settings for polling and kick off new polling tasks. Other
 * responsibilities include settings the wait and retry behavior. It also creates and configures the
 * executor, and ensures that it is shutdown properly.
 */
@Service
@Slf4j
public class StatusService {

  private final RestTemplate restTemplate;
  private final int corePoolSize;
  private final int sleepTime;
  ExecutorService executorService;
  private final int giveUpAfter;

  public StatusService(RestTemplate restTemplate) {
    // These values can be changed or made configurable.
    corePoolSize = 64;
    giveUpAfter = 20;
    sleepTime = 1;
    this.restTemplate = restTemplate;
    executorService = Executors.newFixedThreadPool(corePoolSize);
  }

  public Future<StatusResponse> poll(URI statusUri) {

    return builder().polling(new Task(statusUri)).build().start();
  }

  private PollerBuilder builder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(giveUpAfter, TimeUnit.SECONDS));
  }

  class Task implements AttemptMaker<StatusResponse> {

    private final URI uri;

    Task(URI uri) {
      this.uri = uri;
    }

    public AttemptResult<StatusResponse> process() {
      StatusResponse statusResponse = null;
      try {
        ResponseEntity<StatusResponse> entity =
            restTemplate.getForEntity(uri, StatusResponse.class);
        statusResponse = entity.getBody();

      } catch (HttpStatusCodeException e) {
        // TODO Follow-on ticket -- Handle (4XX) and (5XX) responses

      } catch (ResourceAccessException e) {
        // ResourceAccessException thrown if host is not available, unreachable, or not
        // listening on the port. Exit this attempt, but try again later.
        continueFor(e);
      }

      //    If the transform job is done, stop polling
      if (statusResponse != null && isDone(statusResponse)) {

        // Stop polling
        return finishWith(statusResponse);
      }

      // Job not done, keep polling
      return justContinue();
    }
  }

  // Return true to stop polling
  // TODO: Follow-on ticket --
  private boolean isDone(StatusResponse response) {
    return "complete".equals(response.getStatus());
  }
}
