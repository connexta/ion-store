/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.connexta.store.WebClientTestUtils;
import com.connexta.store.exceptions.common.DetailedResponseStatusException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class IndexClientImplTest {

  private static final UUID DATASET_ID = UUID.randomUUID();
  private static final URL FILE_URL;
  private static final URL IRM_URL;
  private static final URL METACARD_URL;

  static {
    try {
      FILE_URL = new URL("http://localhost:9090/file");
      IRM_URL = new URL("http://localhost:9090/irm");
      METACARD_URL = new URL("http://localhost:9090/metacard");
    } catch (MalformedURLException e) {
      throw new RuntimeException();
    }
  }

  @Test
  void testIndexDataset() throws Exception {
    // setup
    ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
    WebClient webClient =
        WebClientTestUtils.mockEmptyResponseWebClient(HttpStatus.OK, exchangeFunction);

    IndexClient indexClient = new IndexClientImpl(webClient);

    // when
    indexClient.indexDataset(DATASET_ID, FILE_URL, IRM_URL, METACARD_URL);

    // then
    ArgumentCaptor<ClientRequest> indexRequestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
    verify(exchangeFunction).exchange(indexRequestCaptor.capture());

    ClientRequest indexRequest = indexRequestCaptor.getValue();
    assertThat(indexRequest.method(), is(HttpMethod.PUT));
    assertThat(indexRequest.url().toASCIIString(), is("/index/" + DATASET_ID));

    JSONObject jsonObject = new JSONObject(WebClientTestUtils.getRequestBodyAsString(indexRequest));
    assertThat(jsonObject.get("irmLocation"), is(IRM_URL.toString()));
    assertThat(jsonObject.get("metacardLocation"), is(METACARD_URL.toString()));
    assertThat(jsonObject.get("fileLocation"), is(FILE_URL.toString()));
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 401, 403, 500, 501})
  void testNonSuccessHttpResponse(int statusCode) {
    // setup
    ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
    WebClient webClient =
        WebClientTestUtils.mockEmptyResponseWebClient(
            HttpStatus.valueOf(statusCode), exchangeFunction);

    IndexClient indexClient = new IndexClientImpl(webClient);

    // expect
    DetailedResponseStatusException exception =
        assertThrows(
            DetailedResponseStatusException.class,
            () -> indexClient.indexDataset(DATASET_ID, FILE_URL, IRM_URL, METACARD_URL));

    assertThat(exception.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR));
  }
}
