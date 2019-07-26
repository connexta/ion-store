/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.client;

import com.connexta.ingest.common.StreamingRestTemplate;
import com.connexta.ingest.exceptions.StoreException;
import java.io.InputStream;
import java.net.URI;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class StoreClient {

  @NotBlank private final String storeEndpoint;
  @NotNull private RestTemplate restTemplate;

  public StoreClient(
      @NotNull final RestTemplate restTemplate, @NotBlank final String storeEndpoint) {
    this.restTemplate = restTemplate;
    this.storeEndpoint = storeEndpoint;
    log.info("Store URL: {}", storeEndpoint);
  }

  /** @return the location of the product */
  public URI store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mimeType,
      @NotNull final InputStream inputStream,
      @NotBlank final String fileName)
      throws StoreException {
    final MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("fileSize", fileSize);
    builder.part("mimeType", mimeType);
    builder.part(
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
    builder.part("fileName", fileName);

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Version", "0.1.0");

    final HttpEntity<MultiValueMap<String, HttpEntity<?>>> request =
        new HttpEntity<>(builder.build(), headers);
    log.info("Sending POST request to {}: {}", storeEndpoint, request);
    /*
      // Injected RestTemplate has a metrics interceptor which reads the body into memory. Create a new one.
      restTemplate = new RestTemplate();
      SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
      requestFactory.setBufferRequestBody(false);
      restTemplate.setRequestFactory(requestFactory);

      ResponseEntity<URI> responseEntity;
      try {
        responseEntity = restTemplate.exchange(storeEndpoint, HttpMethod.POST, request, URI.class);
      } catch (final RestClientException e) {
        throw new StoreException("Unable to POST to store endpoint", e);
      }
      return responseEntity.getBody();
    }

    */
    return new StreamingRestTemplate<URI>().exchange(storeEndpoint, HttpMethod.POST, request);
  }
}
