/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class WebClientTestUtils {

  private WebClientTestUtils() {}

  /**
   * Works by passing in a mock {@link ExchangeFunction}, which is used to capture the {@link ClientRequest}
   * that is sent through the {@link WebClient}. For example,
   *
   * <pre>
   * {@code
   * ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
   * WebClient webClient = WebClientTestUtils
   *         .mockEmptyResponseWebClient(HttpStatus.OK, exchangeFunction);
   *
   * ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
   * verify(exchangeFunction).exchange(indexRequestCaptor.capture());
   *
   * ClientRequest clientRequest = requestCaptor.getValue();
   * // assert clientRequest
   * </pre>
   *
   * @param httpStatus the http status code to return in the response
   * @param exchangeFunction the mocked exchange function
   * @return the mocked WebClient
   */
  public static WebClient mockEmptyResponseWebClient(
      HttpStatus httpStatus, ExchangeFunction exchangeFunction) {
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(
            Mono.just(
                ClientResponse.create(httpStatus)
                    .header("content-type", "application/json")
                    .body("")
                    .build()));

    return WebClient.builder().exchangeFunction(exchangeFunction).build();
  }

  /**
   * Extracts the body from the {@link ClientRequest} as string.
   *
   * @param clientRequest request to extract body from
   * @return the request body as a string
   */
  public static String getRequestBodyAsString(ClientRequest clientRequest) {
    MockClientHttpRequest mockClientHttpRequest =
        new MockClientHttpRequest(HttpMethod.PUT, "doesntMatter");
    clientRequest.writeTo(mockClientHttpRequest, ExchangeStrategies.withDefaults()).block();
    return mockClientHttpRequest.getBodyAsString().block();
  }
}
