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

import com.connexta.multiintstore.storage.persistence.repository.CommonSearchTermsRepository;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class MultiIntStoreIntegrationTest extends MultiIntStoreIntegrationTestContainers {

  @Autowired private WebApplicationContext wac;
  @Autowired private CommonSearchTermsRepository cstRepository;

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

  // TODO: Does not pass because it did not talk to Solr during integration test.
  //  "IOException occurred when talking to server at: http://localhost:32773/solr"
  @Ignore
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

    // TODO
    Thread.sleep(5000);

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
        cstRepository.findById(ingestId),
        isPresentAnd(Matchers.hasProperty("contents", is(cstContents))));
  }

  @Test
  public void testRetrieve() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/retrieve/1"))
        .andDo(print())
        .andExpect(status().isOk());
  }
}
