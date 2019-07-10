/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.client;

import com.connexta.ingest.exceptions.StoreException;
import java.net.URI;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class StoreClient {

  @NotNull private final RestTemplate restTemplate;

  @NotEmpty private final String storeEndpoint;

  public StoreClient(
      @NotNull final RestTemplate restTemplate,
      @Qualifier("storeEndpoint") @NotEmpty final String storeEndpoint) {
    this.restTemplate = restTemplate;
    this.storeEndpoint = storeEndpoint;
    log.info("Store URL: {}", storeEndpoint);
  }

  /** @return the location of the product */
  public URI store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotEmpty final String mimeType,
      @NotNull final MultipartFile file,
      @NotEmpty final String fileName)
      throws StoreException {
    final MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("fileSize", fileSize);
    builder.part("mimeType", mimeType);
    builder.part("file", file.getResource());
    builder.part("fileName", fileName);

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Version", "0.1.0");

    final HttpEntity<MultiValueMap<String, HttpEntity<?>>> request =
        new HttpEntity<>(builder.build(), headers);
    log.info("Sending POST request to {}: {}", storeEndpoint, request);

    final URI location;
    try {
      location = restTemplate.postForLocation(storeEndpoint, request);
    } catch (final RestClientException e) {
      throw new StoreException("Unable to POST to store endpoint", e);
    }
    return location;
  }
}
