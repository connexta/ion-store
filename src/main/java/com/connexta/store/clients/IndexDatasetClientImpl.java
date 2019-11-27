/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import com.connexta.search.rest.models.IndexRequest;
import com.connexta.store.exceptions.IndexDatasetException;
import java.net.URI;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
public class IndexDatasetClientImpl implements IndexDatasetClient {

  public static final String ACCEPT_VERSION_HEADER_NAME = "Accept-Version";

  @NotNull private final RestTemplate restTemplate;
  @NotBlank private final String indexEndpoint;
  @NotBlank private final String indexApiVersion;

  @Override
  public void indexDataset(
      @Pattern(regexp = "^[0-9a-zA-Z]+$") @Size(min = 32, max = 32) final String datasetId,
      @NotNull final URI irmUri)
      throws IndexDatasetException {
    final HttpHeaders headers = new HttpHeaders();
    headers.set(ACCEPT_VERSION_HEADER_NAME, indexApiVersion);

    try {
      restTemplate.put(
          indexEndpoint + "index/" + datasetId.replace("-", ""),
          new HttpEntity<>(new IndexRequest().irmLocation(irmUri), headers));
    } catch (Exception e) {
      throw new IndexDatasetException(
          String.format("Error indexing datasetId=%s: %s", datasetId, e.getMessage()), e);
    }
  }
}
