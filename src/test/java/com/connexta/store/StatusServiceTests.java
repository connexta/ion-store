/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.connexta.poller.service.StatusService;
import com.connexta.poller.service.StatusServiceImpl;
import java.util.concurrent.Executors;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

class StatusServiceTests {

  private final MockWebServer server = new MockWebServer();

  @Test
  void testPoll() throws InterruptedException {
    StatusService statusService =
        new StatusServiceImpl(1, 1000000, Executors.newFixedThreadPool(1), new RestTemplate());
    HttpUrl url = server.url("/");
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"status\":\"complete\"}"));
    statusService.submit(url.uri());
    RecordedRequest request = server.takeRequest();
    assertThat(request.getRequestUrl(), is(url));
  }
}
