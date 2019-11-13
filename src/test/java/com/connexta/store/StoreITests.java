/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static com.connexta.store.controllers.StoreController.ADD_METADATA_URL_TEMPLATE;
import static com.connexta.store.controllers.StoreController.CREATE_DATASET_URL_TEMPLATE;
import static com.connexta.store.controllers.StoreController.SUPPORTED_METADATA_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.controllers.StoreController;
import com.connexta.store.controllers.StoreControllerRetrieveFileComponentTest;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext
@Testcontainers
public class StoreITests {

  private static final String DATASET_ID = "341d6c1ce5e0403a99fe86edaed66eea";
  private static final String MINIO_ADMIN_ACCESS_KEY = "admin";
  private static final String MINIO_ADMIN_SECRET_KEY = "12345678";
  private static final int MINIO_PORT = 9000;

  private static final byte[] TEST_FILE = "some-content".getBytes();
  private static final byte[] TEST_METACARD = "metacard-content".getBytes();
  private static final Resource METACARD =
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
  public static final GenericContainer minioContainer =
      new GenericContainer("minio/minio:RELEASE.2019-07-10T00-34-56Z")
          .withEnv("MINIO_ACCESS_KEY", MINIO_ADMIN_ACCESS_KEY)
          .withEnv("MINIO_SECRET_KEY", MINIO_ADMIN_SECRET_KEY)
          .withExposedPorts(MINIO_PORT)
          .withCommand("server --compat /data")
          .waitingFor(
              new HttpWaitStrategy()
                  .forPath("/minio/health/ready")
                  .withStartupTimeout(Duration.ofSeconds(30)));

  @TestConfiguration
  static class Config {

    @Bean
    public AmazonS3Configuration testAmazonS3Configuration() {
      return new AmazonS3Configuration(
          String.format(
              "http://%s:%d",
              minioContainer.getContainerIpAddress(), minioContainer.getMappedPort(MINIO_PORT)),
          "local",
          MINIO_ADMIN_ACCESS_KEY,
          MINIO_ADMIN_SECRET_KEY);
    }
  }

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
    builder.part(
        "file",
        new ByteArrayResource(TEST_FILE) {

          @Override
          public long contentLength() {
            return TEST_FILE.length;
          }

          @Override
          public String getFilename() {
            return "originalFilename.txt";
          }
        });
    builder.part("metacard", METACARD);
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

  @Test
  public void testCreateDatasetWhenS3IsEmpty() throws Exception {
    // given
    final InputStream inputStream =
        IOUtils.toInputStream(
            "All the color had been leached from Winterfell until only grey and white remained",
            StandardCharsets.UTF_8);
    final long fileSize = (long) inputStream.available();
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
            return "test_file_name.txt";
          }
        });

    // when
    final ResponseEntity response =
        restTemplate.postForEntity(CREATE_DATASET_URL_TEMPLATE, body, Void.class);

    // then
    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
  }

  @Test
  public void testCreateDatasetWhenS3IsntEmpty() throws Exception {
    // given
    final InputStream firstInputStream =
        IOUtils.toInputStream("first file contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String fileName = "test_file_name.txt";
    final MultiValueMap<String, Object> firstBody = new LinkedMultiValueMap<>();
    firstBody.add(
        "file",
        new InputStreamResource(firstInputStream) {

          @Override
          public long contentLength() {
            return firstFileSize;
          }

          @Override
          public String getFilename() {
            return fileName;
          }
        });
    final URI firstLocation = restTemplate.postForLocation(CREATE_DATASET_URL_TEMPLATE, firstBody);

    // and create another dataset
    final InputStream inputStream =
        IOUtils.toInputStream("another file contents", StandardCharsets.UTF_8);
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
            return fileName;
          }
        });

    // when
    final ResponseEntity response =
        restTemplate.postForEntity(CREATE_DATASET_URL_TEMPLATE, body, Void.class);

    // then
    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
  }

  @Test
  public void testRetrieveFile() throws Exception {
    // given
    final Charset encoding = StandardCharsets.UTF_8;
    final String contents =
        "All the color had been leached from Winterfell until only grey and white remained";
    final InputStream inputStream = IOUtils.toInputStream(contents, encoding);
    final long fileSize = (long) inputStream.available();
    final String mediaType = MediaType.TEXT_PLAIN_VALUE;
    final String fileName = "test_file_name.txt";
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
            return fileName;
          }
        });

    final URI location = restTemplate.postForLocation(CREATE_DATASET_URL_TEMPLATE, body);

    // when
    final ResponseEntity<Resource> response = restTemplate.getForEntity(location, Resource.class);

    // then
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    final Resource retrievedFile = response.getBody();
    assertThat(retrievedFile.getFilename(), is(fileName));
    assertThat(retrievedFile.contentLength(), is(fileSize));
    assertThat(retrievedFile.isReadable(), is(true));
    final HttpHeaders responseHeaders = response.getHeaders();
    assertThat(
        responseHeaders.getContentDisposition().toString(),
        is(String.format("attachment; filename=\"%s\"", fileName)));
    assertThat(responseHeaders.getContentType(), is(MediaType.valueOf(mediaType)));
    assertThat(IOUtils.toString(retrievedFile.getInputStream(), encoding), is(contents));
  }

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
