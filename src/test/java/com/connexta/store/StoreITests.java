/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static com.connexta.store.controllers.StoreController.ADD_METADATA_URL_TEMPLATE;
import static com.connexta.store.controllers.StoreController.CREATE_DATASET_URL_TEMPLATE;
import static com.connexta.store.controllers.StoreController.METACARD_MEDIA_TYPE;
import static com.connexta.store.controllers.StoreController.SUPPORTED_METADATA_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.controllers.StoreController;
import com.connexta.store.controllers.StoreControllerRetrieveFileComponentTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext
@Testcontainers
@AutoConfigureWebTestClient
public class StoreITests {

  private static final String DATASET_ID = "341d6c1ce5e0403a99fe86edaed66eea";
  private static final String LAST_MODIFIED_DATE = "2017-06-11T14:32:28Z";
  private static final int LOCALSTACK_PORT = 4572;

  @Inject private WebTestClient webTestClient;

  private static final byte[] TEST_FILE = "some-content".getBytes();
  private static final Resource TEST_FILE_RESOURCE =
      new ByteArrayResource(TEST_FILE) {

        @Override
        public long contentLength() {
          return TEST_FILE.length;
        }

        @Override
        public String getFilename() {
          return "originalFilename.txt";
        }
      };
  private static final byte[] TEST_METACARD = "metacard-content".getBytes();
  private static final Resource TEST_METACARD_RESOURCE =
      new ByteArrayResource(TEST_METACARD) {

        @Override
        public long contentLength() {
          return TEST_METACARD.length;
        }

        @Override
        public String getFilename() {
          return "metacard.xml";
        }
      };

  @Container
  public static final GenericContainer s3Container =
      new GenericContainer("localstack/localstack:0.10.5")
          .withExposedPorts(LOCALSTACK_PORT)
          .withEnv("SERVICES", "s3")
          .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Ready\\.\n"));

  @TestConfiguration
  static class Config {

    @Bean
    public AmazonS3Configuration testAmazonS3Configuration() {
      return new AmazonS3Configuration(
          String.format(
              "http://%s:%d",
              s3Container.getContainerIpAddress(), s3Container.getMappedPort(LOCALSTACK_PORT)),
          "local",
          "access-key",
          "secret-key");
    }
  }

  // TODO Remove TestRestTemplate and use the WebTestClient that is already injected in this class.
  private TestRestTemplate restTemplate;
  private MockRestServiceServer indexMockRestServiceServer;
  private MockRestServiceServer transformMockRestServiceServer;
  @Autowired private ApplicationContext applicationContext;
  @Inject private AmazonS3 amazonS3;

  @Inject
  @Named("nonBufferingRestTemplate")
  private RestTemplate nonBufferingRestTemplate;

  @Inject
  @Named("transformClientRestTemplate")
  private RestTemplate transformClientRestTemplate;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @Value("${endpointUrl.retrieve}")
  private String endpointUrlRetrieve;

  @Value("${endpointUrl.index}")
  private String endpointUrlIndex;

  @Value("${endpoints.index.version}")
  private String indexApiVersion;

  @Value("${endpointUrl.transform}")
  private String endpointUrlTransform;

  @Value("${endpoints.transform.version}")
  private String endpointsTransformVersion;

  @Value("${s3.bucket.file}")
  private String fileBucket;

  @Value("${s3.bucket.irm}")
  private String irmBucket;

  @Value("${s3.bucket.metacard}")
  private String metacardBucket;

  private static final MultiValueMap<String, HttpEntity<?>> TEST_INGEST_REQUEST_BODY;

  static {
    final MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", TEST_FILE_RESOURCE);
    builder.part("metacard", TEST_METACARD_RESOURCE);
    builder.part("correlationId", "000f4e4a");
    TEST_INGEST_REQUEST_BODY = builder.build();
  }

