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

import com.connexta.store.exceptions.DetailedResponseStatusException;
import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This is class is meant to be instantiated as a Bean, Component, or Service. The class's
 * responsibilities are to manage the settings for polling and kick off new polling tasks. Other
 * responsibilities include settings the wait and retry behavior. It also creates and configures the
 * executor, and ensures that it is shutdown properly.
 */
@Service
@Slf4j
@AllArgsConstructor
public class StatusService {

  private final int sleepTime = 1;
  private final int giveUpAfter = 20;
  @NotNull ExecutorService executorService;
  @NotNull WebClient webClient;

  public Future<StatusResponse> poll(URI uri) {
    final HttpStatus expectedHttpStatus = HttpStatus.OK;

    AttemptMaker<StatusResponse> attemptMaker =
        () -> {
          StatusResponse statusResponse = null;
          try {
            statusResponse =
                webClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(
                        httpStatus -> !expectedHttpStatus.equals(httpStatus),
                        clientResponse ->
                            Mono.error(
                                () ->
                                    new DetailedResponseStatusException(
                                        clientResponse.statusCode(),
                                        String.format(
                                            "Could not get status for %s", uri.toString()))))
                    .bodyToMono(StatusResponse.class)
                    .block();
          } catch (Throwable e) {
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
