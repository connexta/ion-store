/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.connexta.poller.service.Poller;
import com.connexta.poller.service.StatusService;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.config.S3StorageConfiguration;
import com.connexta.store.controllers.StoreController;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class PollerTests {

  // Three mocks to prevent the error "No qualifying bean of type
  // 'com.amazonaws.services.s3.AmazonS3'
  // available: expected at least 1 bean which qualifies as autowire candidate"
  @MockBean AmazonS3Configuration amazonS3Configuration;
  @MockBean S3StorageConfiguration s3StorageConfiguration;
  @MockBean StoreController storeController;

  @Mock URI item;
  @MockBean BlockingQueue<URI> transferQueue;
  @MockBean StatusService statusService;
  @Inject Poller poller;

  @Test
  void testStart() throws InterruptedException {
    doReturn(item).when(transferQueue).take();
    // TODO: Test could fail for timing issues. Currently it does not (on my machine).
    //    Thread.sleep(250);
    poller.start();
    verify(transferQueue, atLeast(1)).take();
    verify(statusService, atLeast(1)).poll(item);
  }

  @Test
  void testStop() throws InterruptedException {
    doReturn(item).when(transferQueue).take();
    poller.start();
    poller.stop(Duration.ZERO);
    clearInvocations(transferQueue, statusService);
    verifyNoInteractions(transferQueue);
    verifyNoInteractions(statusService);
  }

  @Test
  void testStartAfterStop() throws InterruptedException {
    poller.start();
    poller.stop(Duration.ZERO);
    poller.start();
    doReturn(item).when(transferQueue).take();
    // TODO: Test could fail for timing issues. Currently it does not (on my machine).
    Thread.sleep(250);
    verify(transferQueue, atLeast(1)).take();
    verify(statusService, atLeast(1)).poll(item);
  }
}
