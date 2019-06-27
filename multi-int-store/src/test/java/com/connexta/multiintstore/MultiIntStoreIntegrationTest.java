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

import com.connexta.multiintstore.config.CallbackAcceptVersion;
import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = MultiIntStoreIntegrationTest.Initializer.class)
@EnableConfigurationProperties
public class MultiIntStoreIntegrationTest {

  @ClassRule
  public static final GenericContainer solr =
      new GenericContainer("solr:8")
          .withCommand("solr-create -c searchTerms")
          .withExposedPorts(8983)
          .waitingFor(Wait.forHttp("/solr/admin/cores?action=STATUS"));

  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
              "solr.host=" + solr.getContainerIpAddress(),
              "solr.port=" + solr.getMappedPort(8983),
              "endpointUrl.retrieve=" + RETRIEVE_ENDPOINT)
          .applyTo(configurableApplicationContext.getEnvironment());
    }
  }

  private static final String RETRIEVE_ENDPOINT = "http://localhost:9040/retrieve/";

  @Autowired private WebApplicationContext wac;
  @Autowired private IndexedMetadataRepository indexedMetadataRepository;
  @Autowired private CallbackAcceptVersion acceptVersion;

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
                    createMetadataCallbackJson(
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
                createMetadataCallbackJson(
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
                createMetadataCallbackJson(
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

  // TODO: Update the MIS itests when we remove the deprecated endpoints

  /*
   * ================================================================
   * ==================== New MIS endpoint tests ====================
   * ================================================================
   */
  @Test
  public void testGetProduct() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/1"))
        .andDo(print())
        .andExpect(status().isNotImplemented());
  }

  @Test
  public void testPutProduct() throws Exception {

    MockMultipartFile productInfo =
        new MockMultipartFile(
            "productInfo",
            "test.json",
            "application/json",
            "{\"title\": \"Where's Kyle?\"}".getBytes());
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "application/octet-stream", "data".getBytes());

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/mis/product")
            .file(productInfo)
            .file(file)
            .with(
                request -> {
                  request.setMethod(HttpMethod.PUT.toString());
                  return request;
                })
            .header("Accept-Version", acceptVersion.getCallbackAcceptVersion());

    mockMvc
        .perform(builder)
        .andExpect(MockMvcResultMatchers.status().isNotImplemented())
        .andDo(MockMvcResultHandlers.print());
  }

  @Test
  public void testGetMetadata() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/1/cst"))
        .andDo(print())
        .andExpect(status().isNotImplemented());
  }

  @Test
  public void testPutMetadata() throws Exception {
    MockMultipartFile productInfo =
        new MockMultipartFile(
            "productInfo",
            "test.json",
            "application/json",
            "{\"title\": \"Where's Kyle?\"}".getBytes());
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "test.txt",
            "application/octet-stream",
            "{\"title\": \"Where's Kyle?\"}".getBytes());

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/mis/product/1/cst")
            .file(productInfo)
            .file(file)
            .with(
                request -> {
                  request.setMethod(HttpMethod.PUT.toString());
                  return request;
                })
            .header("Accept-Version", acceptVersion.getCallbackAcceptVersion());

    mockMvc
        .perform(builder)
        .andExpect(MockMvcResultMatchers.status().isNotImplemented())
        .andDo(MockMvcResultHandlers.print());
  }

  private static String createMetadataCallbackJson(
      String id,
      String status,
      String type,
      String mimeType,
      int bytes,
      String location,
      String classification,
      String ownerProducer)
      throws Exception {
    return new JSONObject()
        .put("id", id)
        .put("status", status)
        .put("type", type)
        .put("mimeType", mimeType)
        .put("bytes", bytes)
        .put("location", location)
        .put(
            "security",
            new JSONObject()
                .put("classification", classification)
                .put("ownerProducer", ownerProducer))
        .toString();
  }
}
