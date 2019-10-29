/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.connexta.search.rest.models.IndexRequest;
import com.connexta.store.exceptions.IndexDatasetException;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class IndexDatasetClientImplTest {

  private static final String INDEX_ENDPOINT = "http://search:8080/index/";
  private static final String INDEX_API_VERSION = "testIndexApiVersion";

  @Mock private RestTemplate mockRestTemplate;

  private IndexDatasetClient indexDatasetClient;

  @BeforeEach
  void beforeEach() {
    indexDatasetClient =
        new IndexDatasetClientImpl(mockRestTemplate, INDEX_ENDPOINT, INDEX_API_VERSION);
  }

  @AfterEach
  void afterEach() {
    verifyNoMoreInteractions(mockRestTemplate);
  }

  @Test
  void testRestTemplateException() throws Exception {
    // given
    final String datasetId = "datasetId";
    final URI irmUri = new URI(String.format("http://transform:9090/%s/irm", datasetId));

    final RuntimeException runtimeException = new RuntimeException();
    doThrow(runtimeException)
        .when(mockRestTemplate)
        .put(
            eq(INDEX_ENDPOINT + datasetId),
            argThat(
                (ArgumentMatcher<HttpEntity>)
                    argument -> {
                      final String first =
                          argument
                              .getHeaders()
                              .getFirst(IndexDatasetClientImpl.ACCEPT_VERSION_HEADER_NAME);
                      return (new IndexRequest().irmLocation(irmUri)).equals(argument.getBody())
                          && StringUtils.equals(first, INDEX_API_VERSION);
                    }));

    // verify
    final IndexDatasetException thrown =
        assertThrows(
            IndexDatasetException.class, () -> indexDatasetClient.indexDataset(datasetId, irmUri));
    assertThat(thrown.getCause(), is(runtimeException));
  }

  @Test
  void testIndexDataset() throws Exception {
    // given
    final String datasetId = "datasetId";
    final URI irmUri = new URI(String.format("http://transform:9090/%s/irm", datasetId));

    // when
    indexDatasetClient.indexDataset(datasetId, irmUri);

    // then
    verify(mockRestTemplate)
        .put(
            eq(INDEX_ENDPOINT + datasetId),
            argThat(
                (ArgumentMatcher<HttpEntity>)
                    argument -> {
                      final String first =
                          argument
                              .getHeaders()
                              .getFirst(IndexDatasetClientImpl.ACCEPT_VERSION_HEADER_NAME);
                      return (new IndexRequest().irmLocation(irmUri)).equals(argument.getBody())
                          && StringUtils.equals(first, INDEX_API_VERSION);
                    }));
  }
}
