/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.connexta.multiintstore.config.CallbackAcceptVersion;
import java.util.Objects;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DataRetrieverTest {

  @Autowired private DataRetriever dataRetriever;

  @Autowired private RestTemplate restTemplate;

  @Autowired private CallbackAcceptVersion callbackAcceptVersion;

  @After
  public void afterEach() {
    Mockito.reset(restTemplate);
  }

  @Test
  public void retrieverTest() throws Exception {
    String url = "nice.local";
    MediaType mediaType = MediaType.APPLICATION_JSON;
    JSONObject jsonRetval = new JSONObject().put("Quality", "Test");

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .then(
            invocation -> {
              HttpEntity httpEntity = invocation.getArgument(2, HttpEntity.class);
              assertThat(
                  Objects.requireNonNull(httpEntity.getHeaders().get("Accept-Version")).get(0),
                  is(callbackAcceptVersion.getCallbackAcceptVersion()));
              assertThat(httpEntity.getHeaders().getAccept().get(0), is(mediaType));
              return new ResponseEntity<>(jsonRetval.toString(), HttpStatus.OK);
            });

    assertThat(
        dataRetriever.getMetadata(url, mediaType.toString(), String.class),
        is(jsonRetval.toString()));
  }

  @Test(expected = RetrievalClientException.class)
  public void clientErrorRetrieverTest() throws Exception {
    String url = "nice.local";
    MediaType mediaType = MediaType.APPLICATION_JSON;

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    dataRetriever.getMetadata(url, mediaType.toString(), String.class);
  }

  @Test(expected = RetrievalServerException.class)
  public void serverErrorRetrieverTest() throws Exception {
    String url = "nice.local";
    MediaType mediaType = MediaType.APPLICATION_JSON;

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    dataRetriever.getMetadata(url, mediaType.toString(), String.class);
  }

  @Test(expected = RetrievalServerException.class)
  public void emptyBodyRetrieverTest() throws Exception {
    String url = "nice.local";
    MediaType mediaType = MediaType.APPLICATION_JSON;

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    dataRetriever.getMetadata(url, mediaType.toString(), String.class);
  }
}
