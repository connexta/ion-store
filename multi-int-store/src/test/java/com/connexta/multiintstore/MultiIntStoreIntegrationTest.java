/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = MultiIntStoreIntegrationTest.Initializer.class)
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ActiveProfiles({"test", "solrProduction"})
public class MultiIntStoreIntegrationTest {

  private static final String TEST_CONTENTS =
      "All the color had been leached from Winterfell until only grey and white remained";
  private static final byte[] TEST_FILE = TEST_CONTENTS.getBytes();
  private static final String TEST_FILE_SIZE = String.valueOf(TEST_FILE.length);
  private static MockHttpServletRequestBuilder POST_PRODUCT_REQUEST =
      multipart("/mis/product")
          .file("file", TEST_FILE)
          .param("fileSize", TEST_FILE_SIZE)
          .param("fileName", "file")
          .param("mimeType", "plain/text")
          .header("Accept-Version", "1.2.1")
          .accept(MediaType.APPLICATION_JSON)
          .contentType(MediaType.MULTIPART_FORM_DATA);
  private static final String TEST_METADATA_PRODUCT_ID = "12";
  private static MockHttpServletRequestBuilder PUT_METADATA_REQUEST =
      multipart(String.format("/mis/product/%s/cst", TEST_METADATA_PRODUCT_ID))
          .file("file", TEST_FILE)
          .param("fileSize", TEST_FILE_SIZE)
          .param("fileName", "file")
          .param("mimeType", "plain/text")
          .header("Accept-Version", "1.2.1")
          .with(
              request -> {
                request.setMethod(HttpMethod.PUT.toString());
                return request;
              })
          .accept(MediaType.APPLICATION_JSON)
          .contentType(MediaType.MULTIPART_FORM_DATA);

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
              "solr.host=" + solr.getContainerIpAddress(), "solr.port=" + solr.getMappedPort(8983))
          .applyTo(configurableApplicationContext.getEnvironment());
    }
  }

  @Value("${endpointUrl.retrieve}")
  private String endpointUrlRetrieve = "http://localhost:9040/retrieve/";

  @Autowired private MockMvc mockMvc;
  @Autowired private S3Client mockS3Client;
  @Autowired private IndexedMetadataRepository indexedMetadataRepository;
  @Autowired private RestTemplate restTemplate;

  private MockRestServiceServer server;

  @Before
  public void setup() {
    server = MockRestServiceServer.createServer(restTemplate);

    indexedMetadataRepository.deleteAll();
  }

  @After
  public void stop() {
    server.verify();
    server.reset();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testEmptySearchService() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=searchKeyword"))
        .andExpect(status().isOk())
        .andExpect(content().string("[]"));
  }

  @Test
  public void testEmptySearchResults() throws Exception {
    mockMvc.perform(PUT_METADATA_REQUEST);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=thisKeywordDoesntMatchAnything"))
        .andExpect(status().isOk())
        .andExpect(content().string("[]"));
  }

  @Test
  public void testSearchResults() throws Exception {
    mockMvc.perform(PUT_METADATA_REQUEST);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/search?q=Winterfell"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    String.format("[\"%s%s\"]", endpointUrlRetrieve, TEST_METADATA_PRODUCT_ID)));
  }

  // TODO: Update the MIS itests when we remove the deprecated endpoints

  /*
   * ================================================================
   * ==================== Store Controller Tests ====================
   * ================================================================
   */
  @Test
  public void testRetrieveProduct() throws Exception {
    // TODO
  }

  @Test
  public void testStoreProduct() throws Exception {
    when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    mockMvc.perform(POST_PRODUCT_REQUEST).andExpect(MockMvcResultMatchers.status().isCreated());
  }

  @Test
  public void storeCST() throws Exception {
    mockMvc.perform(PUT_METADATA_REQUEST);

    assertThat(
        indexedMetadataRepository.findById(TEST_METADATA_PRODUCT_ID),
        isPresentAnd(Matchers.hasProperty("contents", is(TEST_CONTENTS))));
  }

  @Test
  public void testStoreMetadata() throws Exception {
    mockMvc.perform(PUT_METADATA_REQUEST).andExpect(MockMvcResultMatchers.status().isOk());
  }

  /*
   * ================================================================
   * ========================== S3 Tests ============================
   * ================================================================
   */

  @Test
  public void testS3UnableToStore() throws Exception {
    // given
    when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(SdkServiceException.builder().build());

    // verify
    mockMvc.perform(POST_PRODUCT_REQUEST).andExpect(status().is5xxServerError());
  }

  @Test
  public void testS3Unavailable() throws Exception {
    // given
    when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(SdkClientException.builder().build());

    // verify
    mockMvc.perform(POST_PRODUCT_REQUEST).andExpect(status().is5xxServerError());
  }

  @Test
  public void testS3ThrowsRuntimeException() throws Exception {
    // given
    when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(new RuntimeException());

    // verify
    mockMvc.perform(POST_PRODUCT_REQUEST).andExpect(status().is5xxServerError());
  }
}
