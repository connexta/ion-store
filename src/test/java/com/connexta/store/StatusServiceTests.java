/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.fail;

import com.connexta.poller.service.StatusResponse;
import com.connexta.poller.service.StatusService;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.config.S3StorageConfiguration;
import com.connexta.store.controllers.StoreController;
import com.dyngr.exception.PollerStoppedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class StatusServiceTests {

  // Three mocks to prevent the error "No qualifying bean of type
  // 'com.amazonaws.services.s3.AmazonS3'
  // available: expected at least 1 bean which qualifies as autowire candidate"
  @MockBean AmazonS3Configuration amazonS3Configuration;
  @MockBean S3StorageConfiguration s3StorageConfiguration;
  @MockBean StoreController storeController;

  private final MockWebServer server = new MockWebServer();

  @Inject StatusService statusService;

  @Test
  void testPoll()
      throws URISyntaxException, ExecutionException, InterruptedException {
    HttpUrl url = server.url("/");
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"status\":\"complete\"}"));
    Future<StatusResponse> promisedResponse = statusService.poll(url.uri());
    StatusResponse statusResponse = promisedResponse.get();
    assertThat(statusResponse.getStatus(), is("complete"));
  }

  // TODO Make timeout on StatusService configurable so this test can use a shorter timeout period
  @Test
  void testHostNotAvailable() throws ExecutionException, InterruptedException, URISyntaxException {
    Future<StatusResponse> promisedResponse = statusService.poll(new URI("http://nohost"));
    try { promisedResponse.get();}
    catch (ExecutionException e) {
      assertThat(e.getCause(), isA(PollerStoppedException.class));
      return;
    }
    fail();
  }
}
