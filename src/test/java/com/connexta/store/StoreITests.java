/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.awaitility.Awaitility.await;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.rest.models.QuarantineRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import junit.framework.AssertionFailedError;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
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
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Testcontainers
@AutoConfigureWebTestClient
class StoreITests {

  private static final String TEST_FILE_PATH = "com/connexta/store/testFile.txt";
  private static final String TRANSFORM_DONE_RESPONSE_FILE =
      "com/connexta/store/transformDone.json";

  private static final String DATASET_ID = UUID.randomUUID().toString();
  private static final String ADMIN_ACCESS_KEY = "admin";
  private static final String ADMIN_SECRET_KEY = "12345678";
  private static final String LAST_MODIFIED_DATE = "2017-06-11T14:32:28Z";
  private static final int LOCALSTACK_PORT = 4572;
  private static final String ACCEPT_VERSION = "Accept-Version";
  private static final String APPLICATION_DNI_TDF_XML = "application/dni-tdf+xml";
  private static final String ORIGINAL_FILENAME = "originalFilename.txt";

  @Autowired private WebTestClient storeTestClient;

  private static final String TEST_FILE_CONTENT = "some-content";
  private static final Resource TEST_FILE_RESOURCE =
      new ByteArrayResource(TEST_FILE_CONTENT.getBytes()) {

        @Override
        public long contentLength() {
          return TEST_FILE_CONTENT.getBytes().length;
        }

        @Override
        public String getFilename() {
          return ORIGINAL_FILENAME;
        }
      };
  private static final String TEST_METACARD_CONTENT = "metacard-content";
  private static final Resource TEST_METACARD_RESOURCE =
      new ByteArrayResource(TEST_METACARD_CONTENT.getBytes()) {

        @Override
        public long contentLength() {
          return TEST_METACARD_CONTENT.getBytes().length;
        }

        @Override
        public String getFilename() {
          return "metacard.xml";
        }
      };

