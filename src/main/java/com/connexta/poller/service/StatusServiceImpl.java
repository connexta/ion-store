/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.service;

import static com.dyngr.core.AttemptResults.continueFor;
import static com.dyngr.core.AttemptResults.justContinue;
import static com.dyngr.core.AttemptResults.justFinish;

import com.connexta.store.exceptions.DetailedResponseStatusException;
import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * The class's responsibilities are to manage the settings for polling and kick off new polling
 * tasks. Other responsibilities include settings the wait and retry behavior.
 */
@Slf4j
@AllArgsConstructor
public class StatusServiceImpl implements StatusService {

  @Min(0)
  private final int secondsBetweenRetries;

  @Min(1)
  private final int secondsToLive;

  @NotNull private final ExecutorService executorService;
  @NotNull private final WebClient webClient;

  @Override
  @SuppressWarnings("unused")
  public void submit(URI uri) {
    builder().polling(getAtemptMaker(uri)).build().start();
  }

  private AttemptMaker<Void> getAtemptMaker(URI uri) {
    return () -> {
      StatusResponse statusResponse = null;
      try {
        statusResponse =
            webClient
                .get()
                .uri(uri)
                .retrieve()
                .onStatus(
                    httpStatus -> !HttpStatus.OK.equals(httpStatus),
                    clientResponse ->
                        Mono.error(
                            () ->
                                new DetailedResponseStatusException(
                                    clientResponse.statusCode(),
                                    String.format("Could not get status for %s", uri.toString()))))
                .bodyToMono(StatusResponse.class)
                .block();
      } catch (Exception e) {
        continueFor(e);
      }

      // TODO: Follow-on ticket -- If the transform job is done, stop polling
      if (statusResponse != null && "complete".equalsIgnoreCase(statusResponse.getStatus())) {

        // TODO: Is this were the everyone expects the post-status logic to happen?

        // Stop polling
        return justFinish();
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
