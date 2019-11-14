/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.poller.service.StatusResponse;
import com.connexta.poller.service.StatusResponseImpl;
import com.connexta.poller.service.StatusService;
import com.connexta.poller.service.StatusServiceImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestTemplate;

class StatusServiceTests {

  private static StatusService statusService;
  private static URI statusUri;

  @BeforeAll
  static void beforeAll() throws URISyntaxException {
    statusUri = new URI("http://host");
  }

  @Test
  void testCompleteStatus() throws InterruptedException, ExecutionException, URISyntaxException {
    RestTemplate restTemplate = mock(RestTemplate.class);
    doReturn(new StatusResponseImpl("complete")).when(restTemplate).getForObject(any(), any());
    statusService = new StatusServiceImpl(1, 100, Executors.newFixedThreadPool(1), restTemplate);
    statusService.submit(statusUri);
    verify(restTemplate).getForObject(eq(statusUri), any());
  }

  @Test
  void testInProgress() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    // TODO: Can't get the compiler to accept any(StatusResponse.class) for second matcher
    when(restTemplate.getForObject(eq(statusUri), any()))
        .thenReturn(new StatusResponseImpl("inprogress"))
        .thenReturn(new StatusResponseImpl("complete"));
    StatusService statusService =
        new StatusServiceImpl(1, 2, Executors.newFixedThreadPool(1), restTemplate);
    statusService.submit(statusUri);
    // TODO: Does this verify really prove anything?
    verify(restTemplate, times(2)).getForObject(eq(statusUri), any());
  }

  // TODO: How to test this case without a Future?
  @Disabled
  @Test
  @Timeout(5)
  void testDiesOnTimeout() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.getForObject(any(), any())).thenReturn(new StatusResponseImpl("inprogress"));
    StatusService statusService =
        new StatusServiceImpl(1, 2, Executors.newFixedThreadPool(1), restTemplate);
    statusService.submit(statusUri);
  }

  // TODO: The thread just dies when the 404 exception is thrown. Can't figure it out.
  @Disabled
  @Test
  void testTemporaryFailure() {
    StatusResponse response = mock(StatusResponse.class);
    doReturn("complete").when(response).getStatus();
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.getForObject(any(), any())).thenThrow(NotFound.class).thenReturn(response);
    StatusService statusService =
        new StatusServiceImpl(1, 2, Executors.newFixedThreadPool(1), restTemplate);
    statusService.submit(statusUri);
    verify(restTemplate, times(2)).getForObject(eq(statusUri), any());
  }
}
