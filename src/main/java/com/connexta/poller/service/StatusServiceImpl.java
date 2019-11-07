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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

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
  @NotNull private final RestTemplate restTemplate;

  @Override
  @SuppressWarnings("unused")
  public void submit(URI uri) {
    StatusResponse status = null;
    try {
      status = poll(uri);
    } catch (ExecutionException | InterruptedException e) {
      log.info("Polling failed for {}", uri);
      return;
    }
    process(status);
  }

  private StatusResponse poll(URI uri) throws ExecutionException, InterruptedException {
    Future<StatusResponse> statusFuture = builder().polling(getAttemptMaker(uri)).build().start();
    return statusFuture.get();
  }

  private AttemptMaker<StatusResponse> getAttemptMaker(URI uri) {
    return () -> {
      StatusResponse statusResponse = null;
      try {
        statusResponse = restTemplate.getForObject(uri, StatusResponse.class);
      } catch (ResourceAccessException e) {
        continueFor(e);
      }

      // TODO: If the transform job is done, stop polling
      if (statusResponse != null && "complete".equalsIgnoreCase(statusResponse.getStatus())) {

        // Stop polling
        return finishWith(statusResponse);
      }

      // Job not done, keep polling
      return justContinue();
    };
  }

  protected void process(StatusResponse status) {
    // TODO: Handle transformation
  }

  private PollerBuilder builder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(secondsBetweenRetries, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(secondsToLive, TimeUnit.SECONDS));
  }
}
