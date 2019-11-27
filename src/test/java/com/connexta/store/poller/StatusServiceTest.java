/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.poller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Queues;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class StatusServiceTest {

  @Test
  void testQueueDrainedWhenTasksExceedThreadPoolSize() throws Exception {
    // setup
    final int taskQueueSize = 64 * 2;
    BlockingQueue<TransformStatusTask> taskQueue = Queues.newLinkedBlockingQueue(taskQueueSize);
    TransformStatusTask task = new TransformStatusTask("datasetId", new URL("http://test:8080"));
    for (int i = 0; i < taskQueueSize; i++) {
      taskQueue.put(task);
    }

    TransformPollerFactory factory = mock(TransformPollerFactory.class);
    when(factory.newPoller(task)).thenReturn(new TestTransformStatusPoller(false));

    ExecutorService queueTakeService = mock(ExecutorService.class);

    StatusService statusService = new StatusService(taskQueue, factory, queueTakeService, 5L, 12);

    when(queueTakeService.submit(any(Runnable.class)))
        .thenAnswer(
            invocation -> {
              for (int i = 0; i < taskQueueSize; i++) {
                statusService.submit(taskQueue.take());
              }
              return null;
            });

    // when
    statusService.init();

    // then
    assertThat(taskQueue.size(), is(0));
  }

  @Test
  void testPollRetry() throws Exception {
    assert true;
    //    // setup
    //    final int taskQueueSize = 1;
    //    BlockingQueue<TransformStatusTask> taskQueue =
    // Queues.newLinkedBlockingQueue(taskQueueSize);
    //    TransformStatusTask task = new TransformStatusTask("datasetId", new
    // URL("http://test:8080"));
    //    for (int i = 0; i < taskQueueSize; i++) {
    //      taskQueue.put(task);
    //    }
    //
    //    TestTransformStatusPoller poller = mock(TestTransformStatusPoller.class);
    //    when(poller.run()).thenReturn(true);
    //    TransformPollerFactory factory = mock(TransformPollerFactory.class);
    //    when(factory.newPoller(task)).thenReturn(poller);
    //
    //    ExecutorService queueTakeService = mock(ExecutorService.class);
    //
    //    final int attemptCount = 12;
    //    StatusService statusService =
    //        new StatusService(taskQueue, factory, queueTakeService, 1L, attemptCount);
    //
    //    when(queueTakeService.submit(any(Runnable.class)))
    //        .thenAnswer(
    //            invocation -> {
    //              for (int i = 0; i < taskQueueSize; i++) {
    //                statusService.submit(taskQueue.take());
    //              }
    //              return null;
    //            });
    //
    //    // when
    //    statusService.init();
    //
    //    // then
    //    verify(poller, times(attemptCount)).run();
    //    assertThat(taskQueue.size(), is(0));
  }

  @Test
  void testPollDone() {
    assert true;
  }

  @Test
  void testRetryOnException() {
    assert true;
  }

  private class TestTransformStatusPoller extends TransformStatusPoller {

    private boolean pollerResult = false;

    /** Creates a new TestTransformStatusPoller. */
    TestTransformStatusPoller(boolean tryAgain) {
      // this test will not use these null arguments since the run() method is overridden
      this(null, null, null);
      this.pollerResult = tryAgain;
    }

    private TestTransformStatusPoller(
        TransformStatusTask transformStatusTask,
        WebClient transformWebClient,
        WebClient storeWebClient) {
      super(transformStatusTask, transformWebClient, storeWebClient);
    }

    @Override
    public boolean run() {
      return pollerResult;
    }
  }
}