  @BeforeEach
  public void beforeEach() {
    restTemplate =
        new CustomTestRestTemplate(applicationContext)
            .addRequestHeader(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion);

    indexMockRestServiceServer = MockRestServiceServer.createServer(nonBufferingRestTemplate);
    transformMockRestServiceServer =
        MockRestServiceServer.createServer(transformClientRestTemplate);

    amazonS3.createBucket(fileBucket);
    amazonS3.createBucket(irmBucket);
    amazonS3.createBucket(metacardBucket);
  }

  @AfterEach
  public void afterEach() {
    indexMockRestServiceServer.verify();
    transformMockRestServiceServer.verify();
    transformMockRestServiceServer.reset();

    cleanBucket(fileBucket);
    cleanBucket(irmBucket);
    cleanBucket(metacardBucket);
  }

  private void cleanBucket(String bucket) {
    amazonS3.listObjects(bucket).getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .forEach(key -> amazonS3.deleteObject(bucket, key));
    amazonS3.deleteBucket(bucket);
  }

  @Test
  public void testContextLoads() {}

  /**
   * @see #testRetrieveFileWhenS3IsEmpty()
   * @see StoreControllerRetrieveFileComponentTest#testS3KeyDoesNotExist()
   */
  @Test
  public void testRetrieveFileNotFound() throws Exception {
    // given
    final InputStream inputStream =
        IOUtils.toInputStream(
            "All the color had been leached from Winterfell until only grey and white remained",
            StandardCharsets.UTF_8);
    final long fileSize = inputStream.available();
    final MultiValueMap<String, Object> multipartBodyBuilder = new LinkedMultiValueMap<>();
    multipartBodyBuilder.add(
        "file",
        new InputStreamResource(inputStream) {

          @Override
          public long contentLength() {
            return fileSize;
          }

          @Override
          public String getFilename() {
            return "test_file_name.txt";
          }
        });

    restTemplate.postForEntity(CREATE_DATASET_URL_TEMPLATE, multipartBodyBuilder, Void.class);

    // verify
    assertThat(
        restTemplate
            .getForEntity(
                UriComponentsBuilder.fromUriString(endpointUrlRetrieve)
                    .path(StoreController.RETRIEVE_FILE_URL_TEMPLATE)
                    .build(DATASET_ID),
                Resource.class)
            .getStatusCode(),
        is(HttpStatus.NOT_FOUND));
  }

  /**
   * @see #testRetrieveFileNotFound()
   * @see StoreControllerRetrieveFileComponentTest#testS3KeyDoesNotExist()
   */
  @Test
  public void testRetrieveFileWhenS3IsEmpty() throws Exception {
    assertThat(
        restTemplate
            .getForEntity(
                UriComponentsBuilder.fromUriString(endpointUrlRetrieve)
                    .path(StoreController.RETRIEVE_FILE_URL_TEMPLATE)
                    .build(DATASET_ID),
                Resource.class)
            .getStatusCode(),
        is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void testAddMetadata() throws Exception {
    // given
    final String datasetId = "00067360b70e4acfab561fe593ad3f7a";
    final URI irmUri =
        UriComponentsBuilder.fromUriString(endpointUrlRetrieve)
            .path(StoreController.RETRIEVE_IRM_URL_TEMPLATE)
            .build(datasetId);

    // and stub index server
    indexMockRestServiceServer
        .expect(requestTo(String.format("%s%s", endpointUrlIndex, datasetId)))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Accept-Version", indexApiVersion))
        .andExpect(jsonPath("$.irmLocation").value(irmUri.toString()))
        .andRespond(withStatus(HttpStatus.OK));

    // when add metadata
    final Charset encoding = StandardCharsets.UTF_8;
    final String irm = "<?xml version=\"1.0\" ?><metadata></metadata>";
    final InputStream inputStream = IOUtils.toInputStream(irm, encoding);
    final long fileSize = inputStream.available();
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new InputStreamResource(inputStream) {

          @Override
          public long contentLength() {
            return fileSize;
          }

          @Override
          public String getFilename() {
            return "test_file_name.xml";
          }
        });
    restTemplate.put(ADD_METADATA_URL_TEMPLATE, body, datasetId, SUPPORTED_METADATA_TYPE);

    // then
    assertThat(irmUri, getRequestIsSuccessful());
  }

  @Test
  public void testRetrieveIrm() throws Exception {
    // given
    final String datasetId = "00067360b70e4acfab561fe593ad3f7a";
    final URI irmUri =
        UriComponentsBuilder.fromUriString(endpointUrlRetrieve)
            .path(StoreController.RETRIEVE_IRM_URL_TEMPLATE)
            .build(datasetId);

    // and stub index server
    indexMockRestServiceServer
        .expect(requestTo(String.format("%s%s", endpointUrlIndex, datasetId)))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Accept-Version", indexApiVersion))
        .andExpect(jsonPath("$.irmLocation").value(irmUri.toString()))
        .andRespond(withStatus(HttpStatus.OK));

    // and add metadata
    final Charset encoding = StandardCharsets.UTF_8;
    final String irm = "<?xml version=\"1.0\" ?><metadata></metadata>";
    final InputStream inputStream = IOUtils.toInputStream(irm, encoding);
    final long fileSize = inputStream.available();
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new InputStreamResource(inputStream) {

          @Override
          public long contentLength() {
            return fileSize;
          }

          @Override
          public String getFilename() {
            return "test_file_name.xml";
          }
        });
    restTemplate.put(ADD_METADATA_URL_TEMPLATE, body, datasetId, SUPPORTED_METADATA_TYPE);

    // when
    final ResponseEntity<Resource> response = restTemplate.getForEntity(irmUri, Resource.class);

    // then
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    final Resource retrievedFile = response.getBody();
    assertThat(retrievedFile.contentLength(), is(fileSize));
    assertThat(retrievedFile.isReadable(), is(true));
    final HttpHeaders responseHeaders = response.getHeaders();
    assertThat(responseHeaders.getContentType(), is(StoreController.IRM_MEDIA_TYPE));
    assertThat(IOUtils.toString(retrievedFile.getInputStream(), encoding), is(irm));
  }

