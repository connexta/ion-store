/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

import com.connexta.multiintstore.config.CallbackAcceptVersion;
import java.util.Collections;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

@Service
public class DataRetriever {

  @NotEmpty private final String callbackAcceptVersion;

  @NotEmpty private final RestTemplate restTemplate;

  public DataRetriever(
      @NotEmpty final CallbackAcceptVersion callbackAcceptVersion,
      @NotNull final RestTemplate restTemplate) {
    this.callbackAcceptVersion = callbackAcceptVersion.getCallbackAcceptVersion();
    this.restTemplate = restTemplate;
  }

  /**
   * @throws RetrievalClientException if there is a 400 status code when sending the request
   * @throws RetrievalServerException if there was a 500 status code when sending the request or if
   *     the server returned a request with an empty body
   * @return The metadata in the format of the clazz parameter
   */
  public <T> T getMetadata(String url, String mediaType, Class<T> clazz)
      throws RetrievalServerException, RetrievalClientException {

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Version", callbackAcceptVersion);
    headers.setAccept(Collections.singletonList(MediaType.valueOf(mediaType)));

    try {
      ResponseEntity<T> exchange =
          restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), clazz);

      if (!exchange.hasBody()) {
        throw new RetrievalServerException(
            HttpMethod.GET.name() + ": " + url + " returned an empty body");
      }

      return exchange.getBody();

    } catch (HttpClientErrorException e) {
      throw new RetrievalClientException("Failed to retrieve metadata due to a Client error", e);
    } catch (HttpServerErrorException e) {
      throw new RetrievalServerException("Failed to retrieve metadata due to a Server error", e);
    } catch (UnknownHttpStatusCodeException e) {
      throw new RetrievalServerException(
          "Failed to retrieve metadata due to an illegal HTTP status code", e);
    }
  }
}
