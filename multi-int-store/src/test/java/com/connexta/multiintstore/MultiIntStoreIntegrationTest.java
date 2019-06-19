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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
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
  @Autowired private IndexedMetadataRepository indexedMetadataRepository;

  private MockMvc mockMvc;
  private StubServer server;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    server = new StubServer();
    server.start();

    indexedMetadataRepository.deleteAll();
  }

  @After
  public void stop() {
    server.stop();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void handleCSTCallback() throws Exception {

    final String contents = "Super cool CST";
    final String ingestId = "1234";

    whenHttp(server)
        .match(Condition.endsWithUri("/location/cst001"))
        .then(
            Action.contentType(MediaType.TEXT_PLAIN.toString()),
            Action.stringContent(contents),
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
        indexedMetadataRepository.findById(ingestId),
        isPresentAnd(Matchers.hasProperty("contents", is(contents))));
  }

  @Test
  public void testRetrieve() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/retrieve/1"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  public void testEmptySearchService() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isOk())
        .andExpect(content().string("[]"));
  }

  @Test
  public void testEmptySearchResults() throws Exception {
    // given:
    final String contents =
        "All the color had been leached from Winterfell until only grey and white remained";
    final String ingestId = "1234";

    whenHttp(server)
        .match(Condition.endsWithUri("/location/cst001"))
        .then(
            Action.contentType(MediaType.TEXT_PLAIN.toString()),
            Action.stringContent(contents),
            Action.status(HttpStatus.OK_200));

    mockMvc.perform(
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
                    "ownerProducer")));

    // verify:
    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=thisKeywordDoesntMatchAnything"))
        .andExpect(status().isOk())
        .andExpect(content().string("[]"));
  }

  @Test
  public void testSearchResults() throws Exception {
    // given:
    final String contents =
        "All the color had been leached from Winterfell until only grey and white remained";
    final String ingestId = "1234";

    whenHttp(server)
        .match(Condition.endsWithUri("/location/cst001"))
        .then(
            Action.contentType(MediaType.TEXT_PLAIN.toString()),
            Action.stringContent(contents),
            Action.status(HttpStatus.OK_200));

    mockMvc.perform(
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
                    "ownerProducer")));

    // verify:
    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=Winterfell"))
        .andExpect(status().isOk())
        .andExpect(content().string(String.format("[\"%s%s\"]", RETRIEVE_ENDPOINT, ingestId)));
  }
}