  @Test
  public void testRetrieveIrmNotFound() throws Exception {
    // given
    final String datasetId = "00067360b70e4acfab561fe593ad3f7a";
    final URI irmUri =
        UriComponentsBuilder.fromUriString(endpointUrlRetrieve)
            .path(StoreController.RETRIEVE_IRM_URL_TEMPLATE)
            .build(datasetId);
    indexMockRestServiceServer
        .expect(requestTo(String.format("%s%s", endpointUrlIndex, datasetId)))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Accept-Version", indexApiVersion))
        .andExpect(jsonPath("$.irmLocation").value(irmUri.toString()))
        .andRespond(withStatus(HttpStatus.OK));
    final Charset encoding = StandardCharsets.UTF_8;
    final String irm = "<?xml version=\"1.0\" ?><metadata></metadata>";
    final InputStream inputStream = IOUtils.toInputStream(irm, encoding);
    final long fileSize = inputStream.available();
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new InputStreamResource(inputStream) {

          @Override
          public long contentLength() {
            return fileSize;
          }

          @Override
          public String getFilename() {
            return "test_file_name.xml";
          }
        });
    restTemplate.put(ADD_METADATA_URL_TEMPLATE, body, datasetId, SUPPORTED_METADATA_TYPE);

    // verify
    assertThat(
        restTemplate
            .getForEntity(
                UriComponentsBuilder.fromUriString(endpointUrlRetrieve)
                    .path(StoreController.RETRIEVE_IRM_URL_TEMPLATE)
                    .build(DATASET_ID),
                Resource.class)
            .getStatusCode(),
        is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void testRetrieveIrmWhenS3IsEmpty() {
    assertThat(
        restTemplate
            .getForEntity(
                UriComponentsBuilder.fromUriString(endpointUrlRetrieve)
                    .path(StoreController.RETRIEVE_IRM_URL_TEMPLATE)
                    .build(DATASET_ID),
                Resource.class)
            .getStatusCode(),
        is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void testSuccessfulIngestRequest() throws Exception {
    transformMockRestServiceServer
        .expect(requestTo(endpointUrlTransform))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Accept-Version", endpointsTransformVersion))
        .andExpect(
            request -> {
              final String fileLocation =
                  (String)
                      new JsonPathExpectationsHelper("$.location")
                          .evaluateJsonPath(
                              ((MockClientHttpRequest) request).getBodyAsString(), String.class);

              // Verify GET request returns file
              // Retrieve file is tested more thoroughly in other methods in this class.
              webTestClient
                  .get()
                  .uri(fileLocation)
                  .exchange()
                  .expectStatus()
                  .isOk()
                  .expectBody(Resource.class)
                  .value(hasContents(TEST_FILE_RESOURCE.getInputStream()));
            })
        .andExpect(jsonPath("$.mimeType").value(MediaType.TEXT_PLAIN_VALUE))
        .andExpect(
            request -> {
              final String metacardLocation =
                  (String)
                      new JsonPathExpectationsHelper("$.metacardLocation")
                          .evaluateJsonPath(
                              ((MockClientHttpRequest) request).getBodyAsString(), String.class);

              // Verify retrieve metacard
              webTestClient
                  .get()
                  .uri(metacardLocation)
                  .exchange()
                  .expectStatus()
                  .isOk()
                  .expectHeader()
                  .contentType(METACARD_MEDIA_TYPE)
                  .expectBody(Resource.class)
                  .value(isReadable())
                  .value(hasContents(TEST_METACARD_RESOURCE.getInputStream()));
            })
        .andRespond(
            withStatus(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    new JSONObject()
                        .put("id", "asdf")
                        .put("message", "The ID asdf has been accepted")
                        .toString()));

    // when
    webTestClient
        .post()
        .uri("/ingest")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .syncBody(TEST_INGEST_REQUEST_BODY)
        .header(
            "Accept-Version",
            "0.5.0") // TODO inject this like we do for the transformApiVersion in TransformClient
        .header(LAST_MODIFIED, LAST_MODIFIED_DATE)
        .exchange()
        .expectStatus()
        .isAccepted();
  }

  /**
   * The error handler throws the same exception for all non-202 status codes returned by the
   * transformation endpoint.
   */
  @Test
  public void testUnsuccessfulTransformRequest() throws Exception {
    transformMockRestServiceServer
        .expect(requestTo(endpointUrlTransform))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Accept-Version", endpointsTransformVersion))
        .andExpect(jsonPath("$.location").isNotEmpty())
        .andExpect(jsonPath("$.mimeType").value(MediaType.TEXT_PLAIN_VALUE))
        .andExpect(jsonPath("$.metacardLocation").isNotEmpty())
        .andRespond(withServerError());

    webTestClient
        .post()
        .uri("/ingest")
        .header("Accept-Version", "1.1.1")
        .header(LAST_MODIFIED, LAST_MODIFIED_DATE)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(MediaType.APPLICATION_JSON)
        .syncBody(TEST_INGEST_REQUEST_BODY)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @NotNull
  private static Matcher<Resource> hasContents(final InputStream expected) {
    return new TypeSafeMatcher<>() {

      @Override
      protected boolean matchesSafely(Resource resource) {
        try {
          return IOUtils.contentEquals(resource.getInputStream(), expected);
        } catch (IOException e) {
          fail("Unable to compare input streams", e);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has the expected contents");
      }
    };
  }

  @NotNull
  private static Matcher<Resource> isReadable() {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(Resource resource) {
        return resource.isReadable();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("is readable");
      }
    };
  }

  @NotNull
  private Matcher<URI> getRequestIsSuccessful() {
    return new TypeSafeMatcher<URI>() {
      private final HttpStatus EXPECTED_GET_REQUEST_RESPONSE_STATUS = HttpStatus.OK;

      @Override
      public void describeTo(Description description) {
        description.appendText(
            "a URI for which a GET request returns "
                + EXPECTED_GET_REQUEST_RESPONSE_STATUS
                + " but the URI");
      }

      @Override
      protected boolean matchesSafely(URI uri) {
        return restTemplate.getForEntity(uri, Resource.class).getStatusCode()
            == EXPECTED_GET_REQUEST_RESPONSE_STATUS;
      }
    };
  }
}
