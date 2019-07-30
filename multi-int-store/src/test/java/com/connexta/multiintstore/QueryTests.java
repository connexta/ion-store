/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import java.io.IOException;
import javax.inject.Inject;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.SolrParams;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * This class contains tests for the query endpoint that use a mocked {@link AmazonS3} and {@link
 * SolrClient}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@MockBean(AmazonS3.class)
public class QueryTests {

  @MockBean private SolrClient mockSolrClient;

  @Inject private MockMvc mockMvc;

  @After
  public void after() {
    verifyNoMoreInteractions(ignoreStubs(mockSolrClient));
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testBadRequest() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/search")).andExpect(status().isBadRequest());
  }

  @Test
  public void testEmptyKeyword() throws Exception {
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "");
    mockMvc
        .perform(MockMvcRequestBuilders.get(queryUriBuilder.build()))
        .andExpect(status().isBadRequest());
  }

  /** @see SolrClient#query(SolrParams, METHOD) */
  @Test
  public void testSolrCommunicationError() throws Exception {
    when(mockSolrClient.query(eq("searchTerms"), any(SolrQuery.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(IOException.class);

    final URIBuilder uriBuilder = new URIBuilder();
    uriBuilder.setPath("/search");
    uriBuilder.setParameter("q", "queryKeyword");
    mockMvc
        .perform(MockMvcRequestBuilders.get(uriBuilder.build()))
        .andExpect(status().isInternalServerError());
  }

  /** @see SolrClient#query(SolrParams, METHOD) */
  @Test
  public void testSolrServerError() throws Exception {
    when(mockSolrClient.query(eq("searchTerms"), any(SolrQuery.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(SolrServerException.class);

    final URIBuilder uriBuilder = new URIBuilder();
    uriBuilder.setPath("/search");
    uriBuilder.setParameter("q", "queryKeyword");
    mockMvc
        .perform(MockMvcRequestBuilders.get(uriBuilder.build()))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testSolrThrowsRuntimeException() throws Exception {
    when(mockSolrClient.query(eq("searchTerms"), any(SolrQuery.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(RuntimeException.class);

    final URIBuilder uriBuilder = new URIBuilder();
    uriBuilder.setPath("/search");
    uriBuilder.setParameter("q", "queryKeyword");
    mockMvc
        .perform(MockMvcRequestBuilders.get(uriBuilder.build()))
        .andExpect(status().isInternalServerError());
  }
}
