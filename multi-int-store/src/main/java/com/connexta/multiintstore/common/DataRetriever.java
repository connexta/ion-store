/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

import com.connexta.multiintstore.config.ApplicationConfiguration;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DataRetriever {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataRetriever.class);

  private final ApplicationConfiguration config;

  @Autowired
  public DataRetriever(ApplicationConfiguration config) {
    this.config = config;
  }

  public <T> T getMetadata(String url, String mediaType, Class<T> clazz) {

    ArrayList<MediaType> mediaTypes = new ArrayList<>();
    mediaTypes.add(MediaType.valueOf(mediaType));

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Version", config.getIonVersion());
    headers.setAccept(mediaTypes);

    LOGGER.info("Ion version = {}", config.getIonVersion());

    HttpEntity<String> requestEntity = new HttpEntity<>(headers);

    RestTemplate request = new RestTemplate();

    ResponseEntity<T> response = request.exchange(url, HttpMethod.GET, requestEntity, clazz);

    return response.getBody();
  }
}
