/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.service;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Poller {

  private final StatusService statusService;
  private final BlockingQueue<URI> transferQueue;
  private ExecutorService executor;
  private AtomicBoolean isStarted;

  public Poller(StatusService statusService, BlockingQueue<URI> transferQueue) {
    this.statusService = statusService;
    this.transferQueue = transferQueue;
    isStarted = new AtomicBoolean(false);
  }

  public void start() {

    // Prevent starting more than once
    if (isStarted.compareAndSet(false, true)) {

      // The stop() method sets this var to null to allow restarts
      if (executor == null) {
        // Nothing magical about the number of threads. We can tweak it after we see how it is
        // working.
        executor = Executors.newFixedThreadPool(32);
      }

      // Start consuming work
      executor.submit(this::beginWorking);
    }
  }

  private void beginWorking() {
    URI statusUri = null;
    while (!executor.isTerminated()) {
      try {
        statusUri = transferQueue.take();
      } catch (InterruptedException e) {

        isStarted.set(false);
        // Reset interrupt
        Thread.currentThread().isInterrupted();
        return;
      }

      // Start polling
      Future<StatusResponse> pollResult = statusService.poll(statusUri);

      // Run worker another thread so processing the transferQueue can continue
      executor.submit(new Job(pollResult));
    }
  }

  public void stop(Duration cleanupTime) {
    List<Runnable> unfinished = Collections.emptyList();
    if (executor != null) {
      executor.shutdown();
      try {
        // Give running tasks time to finish
        if (!executor.awaitTermination(cleanupTime.toMillis(), TimeUnit.MILLISECONDS)) {
          unfinished = executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        unfinished = executor.shutdownNow();
      } finally {
        executor = null;
        isStarted.set(false);
      }
    }
    log.warn("Stopped WorkerService with {} tasks uncompleted", unfinished.size());
  }

  @AllArgsConstructor
  static class Job implements Runnable {

    private final Future<StatusResponse> pollResult;

    public void run() {
      StatusResponse response;
      try {
        response = pollResult.get();
      } catch (InterruptedException | ExecutionException e) {
        // TODO: Follow-on ticket -- Log the interruption
        Thread.currentThread().interrupt();
        return;
      }

      // TODO Follow-on ticket -- add functionality to call Store Service and other work

    }
  }
}
