/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import com.connexta.store.controllers.StoreController;
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
public class IndexDatasetClientImpl implements IndexDatasetClient {

  public static final String ACCEPT_VERSION_HEADER_NAME = "Accept-Version";

  @NotNull private final RestTemplate restTemplate;
  @NotBlank private final String indexEndpoint;
  @NotBlank private final String indexApiVersion;

  @Override
  public void indexDataset(
      @NotNull final InputStream irmInputStream,
      @NotNull @Min(1L) @Max(10737418240L) final long fileSize,
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId)
      throws IndexMetadataException {
    // TODO Use IndexApi classes to create request
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new InputStreamResource(irmInputStream) {

          @Override
          public long contentLength() {
            return fileSize;
          }

          @Override
          public String getFilename() {
            return StoreController.SUPPORTED_METADATA_TYPE + ".xml";
          }
        });
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(ACCEPT_VERSION_HEADER_NAME, indexApiVersion);

    try {
      restTemplate.put(indexEndpoint + datasetId, new HttpEntity<>(body, httpHeaders));
    } catch (Exception e) {
      throw new IndexMetadataException(
          String.format("Error indexing datasetId=%s: %s", datasetId, e.getMessage()), e);
    }
  }
}
