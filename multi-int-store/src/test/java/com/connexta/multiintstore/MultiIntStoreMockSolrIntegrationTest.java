/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class MultiIntStoreMockSolrIntegrationTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private SolrClient mockSolrClient;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testSearchSolrCommunicationError() throws Exception {
    // given:
    when(mockSolrClient.query(eq("searchTerms"), any(SolrParams.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(IOException.class);

    // verify:
    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testSearchSolrServerError() throws Exception {
    // given:
    when(mockSolrClient.query(eq("searchTerms"), any(SolrParams.class), eq(SolrRequest.METHOD.GET)))
        .thenThrow(SolrServerException.class);

    // verify:
    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isInternalServerError());
  }
}
