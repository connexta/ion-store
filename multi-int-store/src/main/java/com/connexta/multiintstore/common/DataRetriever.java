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
import org.springframework.web.client.RestTemplate;

@Service
public class DataRetriever {

  @NotEmpty private final String callbackAcceptVersion;

  public DataRetriever(@NotNull final CallbackAcceptVersion callbackAcceptVersion) {
    this.callbackAcceptVersion = callbackAcceptVersion.getCallbackAcceptVersion();
  }

  public <T> T getMetadata(String url, String mediaType, Class<T> clazz) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Version", callbackAcceptVersion);
    headers.setAccept(Collections.singletonList(MediaType.valueOf(mediaType)));

    return new RestTemplate()
        .exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), clazz)
        .getBody();
  }
}
