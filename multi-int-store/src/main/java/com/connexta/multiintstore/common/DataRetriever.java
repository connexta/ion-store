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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

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
   * @throws ClientException if there is a 400 status code when sending the request
   * @throws ServerException if there was a 500 status code when sending the request
   * @return The metadata in the format of the clazz parameter
   */
  public <T> T getMetadata(String url, String mediaType, Class<T> clazz) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Version", callbackAcceptVersion);
    headers.setAccept(Collections.singletonList(MediaType.valueOf(mediaType)));

    try {
      return restTemplate
          .exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), clazz)
          .getBody();
    } catch (HttpClientErrorException e) {
      throw new ClientException("Failed to retrieve metadata due to a Client error", e);
    } catch (HttpServerErrorException e) {
      throw new ServerException("Failed to retrieve metadata due to a Server error", e);
    }
  }
}
