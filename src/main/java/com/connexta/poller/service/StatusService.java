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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * This is class is meant to be instantiated as a Bean, Component, or Service. The class's
 * responsibilities are to manage the settings for polling and kick off new polling tasks. Other
 * responsibilities include settings the wait and retry behavior. It also creates and configures the
 * executor, and ensures that it is shutdown properly.
 */
@Service
@Slf4j
public class StatusService {

  private final int sleepTime;
  ExecutorService executorService;
  private final int giveUpAfter;

  public StatusService(ExecutorService executorService) {
    // These values can be changed or made configurable.
    giveUpAfter = 20;
    sleepTime = 1;
    this.executorService = executorService;
  }

  public Future<StatusResponse> poll(URI uri) {

    AttemptMaker<StatusResponse> attemptMaker =
        () -> {
          StatusResponse statusResponse = null;
          try {
            WebClient webClient = WebClient.create(uri.toString());
            RequestHeadersUriSpec<?> request = webClient.get();
            statusResponse = request.exchange().block().bodyToMono(StatusResponse.class).block();

          } catch (WebClientException e) {
            // TODO Follow-on ticket -- Handle (4XX) and (5XX) responses
            // TODO Test this catch

          } catch (ResourceAccessException e) {
            // TODO Test this catch. Not sure if same exception is thrown since switch to WebClient
            // ResourceAccessException thrown if host is not available, unreachable, or not
            // listening on the port. Exit this attempt, but try again later.
            continueFor(e);
          }

          // TODO: Follow-on ticket -- If the transform job is done, stop polling
          if (statusResponse != null && "complete".equalsIgnoreCase(statusResponse.getStatus())) {

            // Stop polling
            return finishWith(statusResponse);
          }

          // Job not done, keep polling
          return justContinue();
        };

    return builder().polling(attemptMaker).build().start();
  }

  private PollerBuilder builder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(giveUpAfter, TimeUnit.SECONDS));
  }
}
