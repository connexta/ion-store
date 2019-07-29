/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.connexta.multiintstore.config.AmazonS3Configuration;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext
@MockBean(SolrClient.class)
public class S3Tests {

  private static final String MINIO_ADMIN_ACCESS_KEY = "admin";
  private static final String MINIO_ADMIN_SECRET_KEY = "12345678";
  private static final int MINIO_PORT = 9000;

  @ClassRule
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

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private AmazonS3 amazonS3;

  @Value("${endpointUrl.retrieve}")
  private String endpointUrlRetrieve;

  @Value("${aws.s3.bucket.quarantine}")
  private String s3Bucket;

  @Before
  public void before() {
    amazonS3.createBucket(s3Bucket);
  }

  @After
  public void after() {
    amazonS3.listObjects(s3Bucket).getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .forEach(key -> amazonS3.deleteObject(s3Bucket, key));
    amazonS3.deleteBucket(s3Bucket);
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testStoreProductWhenS3IsEmpty() throws Exception {
    // given
    final InputStream inputStream =
        IOUtils.toInputStream(
            "All the color had been leached from Winterfell until only grey and white remained",
            StandardCharsets.UTF_8);
    final long fileSize = (long) inputStream.available();
    final String fileName = "test_file_name.txt";
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("fileSize", String.valueOf(fileSize));
    body.add("mimeType", "text/plain");
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
    body.add("fileName", fileName);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Accept-Version", "0.1.0");

    // when
    final ResponseEntity<Resource> response =
        restTemplate.postForEntity(
            "/mis/product/", new HttpEntity<>(body, httpHeaders), Resource.class);

    // then
    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    assertThat(
        response.getHeaders().getLocation(),
        allOf(
            notNullValue(),
            new TypeSafeMatcher<URI>() {
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
            }));
  }

  @Test
  public void testStoreProductWhenS3IsntEmpty() throws Exception {
    // given
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> firstBody = new LinkedMultiValueMap<>();
    firstBody.add("fileSize", String.valueOf(firstFileSize));
    firstBody.add("mimeType", "text/plain");
    firstBody.add(
        "file",
        new InputStreamResource(firstInputStream) {

          @Override
          public long contentLength() {
            return firstFileSize;
          }

          @Override
          public String getFilename() {
            return firstFileName;
          }
        });
    firstBody.add("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstBody, firstHttpHeaders));

    // and store another product
    final InputStream inputStream =
        IOUtils.toInputStream("another product contents", StandardCharsets.UTF_8);
    final long fileSize = (long) inputStream.available();
    final String fileName = "test_file_name.txt";
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("fileSize", String.valueOf(fileSize));
    body.add("mimeType", "text/plain");
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
    body.add("fileName", fileName);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Accept-Version", "0.1.0");

    // when
    final ResponseEntity<Resource> response =
        restTemplate.postForEntity(
            "/mis/product/", new HttpEntity<>(body, httpHeaders), Resource.class);

    // then
    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    assertThat(
        response.getHeaders().getLocation(),
        allOf(
            notNullValue(),
            not(equalTo(firstLocation)),
            // TODO pull into private class
            new TypeSafeMatcher<URI>() {
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
            }));
  }

  @Test
  public void testRetrieveProduct() throws Exception {
    // given
    final Charset encoding = StandardCharsets.UTF_8;
    final String contents =
        "All the color had been leached from Winterfell until only grey and white remained";
    final InputStream inputStream = IOUtils.toInputStream(contents, encoding);
    final long fileSize = (long) inputStream.available();
    final String fileName = "test_file_name.txt";
    final String mimeType = "text/plain";
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("fileSize", String.valueOf(fileSize));
    body.add("mimeType", mimeType);
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
    body.add("fileName", fileName);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Accept-Version", "0.1.0");

    final URI location =
        restTemplate.postForLocation("/mis/product/", new HttpEntity<>(body, httpHeaders));

    // when
    final ResponseEntity<Resource> response = restTemplate.getForEntity(location, Resource.class);

    // then
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    final Resource getProductResponseResource = response.getBody();
    assertThat(getProductResponseResource.getFilename(), is(fileName));
    assertThat(getProductResponseResource.contentLength(), is(fileSize));
    assertThat(getProductResponseResource.isReadable(), is(true));
    final HttpHeaders getProductResponseHeaders = response.getHeaders();
    assertThat(
        getProductResponseHeaders.getContentDisposition().toString(),
        is(String.format("attachment; filename=\"%s\"", fileName)));
    assertThat(getProductResponseHeaders.getContentType(), is(MediaType.valueOf(mimeType)));
    assertThat(
        IOUtils.toString(getProductResponseResource.getInputStream(), encoding), is(contents));
  }

  /**
   * @see #testRetrieveProductWhenS3IsEmpty()
   * @see RetrieveProductTests#testS3KeyDoesNotExist()
   */
  @Test
  public void testRetrieveProductIdNotFound() throws Exception {
    // given
    final InputStream inputStream =
        IOUtils.toInputStream(
            "All the color had been leached from Winterfell until only grey and white remained",
            StandardCharsets.UTF_8);
    final long fileSize = (long) inputStream.available();
    final String fileName = "test_file_name.txt";
    final MultiValueMap<String, Object> multipartBodyBuilder = new LinkedMultiValueMap<>();
    multipartBodyBuilder.add("fileSize", String.valueOf(fileSize));
    multipartBodyBuilder.add("mimeType", "text/plain");
    multipartBodyBuilder.add(
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
    multipartBodyBuilder.add("fileName", fileName);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Accept-Version", "0.1.0");

    restTemplate.postForEntity(
        "/mis/product/", new HttpEntity<>(multipartBodyBuilder, httpHeaders), Resource.class);

    final URIBuilder uriBuilder = new URIBuilder(endpointUrlRetrieve);
    uriBuilder.setPath(uriBuilder.getPath() + "/1234");

    // verify
    // TODO return 404 if key doesn't exist
    assertThat(
        restTemplate.getForEntity(uriBuilder.build(), Resource.class).getStatusCode(),
        is(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  /**
   * @see #testRetrieveProductIdNotFound()
   * @see RetrieveProductTests#testS3KeyDoesNotExist()
   */
  @Test
  public void testRetrieveProductWhenS3IsEmpty() throws Exception {
    final URIBuilder uriBuilder = new URIBuilder(endpointUrlRetrieve);
    uriBuilder.setPath(uriBuilder.getPath() + "/1234");
    // TODO return 404 if key doesn't exist
    assertThat(
        restTemplate.getForEntity(uriBuilder.build(), Resource.class).getStatusCode(),
        is(HttpStatus.INTERNAL_SERVER_ERROR));
  }
}
