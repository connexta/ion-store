/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import com.connexta.store.exceptions.StoreException;
import java.io.InputStream;
import java.net.URI;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
public class StoreClient {

  @NotNull private final RestTemplate restTemplate;
  @NotBlank private final String storeEndpoint;

  /** @return the non-null location of the product */
  @NotNull
  public URI store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mimeType,
      @NotNull final InputStream inputStream,
      @NotBlank final String fileName)
      throws StoreException {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new InputStreamResource(inputStream) {

          @Override
          public long contentLength() {
            return fileSize;
          }

          @Override
          public String getFilename() {
            return fileName;
          }
        });

    final HttpHeaders headers = new HttpHeaders();
    // TODO inject this like we do for the transformApiVersion in TransformClient
    headers.set("Accept-Version", "0.2.0");

    final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
    log.info("Sending POST request to {}: {}", storeEndpoint, request);

    final URI location;
    try {
      location = restTemplate.postForLocation(storeEndpoint, request);
    } catch (Exception e) {
      throw new StoreException("Store exception", e);
    }
    return location;
  }
}
