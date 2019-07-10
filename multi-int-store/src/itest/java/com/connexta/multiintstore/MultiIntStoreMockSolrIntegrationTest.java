/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MultiIntStoreMockSolrIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private SolrClient mockSolrClient;

  @After
  public void after() {
    Mockito.reset(mockSolrClient);
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testSearchSolrCommunicationError() throws Exception {
    when(mockSolrClient.query(eq("searchTerms"), any(SolrParams.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(IOException.class);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testSearchSolrServerError() throws Exception {
    when(mockSolrClient.query(eq("searchTerms"), any(SolrParams.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(SolrServerException.class);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testSearchSolrRuntimeException() throws Exception {
    when(mockSolrClient.query(eq("searchTerms"), any(SolrParams.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(RuntimeException.class);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testSearchSolrEmptyKeyword() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/search?q=")).andExpect(status().isBadRequest());
    verifyZeroInteractions(mockSolrClient);
  }

  /** @see MultiIntStoreIntegrationTest#testEmptySearchService() */
  @Test
  public void testSearchWhenSolrIsEmpty() throws Exception {
    final QueryResponse mockQueryResponse = mock(QueryResponse.class);
    final SolrDocumentList mockSolrDocumentList = new SolrDocumentList();
    when(mockQueryResponse.getResults()).thenReturn(mockSolrDocumentList);

    when(mockSolrClient.query(eq("searchTerms"), any(SolrParams.class), eq(SolrRequest.METHOD.GET)))
        .thenReturn(mockQueryResponse);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isOk())
        .andExpect(content().string("[]"));
  }
}