  @Container
  private static final GenericContainer s3Container =
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
          ADMIN_ACCESS_KEY,
          ADMIN_SECRET_KEY);
    }
  }

  @Inject private AmazonS3 amazonS3;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @Value("${endpoints.store.url}")
  private String storeUrl;

  @Value("${endpoints.transform.url}")
  private String transformUrl;

  @Value("${endpoints.ingest.version}")
  private String ingestVersion;

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

  private MockWebServer mockTransformServer;
  private MockWebServer mockFileServer;
  private MockWebServer mockIndexServer;

  @BeforeEach
  void beforeEach() throws Exception {
    mockTransformServer = new MockWebServer();
    mockTransformServer.start(12346);
    mockFileServer = new MockWebServer();
    mockFileServer.start();
    mockIndexServer = new MockWebServer();
    mockIndexServer.start(12345);

    amazonS3.createBucket(fileBucket);
    amazonS3.createBucket(irmBucket);
    amazonS3.createBucket(metacardBucket);
  }

  @AfterEach
  void afterEach() throws Exception {
    cleanBucket(fileBucket);
    cleanBucket(irmBucket);
    cleanBucket(metacardBucket);

    mockTransformServer.shutdown();
    mockFileServer.shutdown();
    mockIndexServer.shutdown();
  }

  private void cleanBucket(String bucket) {
    amazonS3.listObjects(bucket).getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .forEach(key -> amazonS3.deleteObject(bucket, key));
    amazonS3.deleteBucket(bucket);
  }

  @Test
  void testRetrieveDataNotFound() {
    storeTestClient
        .get()
        .uri(
            UriComponentsBuilder.fromUriString(storeUrl)
                .path("/dataset/{datasetId}/{dataType}")
                .build(DATASET_ID, "file")
                .toASCIIString())
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void testIngestRequest() throws Exception {
    // setup
    final String transformStatusUrl = transformUrl + "some/location";
    MockResponse transformRequestResponse =
        new MockResponse()
            .setResponseCode(HttpStatus.CREATED.value())
            .setHeader("Location", transformStatusUrl);
    MockResponse transformPollDoneResponse =
        new MockResponse()
            .setResponseCode(200)
            .setBody(getResourceAsString(TRANSFORM_DONE_RESPONSE_FILE))
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    mockTransformServer.enqueue(transformRequestResponse);
    mockTransformServer.enqueue(transformPollDoneResponse);

    MockResponse indexResponse = new MockResponse().setResponseCode(HttpStatus.OK.value());
    mockIndexServer.enqueue(indexResponse);

    final String testFileContent = getResourceAsString(TEST_FILE_PATH);
    mockTransformServer.enqueue(new MockResponse().setResponseCode(200).setBody(testFileContent));

    // when
    storeTestClient
        .post()
        .uri("/ingest")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromValue(TEST_INGEST_REQUEST_BODY))
        .header(ACCEPT_VERSION, ingestVersion)
        .header("Last-Modified", LAST_MODIFIED_DATE)
        .exchange();

    RecordedRequest transformRequest = mockTransformServer.takeRequest();
    final String datasetId =
        (String) new JSONObject(transformRequest.getBody().readUtf8()).get("datasetId");

    final String expectedIrmFileName = "irm-" + datasetId + ".xml";

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () ->
                dataIsAvailable(
                    datasetId,
                    "irm",
                    APPLICATION_DNI_TDF_XML,
                    expectedIrmFileName,
                    testFileContent));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () ->
                dataIsAvailable(
                    datasetId,
                    "file",
                    MediaType.TEXT_PLAIN_VALUE,
                    ORIGINAL_FILENAME,
                    TEST_FILE_CONTENT));
  }

  @Test
  void testQuarantine() throws Exception {
    // setup
    final String transformStatusUrl = transformUrl + "some/location";
    MockResponse transformRequestResponse =
        new MockResponse()
            .setResponseCode(HttpStatus.CREATED.value())
            .setHeader("Location", transformStatusUrl);
    MockResponse transformPollDoneResponse =
        new MockResponse()
            .setResponseCode(200)
            .setBody(getResourceAsString(TRANSFORM_DONE_RESPONSE_FILE))
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    mockTransformServer.enqueue(transformRequestResponse);
    mockTransformServer.enqueue(transformPollDoneResponse);

    final String testFileContent = getResourceAsString(TEST_FILE_PATH);
    mockTransformServer.enqueue(new MockResponse().setResponseCode(200).setBody(testFileContent));

    // when ingested
    storeTestClient
        .post()
        .uri("/ingest")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromValue(TEST_INGEST_REQUEST_BODY))
        .header(ACCEPT_VERSION, ingestVersion)
        .header("Last-Modified", LAST_MODIFIED_DATE)
        .exchange();

    // then data is available
    RecordedRequest transformRequest = mockTransformServer.takeRequest();
    final String datasetId =
        (String) new JSONObject(transformRequest.getBody().readUtf8()).get("datasetId");

    final String expectedIrmFileName = "irm-" + datasetId + ".xml";

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () ->
                dataIsAvailable(
                    datasetId,
                    "irm",
                    APPLICATION_DNI_TDF_XML,
                    expectedIrmFileName,
                    testFileContent));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () ->
                dataIsAvailable(
                    datasetId,
                    "file",
                    MediaType.TEXT_PLAIN_VALUE,
                    ORIGINAL_FILENAME,
                    TEST_FILE_CONTENT));

    // and when quarantined
    storeTestClient
        .post()
        .uri(
            UriComponentsBuilder.fromUriString(storeUrl)
                .path("/dataset/{datasetId}/quarantine")
                .build(datasetId)
                .toASCIIString())
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new QuarantineRequest()))
        .header(ACCEPT_VERSION, storeApiVersion)
        .exchange()
        .expectStatus()
        .isOk();

    // then data is no longer found
    storeTestClient
        .get()
        .uri(
            UriComponentsBuilder.fromUriString(storeUrl)
                .path("/dataset/{datasetId}/{dataType}")
                .build(datasetId, "irm")
                .toASCIIString())
        .exchange()
        .expectStatus()
        .isNotFound();

    storeTestClient
        .get()
        .uri(
            UriComponentsBuilder.fromUriString(storeUrl)
                .path("/dataset/{datasetId}/{dataType}")
                .build(datasetId, "file")
                .toASCIIString())
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  private boolean dataIsAvailable(
      String datasetId,
      String dataType,
      String expectedContentType,
      String expectedFilename,
      String expectedContent) {
    try {
      storeTestClient
          .get()
          .uri(
              UriComponentsBuilder.fromUriString(storeUrl)
                  .path("/dataset/{datasetId}/{dataType}")
                  .build(datasetId, dataType)
                  .toASCIIString())
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .contentType(expectedContentType)
          .expectHeader()
          .contentDisposition(
              ContentDisposition.builder("attachment").filename(expectedFilename).build())
          .expectBody(String.class)
          .isEqualTo(expectedContent);
    } catch (AssertionError e) {
      return false;
    }
    return true;
  }

  private String getResourceAsString(String testResourcePath) throws IOException {
    InputStream inputStream =
        this.getClass().getClassLoader().getResourceAsStream(testResourcePath);

    if (inputStream != null) {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    throw new AssertionFailedError(
        String.format("No test resource found for path %s", testResourcePath));
  }
}
