/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.poller;

import static com.dyngr.core.AttemptResults.justContinue;
import static com.dyngr.core.AttemptResults.justFinish;

import com.dyngr.PollerBuilder;
import com.dyngr.core.AttemptMaker;
import com.dyngr.core.StopStrategies;
import com.dyngr.core.WaitStrategies;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The class's responsibilities are to manage the settings for polling and kick off new polling
 * tasks. Other responsibilities include settings the wait and retry behavior.
 */
@Component
public class StatusService {

  private final ExecutorService executorService = Executors.newFixedThreadPool(64);
  private final TransformPollerFactory transformPollerFactory;

  public StatusService(
      @Autowired BlockingQueue<TransformStatusTask> transformStatusQueue,
      @Autowired TransformPollerFactory transformPollerFactory) {
    this.transformPollerFactory = transformPollerFactory;
    Executors.newSingleThreadExecutor()
        .submit(
            () -> {
              try {
                submit(transformStatusQueue.take());
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
  }

  private void submit(TransformStatusTask task) {
    builder().polling(getAttemptMaker(task)).build().start();
  }

  private AttemptMaker<Void> getAttemptMaker(TransformStatusTask tansformStatusTask) {
    TransformStatusPoller poller = transformPollerFactory.newPoller(tansformStatusTask);
    return () -> {
      boolean runAgain = poller.run();
      return runAgain ? justContinue() : justFinish();
    };
  }

  private PollerBuilder builder() {
    return PollerBuilder.newBuilder()
        .withExecutorService(executorService)
        .stopIfException(false)
        .withWaitStrategy(WaitStrategies.fixedWait(5L, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(12));
  }
}
