/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import com.connexta.store.exceptions.IndexMetadataException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
public class IndexClient {

  @NotNull private final RestTemplate restTemplate;
  @NotBlank private final String indexEndpoint;
  @NotBlank private final String indexApiVersion;

  public void index(
      @NotNull final InputStream cstInputStream,
      @NotNull @Min(1L) @Max(10737418240L) final long fileSize,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String productId)
      throws IndexMetadataException {
    // TODO Use IndexApi classes to create request
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new InputStreamResource(cstInputStream) {

          @Override
          public long contentLength() {
            return fileSize;
          }

          @Override
          public String getFilename() {
            return "cst.json";
          }
        });
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Accept-Version", indexApiVersion);

    try {
      restTemplate.put(indexEndpoint + productId, new HttpEntity<>(body, httpHeaders));
    } catch (Exception e) {
      throw new IndexMetadataException(
          String.format("Error indexing product with id %s", productId), e);
    }
  }
}
