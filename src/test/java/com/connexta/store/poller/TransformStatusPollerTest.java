/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.poller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.util.IOUtils;
import com.google.common.collect.Queues;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;
import org.codice.junit.rules.ClearInterruptions;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class TransformStatusPollerTest {

  private static final String DATASET_ID = "datasetId";
  private static final String TRANSFORM_DONE_RESPONSE =
      "/com/connexta/store/poller/transformDone.json";
  private static final String TRANSFORM_FAILED_RESPONSE =
      "/com/connexta/store/poller/transformFailResponse.json";
  private static final String TRANSFORM_IN_PROGRESS_RESPONSE =
      "/com/connexta/store/poller/transformInProgress.json";
  private static final String EMPTY_RESPONSE = "/com/connexta/store/poller/emptyResponse.json";
  private static final String ADD_METADATA_REQUEST =
      "/com/connexta/store/poller/addMetadataRequest.json";
  private static final String QUARANTINE_REQUEST =
      "/com/connexta/store/poller/quarantineRequest.json";

  private static final URL transformUrl;
  private static final TransformStatusTask task;

  static {
    try {
      transformUrl = new URL("http://transform:9090/" + DATASET_ID);
      task = new TransformStatusTask(DATASET_ID, transformUrl);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException();
    }
  }

  @Rule public ClearInterruptions clearInterruptions = new ClearInterruptions();

  private BlockingQueue<TransformStatusTask> taskQueue = Queues.newLinkedBlockingQueue(1);

  @BeforeEach
  void beforeEach() throws Exception {
    taskQueue.put(task);
  }

  @AfterEach
  void afterEach() {
    taskQueue.clear();
  }

  @Test
  void testInterruptedWhileTaking() {
    // setup
    TransformStatusPoller poller = new TransformStatusPoller(taskQueue, null, null);
    Thread.currentThread().interrupt();

    // when
    poller.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
  }

  private static Stream<Arguments> testAddMetadataNonRetryableResponses() {
    return Stream.of(Arguments.of(HttpStatus.OK), Arguments.of(HttpStatus.NOT_FOUND));
  }

  @ParameterizedTest
  @MethodSource
  void testAddMetadataNonRetryableResponses(HttpStatus httpStatus) throws Exception {
    // setup
    ExchangeFunction transformWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient transformWebClient =
        mockWebClient(TRANSFORM_DONE_RESPONSE, HttpStatus.OK, transformWebClientExchangeFunc);

    ExchangeFunction storeWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient storeWebClient =
        mockWebClient(EMPTY_RESPONSE, httpStatus, storeWebClientExchangeFunc);

    TransformStatusPoller poller =
        new TransformStatusPoller(taskQueue, transformWebClient, storeWebClient);

    // when
    poller.doRun();

    // then
    ArgumentCaptor<ClientRequest> transformRequestsCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);
    ArgumentCaptor<ClientRequest> addMetadataRequestCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);

    verify(transformWebClientExchangeFunc, times(2)).exchange(transformRequestsCaptor.capture());
    verify(storeWebClientExchangeFunc).exchange(addMetadataRequestCaptor.capture());

    ClientRequest transformPollRequest = transformRequestsCaptor.getAllValues().get(0);
    assertThat(transformPollRequest.url().toURL(), is(transformUrl));
    assertThat(transformPollRequest.method(), is(HttpMethod.GET));

    ClientRequest transformDeleteRequest = transformRequestsCaptor.getAllValues().get(1);
    assertThat(transformDeleteRequest.url().toASCIIString(), is("/transform/" + DATASET_ID));
    assertThat(transformDeleteRequest.method(), is(HttpMethod.DELETE));

    ClientRequest addMetadataRequest = addMetadataRequestCaptor.getValue();
    assertThat(addMetadataRequest.url().toASCIIString(), is("/dataset/" + DATASET_ID));
    assertThat(
        getRequestBodyAsString(addMetadataRequest), is(readResourceAsString(ADD_METADATA_REQUEST)));

    assertThat(taskQueue.size(), is(0));
  }

  private static Stream<Arguments> testAddMetadataRetryableResponses() {
    return Stream.of(
        Arguments.of(HttpStatus.UNAUTHORIZED),
        Arguments.of(HttpStatus.FORBIDDEN),
        Arguments.of(HttpStatus.NOT_IMPLEMENTED),
        Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @ParameterizedTest
  @MethodSource
  void testAddMetadataRetryableResponses(HttpStatus httpStatus) throws Exception {
    // setup
    ExchangeFunction transformWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient transformWebClient =
        mockWebClient(TRANSFORM_DONE_RESPONSE, HttpStatus.OK, transformWebClientExchangeFunc);

    ExchangeFunction storeWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient storeWebClient =
        mockWebClient(EMPTY_RESPONSE, httpStatus, storeWebClientExchangeFunc);

    TransformStatusPoller poller =
        new TransformStatusPoller(taskQueue, transformWebClient, storeWebClient);

    // when
    poller.doRun();

    // then
    ArgumentCaptor<ClientRequest> transformRequestsCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);
    ArgumentCaptor<ClientRequest> addMetadataRequestCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);

    verify(transformWebClientExchangeFunc).exchange(transformRequestsCaptor.capture());
    verify(storeWebClientExchangeFunc).exchange(addMetadataRequestCaptor.capture());

    ClientRequest transformPollRequest = transformRequestsCaptor.getValue();
    assertThat(transformPollRequest.url().toURL(), is(transformUrl));
    assertThat(transformPollRequest.method(), is(HttpMethod.GET));

    ClientRequest addMetadataRequest = addMetadataRequestCaptor.getValue();
    assertThat(addMetadataRequest.url().toASCIIString(), is("/dataset/" + DATASET_ID));
    assertThat(
        getRequestBodyAsString(addMetadataRequest), is(readResourceAsString(ADD_METADATA_REQUEST)));

    assertThat(taskQueue.size(), is(1));
  }

  @Test
  void testAddMetadataBadRequest() throws Exception {
    // setup
    ExchangeFunction transformWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient transformWebClient =
        mockWebClient(TRANSFORM_DONE_RESPONSE, HttpStatus.OK, transformWebClientExchangeFunc);

    ExchangeFunction storeWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient storeWebClient =
        mockWebClient(EMPTY_RESPONSE, HttpStatus.BAD_REQUEST, storeWebClientExchangeFunc);

    TransformStatusPoller poller =
        new TransformStatusPoller(taskQueue, transformWebClient, storeWebClient);

    // when
    poller.doRun();

    // then
    ArgumentCaptor<ClientRequest> transformRequestsCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);
    ArgumentCaptor<ClientRequest> addMetadataRequestCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);

    verify(transformWebClientExchangeFunc).exchange(transformRequestsCaptor.capture());
    verify(storeWebClientExchangeFunc).exchange(addMetadataRequestCaptor.capture());

    ClientRequest transformPollRequest = transformRequestsCaptor.getValue();
    assertThat(transformPollRequest.url().toURL(), is(transformUrl));
    assertThat(transformPollRequest.method(), is(HttpMethod.GET));

    ClientRequest addMetadataRequest = addMetadataRequestCaptor.getValue();
    assertThat(addMetadataRequest.url().toASCIIString(), is("/dataset/" + DATASET_ID));
    assertThat(
        getRequestBodyAsString(addMetadataRequest), is(readResourceAsString(ADD_METADATA_REQUEST)));

    assertThat(taskQueue.size(), is(0));
  }

  private static Stream<Arguments> testQuarantineNonRetryableResponses() {
    return Stream.of(Arguments.of(HttpStatus.OK), Arguments.of(HttpStatus.NOT_FOUND));
  }

  @ParameterizedTest
  @MethodSource
  void testQuarantineNonRetryableResponses(HttpStatus httpStatus) throws Exception {
    // setup
    ExchangeFunction transformWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient transformWebClient =
        mockWebClient(TRANSFORM_FAILED_RESPONSE, HttpStatus.OK, transformWebClientExchangeFunc);

    ExchangeFunction storeWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient storeWebClient =
        mockWebClient(EMPTY_RESPONSE, httpStatus, storeWebClientExchangeFunc);

    TransformStatusPoller poller =
        new TransformStatusPoller(taskQueue, transformWebClient, storeWebClient);

    // when
    poller.doRun();

    // then
    ArgumentCaptor<ClientRequest> transformRequestsCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);
    ArgumentCaptor<ClientRequest> quarantineRequestCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);

    verify(transformWebClientExchangeFunc, times(2)).exchange(transformRequestsCaptor.capture());
    verify(storeWebClientExchangeFunc).exchange(quarantineRequestCaptor.capture());

    ClientRequest transformPollRequest = transformRequestsCaptor.getAllValues().get(0);
    assertThat(transformPollRequest.url().toURL(), is(transformUrl));
    assertThat(transformPollRequest.method(), is(HttpMethod.GET));

    ClientRequest transformDeleteRequest = transformRequestsCaptor.getAllValues().get(1);
    assertThat(transformDeleteRequest.url().toASCIIString(), is("/transform/" + DATASET_ID));
    assertThat(transformDeleteRequest.method(), is(HttpMethod.DELETE));

    ClientRequest quarantineRequest = quarantineRequestCaptor.getValue();
    assertThat(
        quarantineRequest.url().toASCIIString(), is("/dataset/" + DATASET_ID + "/quarantine"));
    assertThat(
        getRequestBodyAsString(quarantineRequest), is(readResourceAsString(QUARANTINE_REQUEST)));

    assertThat(taskQueue.size(), is(0));
  }

  @Test
  void testQuarantineBadRequest() throws Exception {
    // setup
    ExchangeFunction transformWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient transformWebClient =
        mockWebClient(TRANSFORM_FAILED_RESPONSE, HttpStatus.OK, transformWebClientExchangeFunc);

    ExchangeFunction storeWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient storeWebClient =
        mockWebClient(EMPTY_RESPONSE, HttpStatus.BAD_REQUEST, storeWebClientExchangeFunc);

    TransformStatusPoller poller =
        new TransformStatusPoller(taskQueue, transformWebClient, storeWebClient);

    // when
    poller.doRun();

    // then
    ArgumentCaptor<ClientRequest> transformRequestsCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);
    ArgumentCaptor<ClientRequest> quarantineRequestCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);

    verify(transformWebClientExchangeFunc).exchange(transformRequestsCaptor.capture());
    verify(storeWebClientExchangeFunc).exchange(quarantineRequestCaptor.capture());

    ClientRequest transformPollRequest = transformRequestsCaptor.getValue();
    assertThat(transformPollRequest.url().toURL(), is(transformUrl));
    assertThat(transformPollRequest.method(), is(HttpMethod.GET));

    ClientRequest quarantineRequest = quarantineRequestCaptor.getValue();
    assertThat(
        quarantineRequest.url().toASCIIString(), is("/dataset/" + DATASET_ID + "/quarantine"));
    assertThat(
        getRequestBodyAsString(quarantineRequest), is(readResourceAsString(QUARANTINE_REQUEST)));

    assertThat(taskQueue.size(), is(0));
  }

  private static Stream<Arguments> testQuarantineRetryableResponses() {
    return Stream.of(
        Arguments.of(HttpStatus.UNAUTHORIZED),
        Arguments.of(HttpStatus.FORBIDDEN),
        Arguments.of(HttpStatus.NOT_IMPLEMENTED),
        Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @ParameterizedTest
  @MethodSource
  void testQuarantineRetryableResponses(HttpStatus httpStatus) throws Exception {
    // setup
    ExchangeFunction transformWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient transformWebClient =
        mockWebClient(TRANSFORM_FAILED_RESPONSE, HttpStatus.OK, transformWebClientExchangeFunc);

    ExchangeFunction storeWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient storeWebClient =
        mockWebClient(EMPTY_RESPONSE, httpStatus, storeWebClientExchangeFunc);

    TransformStatusPoller poller =
        new TransformStatusPoller(taskQueue, transformWebClient, storeWebClient);

    // when
    poller.doRun();

    // then
    ArgumentCaptor<ClientRequest> transformRequestsCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);
    ArgumentCaptor<ClientRequest> quarantineRequestCaptor =
        ArgumentCaptor.forClass(ClientRequest.class);

    verify(transformWebClientExchangeFunc).exchange(transformRequestsCaptor.capture());
    verify(storeWebClientExchangeFunc).exchange(quarantineRequestCaptor.capture());

    ClientRequest transformPollRequest = transformRequestsCaptor.getValue();
    assertThat(transformPollRequest.url().toURL(), is(transformUrl));
    assertThat(transformPollRequest.method(), is(HttpMethod.GET));

    ClientRequest quarantineRequest = quarantineRequestCaptor.getValue();
    assertThat(
        quarantineRequest.url().toASCIIString(), is("/dataset/" + DATASET_ID + "/quarantine"));
    assertThat(
        getRequestBodyAsString(quarantineRequest), is(readResourceAsString(QUARANTINE_REQUEST)));

    assertThat(taskQueue.size(), is(1));
  }

  @Test
  void testTransformInProgressPutsBackInQueue() throws Exception {
    // setup
    ExchangeFunction transformWebClientExchangeFunc = mock(ExchangeFunction.class);
    WebClient transformWebClient =
        mockWebClient(
            TRANSFORM_IN_PROGRESS_RESPONSE, HttpStatus.OK, transformWebClientExchangeFunc);

    TransformStatusPoller poller =
        new TransformStatusPoller(taskQueue, transformWebClient, mock(WebClient.class));

    // when
    poller.doRun();

    // then
    assertThat(taskQueue.size(), is(1));
  }

  private String getRequestBodyAsString(ClientRequest clientRequest) {
    MockClientHttpRequest mockClientHttpRequest =
        new MockClientHttpRequest(HttpMethod.PUT, "doesntMatter");
    clientRequest.writeTo(mockClientHttpRequest, ExchangeStrategies.withDefaults()).block();
    return mockClientHttpRequest.getBodyAsString().block();
  }

  private WebClient mockWebClient(
      String responseFilePath, HttpStatus httpStatus, ExchangeFunction exchangeFunction) {
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(
            Mono.just(
                ClientResponse.create(httpStatus)
                    .header("content-type", "application/json")
                    .body(readResourceAsString(responseFilePath))
                    .build()));

    return WebClient.builder().exchangeFunction(exchangeFunction).build();
  }

  private String readResourceAsString(String resourcePath) {
    try {
      return IOUtils.toString(this.getClass().getResourceAsStream(resourcePath));
    } catch (IOException e) {
      fail("failed to retrieve test resource " + resourcePath);
      throw new RuntimeException();
    }
  }
}
