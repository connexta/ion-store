/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import com.connexta.multiintstore.services.api.SearchService;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class MultiIntStoreIntegrationTest extends MultiIntStoreIntegrationTestContainers {

  @Autowired private WebApplicationContext wac;
  @Autowired private IndexedMetadataRepository searchRepository;
  @Autowired private SearchService searchService;

  private MockMvc mockMvc;
  private StubServer server;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    server = new StubServer();
    server.start();
  }

  @After
  public void stop() {
    server.stop();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void handleCSTCallback() throws Exception {

    final String cstContents = "Super cool CST";
    final String ingestId = "1234";

    whenHttp(server)
        .match(Condition.endsWithUri("/location/cst001"))
        .then(
            Action.contentType(MediaType.TEXT_PLAIN.toString()),
            Action.stringContent(cstContents),
            Action.status(HttpStatus.OK_200));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/store/" + ingestId)
                .contentType("application/json")
                .content(
                    TestUtil.createMetadataCallbackJson(
                        "1234",
                        "COMPLETE",
                        "cst",
                        MediaType.APPLICATION_JSON.toString(),
                        256,
                        "http://localhost:" + server.getPort() + "/location/cst001",
                        "U",
                        "ownerProducer")))
        .andExpect(status().isOk());

    assertThat(
        searchRepository.findById(ingestId),
        isPresentAnd(Matchers.hasProperty("contents", is(cstContents))));
  }

  @Test
  public void testRetrieve() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/retrieve/1"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  public void testSearchEndpointKeyword() throws Exception {

    searchService.store(
        new IndexedProductMetadata(
            "1",
            "All the color had been leached from Winterfell until only grey and white remained"));
  }
}
