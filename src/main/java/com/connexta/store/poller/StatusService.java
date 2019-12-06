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
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The class's responsibilities are to manage the settings for polling and kick off new polling
 * tasks. Other responsibilities include settings the wait and retry behavior.
 */
public class StatusService {

  private final AtomicBoolean running = new AtomicBoolean(false);
  private final ExecutorService queueTakeService;
  private final ExecutorService executorService = Executors.newFixedThreadPool(64);
  private final TransformPollerFactory transformPollerFactory;
  private final BlockingQueue<TransformStatusTask> transformStatusQueue;
  private final long attemptInterval;
  private final int attemptCount;

  public StatusService(
      BlockingQueue<TransformStatusTask> transformStatusQueue,
      TransformPollerFactory transformPollerFactory) {
    this(transformStatusQueue, transformPollerFactory, Executors.newSingleThreadExecutor(), 5L, 12);
  }

  @VisibleForTesting
  StatusService(
      BlockingQueue<TransformStatusTask> transformStatusQueue,
      TransformPollerFactory transformPollerFactory,
      ExecutorService queueTakeService,
      long attemptInterval,
      int attemptCount) {
    this.transformStatusQueue = transformStatusQueue;
    this.transformPollerFactory = transformPollerFactory;
    this.queueTakeService = queueTakeService;
    this.attemptInterval = attemptInterval;
    this.attemptCount = attemptCount;
  }

  /** Stats a single thread that takes from the transformStatusQueue and runs pollers. */
  public void init() {
    running.set(true);
    this.queueTakeService.submit(
        () -> {
          while (running.get()) {
            try {
              submit(transformStatusQueue.take());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        });
  }

  /** Destroy this status service. Shuts down the thread taking from the transformStatusQueue. */
  public void destroy() {
    running.set(false);
    queueTakeService.shutdownNow();
  }

  @VisibleForTesting
  void submit(TransformStatusTask task) {
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
        .withWaitStrategy(WaitStrategies.fixedWait(attemptInterval, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(attemptCount));
  }
}
