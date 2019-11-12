/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.connexta.poller.service.StatusResponse;
import com.connexta.poller.service.StatusService;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.config.S3StorageConfiguration;
import com.connexta.store.controllers.StoreController;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
// @ExtendWith(MockitoExtension.class)
class StatusServiceTests {

  // Three mocks to prevent the error "No qualifying bean of type
  // 'com.amazonaws.services.s3.AmazonS3'
  // available: expected at least 1 bean which qualifies as autowire candidate"
  @MockBean AmazonS3Configuration amazonS3Configuration;
  @MockBean S3StorageConfiguration s3StorageConfiguration;
  @MockBean StoreController storeController;

  private MockRestServiceServer server;
  @Inject StatusService statusService;
  @Inject RestTemplate restTemplate;

  @BeforeEach
  void beforeEach() {
    server = MockRestServiceServer.bindTo(restTemplate).build();
  }

  @Test
  void testPoll()
      throws URISyntaxException, ExecutionException, InterruptedException, JsonProcessingException {
    server
        .expect(ExpectedCount.min(1), requestTo("http://status"))
        .andRespond(withSuccess("{\"status\":\"complete\"}", MediaType.APPLICATION_JSON));
    Future<StatusResponse> promisedResponse = statusService.poll(new URI("http://status"));
    assertThat(promisedResponse.get().getStatus(), org.hamcrest.CoreMatchers.is("complete"));
  }
}
