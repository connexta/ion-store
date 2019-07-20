/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static com.connexta.multiintstore.models.IndexedProductMetadata.SOLR_COLLECTION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.connexta.multiintstore.config.AmazonS3Configuration;
import com.connexta.multiintstore.config.SolrClientConfiguration;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext
public class SolrTests {

  private static final int SOLR_PORT = 8983;

  @ClassRule
  public static final GenericContainer solrContainer =
      new GenericContainer("solr:8")
          .withCommand("solr-create -c " + SOLR_COLLECTION)
          .withExposedPorts(SOLR_PORT)
          .waitingFor(Wait.forHttp("/solr/" + SOLR_COLLECTION + "/admin/ping"));

  private static final String MINIO_ADMIN_ACCESS_KEY = "admin";
  private static final String MINIO_ADMIN_SECRET_KEY = "12345678";
  private static final int MINIO_PORT = 9000;

  // TODO mock AmazonS3
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
    public SolrClientConfiguration testSolrClientConfiguration() {
      return new SolrClientConfiguration(
          solrContainer.getContainerIpAddress(), solrContainer.getMappedPort(SOLR_PORT));
    }

    // TODO mock AmazonS3
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
  @Autowired private SolrClient solrClient;

  @Value("${aws.s3.bucket.quarantine}")
  private String s3Bucket;

  @Value("${endpointUrl.retrieve}")
  private String endpointUrlRetrieve;

  @Before
  public void before() {
    amazonS3.createBucket(s3Bucket);
  }

  @After
  public void after() throws Exception {
    amazonS3.listObjects(s3Bucket).getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .forEach(key -> amazonS3.deleteObject(s3Bucket, key));
    amazonS3.deleteBucket(s3Bucket);

    emptySolrCore(solrClient, SOLR_COLLECTION);
  }

  @Test
  public void testStoreMetadataCstWhenSolrIsEmpty() throws Exception {
    // given
    final String queryKeyword = "Winterfell";
    final String productContents =
        "All the color had been leached from "
            + queryKeyword
            + " until only grey and white remained";
    final String productEncoding = "UTF-8";
    final InputStream productInputStream = IOUtils.toInputStream(productContents, productEncoding);
    final long productFileSize = (long) productInputStream.available();
    final String productFileName = "test_file_name.txt";
    final String productMimeType = "text/plain";
    final MultipartBodyBuilder storeProductRequestBodyBuilder = new MultipartBodyBuilder();
    storeProductRequestBodyBuilder.part("fileSize", String.valueOf(productFileSize));
    storeProductRequestBodyBuilder.part("mimeType", productMimeType);
    storeProductRequestBodyBuilder.part(
        "file",
        new InputStreamResource(productInputStream) {

          @Override
          public long contentLength() {
            return productFileSize;
          }

          @Override
          public String getFilename() {
            return productFileName;
          }
        });
    storeProductRequestBodyBuilder.part("fileName", productFileName);
    final HttpHeaders storeProductRequestHttpHeaders = new HttpHeaders();
    storeProductRequestHttpHeaders.set("Accept-Version", "0.1.0");

    final URI productLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(
                storeProductRequestBodyBuilder.build(), storeProductRequestHttpHeaders));

    // TODO improve comments for these sections
    final String metadataContents = "{contents:\"" + productContents + "\"";
    final String metadataEncoding = "UTF-8";
    final InputStream metadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long metadataFileSize = (long) metadataInputStream.available();
    final String metadataFileName = "test_file_name.txt";
    final String metadataMimeType = "text/plain";
    final MultipartBodyBuilder storeMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    storeMetadataRequestBodyBuilder.part("fileSize", String.valueOf(metadataFileSize));
    storeMetadataRequestBodyBuilder.part("mimeType", metadataMimeType);
    storeMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(metadataInputStream) {

          @Override
          public long contentLength() {
            return metadataFileSize;
          }

          @Override
          public String getFilename() {
            return metadataFileName;
          }
        });
    storeMetadataRequestBodyBuilder.part("fileName", metadataFileName);
    final HttpHeaders storeMetadataRequestHttpHeaders = new HttpHeaders();
    storeMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // when
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(storeMetadataRequestBodyBuilder.build(), storeMetadataRequestHttpHeaders));

    // then
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", queryKeyword);
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        contains(productLocation.toString()));
  }

  @Test
  public void testStoreMetadataCstWhenSolrIsNotEmpty() throws Exception {
    // given
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultipartBodyBuilder firstMultipartBodyBuilder = new MultipartBodyBuilder();
    firstMultipartBodyBuilder.part("fileSize", String.valueOf(firstFileSize));
    firstMultipartBodyBuilder.part("mimeType", "text/plain");
    firstMultipartBodyBuilder.part(
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
    firstMultipartBodyBuilder.part("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstMultipartBodyBuilder.build(), firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultipartBodyBuilder firstStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    firstStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBodyBuilder.part("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(firstMetadataInputStream) {

          @Override
          public long contentLength() {
            return firstMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return firstMetadataFileName;
          }
        });
    firstStoreMetadataRequestBodyBuilder.part("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(
            firstStoreMetadataRequestBodyBuilder.build(), firstStoreMetadataRequestHttpHeaders));

    // and store another product
    final String queryKeyword = "Winterfell";
    final String productContents =
        "All the color had been leached from "
            + queryKeyword
            + " until only grey and white remained";
    final String productEncoding = "UTF-8";
    final InputStream productInputStream = IOUtils.toInputStream(productContents, productEncoding);
    final long productFileSize = (long) productInputStream.available();
    final String productFileName = "test_file_name.txt";
    final String productMimeType = "text/plain";
    final MultipartBodyBuilder storeProductRequestBodyBuilder = new MultipartBodyBuilder();
    storeProductRequestBodyBuilder.part("fileSize", String.valueOf(productFileSize));
    storeProductRequestBodyBuilder.part("mimeType", productMimeType);
    storeProductRequestBodyBuilder.part(
        "file",
        new InputStreamResource(productInputStream) {

          @Override
          public long contentLength() {
            return productFileSize;
          }

          @Override
          public String getFilename() {
            return productFileName;
          }
        });
    storeProductRequestBodyBuilder.part("fileName", productFileName);
    final HttpHeaders storeProductRequestHttpHeaders = new HttpHeaders();
    storeProductRequestHttpHeaders.set("Accept-Version", "0.1.0");

    final URI productLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(
                storeProductRequestBodyBuilder.build(), storeProductRequestHttpHeaders));

    final String metadataContents = "{contents:\"" + productContents + "\"";
    final String metadataEncoding = "UTF-8";
    final InputStream metadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long metadataFileSize = (long) metadataInputStream.available();
    final String metadataFileName = "test_file_name.txt";
    final String metadataMimeType = "text/plain";
    final MultipartBodyBuilder storeMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    storeMetadataRequestBodyBuilder.part("fileSize", String.valueOf(metadataFileSize));
    storeMetadataRequestBodyBuilder.part("mimeType", metadataMimeType);
    storeMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(metadataInputStream) {

          @Override
          public long contentLength() {
            return metadataFileSize;
          }

          @Override
          public String getFilename() {
            return metadataFileName;
          }
        });
    storeMetadataRequestBodyBuilder.part("fileName", metadataFileName);
    final HttpHeaders storeMetadataRequestHttpHeaders = new HttpHeaders();
    storeMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // when
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(storeMetadataRequestBodyBuilder.build(), storeMetadataRequestHttpHeaders));

    // then
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", queryKeyword);
    assertThat(
        (List<String>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        hasItem(equalTo(productLocation.toString())));
  }

  @Test
  @Ignore("TODO verify id matches something in S3 before storing to solr")
  public void testStoreMetadataProductIdNotFound() {
    // TODO verify 404? 400?
  }

  @Test
  public void testStoreMetadataCstHasAlreadyBeenStored() throws Exception {
    // given
    final String queryKeyword = "Winterfell";
    final String productContents =
        "All the color had been leached from "
            + queryKeyword
            + " until only grey and white remained";
    final String productEncoding = "UTF-8";
    final InputStream productInputStream = IOUtils.toInputStream(productContents, productEncoding);
    final long productFileSize = (long) productInputStream.available();
    final String productFileName = "test_file_name.txt";
    final String productMimeType = "text/plain";
    final MultipartBodyBuilder storeProductRequestBodyBuilder = new MultipartBodyBuilder();
    storeProductRequestBodyBuilder.part("fileSize", String.valueOf(productFileSize));
    storeProductRequestBodyBuilder.part("mimeType", productMimeType);
    storeProductRequestBodyBuilder.part(
        "file",
        new InputStreamResource(productInputStream) {

          @Override
          public long contentLength() {
            return productFileSize;
          }

          @Override
          public String getFilename() {
            return productFileName;
          }
        });
    storeProductRequestBodyBuilder.part("fileName", productFileName);
    final HttpHeaders storeProductRequestHttpHeaders = new HttpHeaders();
    storeProductRequestHttpHeaders.set("Accept-Version", "0.1.0");

    final URI productLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(
                storeProductRequestBodyBuilder.build(), storeProductRequestHttpHeaders));

    final String metadataContents = "{contents:\"" + productContents + "\"";
    final String metadataEncoding = "UTF-8";
    final InputStream metadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long firstMetadataFileSize = (long) metadataInputStream.available();
    final String metadataFileName = "test_file_name.txt";
    final String metadataMimeType = "text/plain";
    final MultipartBodyBuilder firstStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    firstStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBodyBuilder.part("mimeType", metadataMimeType);
    firstStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(metadataInputStream) {

          @Override
          public long contentLength() {
            return firstMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return metadataFileName;
          }
        });
    firstStoreMetadataRequestBodyBuilder.part("fileName", metadataFileName);
    final HttpHeaders storeMetadataRequestHttpHeaders = new HttpHeaders();
    storeMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and the cst metadata is stored
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(
            firstStoreMetadataRequestBodyBuilder.build(), storeMetadataRequestHttpHeaders));

    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final MultipartBodyBuilder secondStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    secondStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBodyBuilder.part("mimeType", metadataMimeType);
    secondStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(secondMetadataInputStream) {

          @Override
          public long contentLength() {
            return secondMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return metadataFileName;
          }
        });
    secondStoreMetadataRequestBodyBuilder.part("fileName", storeMetadataRequestHttpHeaders);

    // when
    // TODO handle when CST has already been stored
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(
            secondStoreMetadataRequestBodyBuilder.build(), storeMetadataRequestHttpHeaders));

    // then query should still work
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", queryKeyword);
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        contains(productLocation.toString()));
  }

  @Test
  public void testQuery() throws Exception {
    // given
    // and store first product
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultipartBodyBuilder firstMultipartBodyBuilder = new MultipartBodyBuilder();
    firstMultipartBodyBuilder.part("fileSize", String.valueOf(firstFileSize));
    firstMultipartBodyBuilder.part("mimeType", "text/plain");
    firstMultipartBodyBuilder.part(
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
    firstMultipartBodyBuilder.part("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstMultipartBodyBuilder.build(), firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultipartBodyBuilder firstStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    firstStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBodyBuilder.part("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(firstMetadataInputStream) {

          @Override
          public long contentLength() {
            return firstMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return firstMetadataFileName;
          }
        });
    firstStoreMetadataRequestBodyBuilder.part("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(
            firstStoreMetadataRequestBodyBuilder.build(), firstStoreMetadataRequestHttpHeaders));

    // and store second product
    final InputStream secondInputStream =
        IOUtils.toInputStream("second product contents", StandardCharsets.UTF_8);
    final long secondFileSize = (long) secondInputStream.available();
    final String secondFileName = "test_file_name.txt";
    final MultipartBodyBuilder secondMultipartBodyBuilder = new MultipartBodyBuilder();
    secondMultipartBodyBuilder.part("fileSize", String.valueOf(secondFileSize));
    secondMultipartBodyBuilder.part("mimeType", "text/plain");
    secondMultipartBodyBuilder.part(
        "file",
        new InputStreamResource(secondInputStream) {

          @Override
          public long contentLength() {
            return secondFileSize;
          }

          @Override
          public String getFilename() {
            return secondFileName;
          }
        });
    secondMultipartBodyBuilder.part("fileName", secondFileName);
    final HttpHeaders secondHttpHeaders = new HttpHeaders();
    secondHttpHeaders.set("Accept-Version", "0.1.0");
    final URI secondLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(secondMultipartBodyBuilder.build(), secondHttpHeaders));

    final String secondMetadataContents = "{contents:\"second product metadata\"";
    final String secondMetadataEncoding = "UTF-8";
    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(secondMetadataContents, secondMetadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final String secondMetadataFileName = "test_file_name.txt";
    final String secondMetadataMimeType = "text/plain";
    final MultipartBodyBuilder secondStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    secondStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBodyBuilder.part("mimeType", secondMetadataMimeType);
    secondStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(secondMetadataInputStream) {

          @Override
          public long contentLength() {
            return secondMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return secondMetadataFileName;
          }
        });
    secondStoreMetadataRequestBodyBuilder.part("fileName", secondMetadataFileName);
    final HttpHeaders secondStoreMetadataRequestHttpHeaders = new HttpHeaders();
    secondStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the second product
    restTemplate.put(
        secondLocation + "/cst",
        new HttpEntity<>(
            secondStoreMetadataRequestBodyBuilder.build(), secondStoreMetadataRequestHttpHeaders));

    // and store third product
    final InputStream thirdInputStream =
        IOUtils.toInputStream("third product contents", StandardCharsets.UTF_8);
    final long thirdFileSize = (long) thirdInputStream.available();
    final String thirdFileName = "test_file_name.txt";
    final MultipartBodyBuilder thirdMultipartBodyBuilder = new MultipartBodyBuilder();
    thirdMultipartBodyBuilder.part("fileSize", String.valueOf(thirdFileSize));
    thirdMultipartBodyBuilder.part("mimeType", "text/plain");
    thirdMultipartBodyBuilder.part(
        "file",
        new InputStreamResource(thirdInputStream) {

          @Override
          public long contentLength() {
            return thirdFileSize;
          }

          @Override
          public String getFilename() {
            return thirdFileName;
          }
        });
    thirdMultipartBodyBuilder.part("fileName", thirdFileName);
    final HttpHeaders thirdHttpHeaders = new HttpHeaders();
    thirdHttpHeaders.set("Accept-Version", "0.1.0");
    final URI thirdLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(thirdMultipartBodyBuilder.build(), thirdHttpHeaders));

    final String thirdMetadataContents = "{contents:\"third product metadata\"";
    final String thirdMetadataEncoding = "UTF-8";
    final InputStream thirdMetadataInputStream =
        IOUtils.toInputStream(thirdMetadataContents, thirdMetadataEncoding);
    final long thirdMetadataFileSize = (long) thirdMetadataInputStream.available();
    final String thirdMetadataFileName = "test_file_name.txt";
    final String thirdMetadataMimeType = "text/plain";
    final MultipartBodyBuilder thirdStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    thirdStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(thirdMetadataFileSize));
    thirdStoreMetadataRequestBodyBuilder.part("mimeType", thirdMetadataMimeType);
    thirdStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(thirdMetadataInputStream) {

          @Override
          public long contentLength() {
            return thirdMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return thirdMetadataFileName;
          }
        });
    thirdStoreMetadataRequestBodyBuilder.part("fileName", thirdMetadataFileName);
    final HttpHeaders thirdStoreMetadataRequestHttpHeaders = new HttpHeaders();
    thirdStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the third product
    restTemplate.put(
        thirdLocation + "/cst",
        new HttpEntity<>(
            thirdStoreMetadataRequestBodyBuilder.build(), thirdStoreMetadataRequestHttpHeaders));

    // verify
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "contents");
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        contains(firstLocation.toString(), secondLocation.toString(), thirdLocation.toString()));
  }

  @Test
  public void testQueryEmptySearchResults() throws Exception {
    // given
    // and store first product
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultipartBodyBuilder firstMultipartBodyBuilder = new MultipartBodyBuilder();
    firstMultipartBodyBuilder.part("fileSize", String.valueOf(firstFileSize));
    firstMultipartBodyBuilder.part("mimeType", "text/plain");
    firstMultipartBodyBuilder.part(
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
    firstMultipartBodyBuilder.part("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstMultipartBodyBuilder.build(), firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultipartBodyBuilder firstStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    firstStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBodyBuilder.part("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(firstMetadataInputStream) {

          @Override
          public long contentLength() {
            return firstMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return firstMetadataFileName;
          }
        });
    firstStoreMetadataRequestBodyBuilder.part("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(
            firstStoreMetadataRequestBodyBuilder.build(), firstStoreMetadataRequestHttpHeaders));

    // and store second product
    final InputStream secondInputStream =
        IOUtils.toInputStream("second product contents", StandardCharsets.UTF_8);
    final long secondFileSize = (long) secondInputStream.available();
    final String secondFileName = "test_file_name.txt";
    final MultipartBodyBuilder secondMultipartBodyBuilder = new MultipartBodyBuilder();
    secondMultipartBodyBuilder.part("fileSize", String.valueOf(secondFileSize));
    secondMultipartBodyBuilder.part("mimeType", "text/plain");
    secondMultipartBodyBuilder.part(
        "file",
        new InputStreamResource(secondInputStream) {

          @Override
          public long contentLength() {
            return secondFileSize;
          }

          @Override
          public String getFilename() {
            return secondFileName;
          }
        });
    secondMultipartBodyBuilder.part("fileName", secondFileName);
    final HttpHeaders secondHttpHeaders = new HttpHeaders();
    secondHttpHeaders.set("Accept-Version", "0.1.0");
    final URI secondLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(secondMultipartBodyBuilder.build(), secondHttpHeaders));

    final String secondMetadataContents = "{contents:\"second product metadata\"";
    final String secondMetadataEncoding = "UTF-8";
    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(secondMetadataContents, secondMetadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final String secondMetadataFileName = "test_file_name.txt";
    final String secondMetadataMimeType = "text/plain";
    final MultipartBodyBuilder secondStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    secondStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBodyBuilder.part("mimeType", secondMetadataMimeType);
    secondStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(secondMetadataInputStream) {

          @Override
          public long contentLength() {
            return secondMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return secondMetadataFileName;
          }
        });
    secondStoreMetadataRequestBodyBuilder.part("fileName", secondMetadataFileName);
    final HttpHeaders secondStoreMetadataRequestHttpHeaders = new HttpHeaders();
    secondStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        secondLocation + "/cst",
        new HttpEntity<>(
            secondStoreMetadataRequestBodyBuilder.build(), secondStoreMetadataRequestHttpHeaders));

    // and store third product
    final InputStream thirdInputStream =
        IOUtils.toInputStream("third product contents", StandardCharsets.UTF_8);
    final long thirdFileSize = (long) thirdInputStream.available();
    final String thirdFileName = "test_file_name.txt";
    final MultipartBodyBuilder thirdMultipartBodyBuilder = new MultipartBodyBuilder();
    thirdMultipartBodyBuilder.part("fileSize", String.valueOf(thirdFileSize));
    thirdMultipartBodyBuilder.part("mimeType", "text/plain");
    thirdMultipartBodyBuilder.part(
        "file",
        new InputStreamResource(thirdInputStream) {

          @Override
          public long contentLength() {
            return thirdFileSize;
          }

          @Override
          public String getFilename() {
            return thirdFileName;
          }
        });
    thirdMultipartBodyBuilder.part("fileName", thirdFileName);
    final HttpHeaders thirdHttpHeaders = new HttpHeaders();
    thirdHttpHeaders.set("Accept-Version", "0.1.0");
    final URI thirdLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(thirdMultipartBodyBuilder.build(), thirdHttpHeaders));

    final String thirdMetadataContents = "{contents:\"third product metadata\"";
    final String thirdMetadataEncoding = "UTF-8";
    final InputStream thirdMetadataInputStream =
        IOUtils.toInputStream(thirdMetadataContents, thirdMetadataEncoding);
    final long thirdMetadataFileSize = (long) thirdMetadataInputStream.available();
    final String thirdMetadataFileName = "test_file_name.txt";
    final String thirdMetadataMimeType = "text/plain";
    final MultipartBodyBuilder thirdStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    thirdStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(thirdMetadataFileSize));
    thirdStoreMetadataRequestBodyBuilder.part("mimeType", thirdMetadataMimeType);
    thirdStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(thirdMetadataInputStream) {

          @Override
          public long contentLength() {
            return thirdMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return thirdMetadataFileName;
          }
        });
    thirdStoreMetadataRequestBodyBuilder.part("fileName", thirdMetadataFileName);
    final HttpHeaders thirdStoreMetadataRequestHttpHeaders = new HttpHeaders();
    thirdStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        thirdLocation + "/cst",
        new HttpEntity<>(
            thirdStoreMetadataRequestBodyBuilder.build(), thirdStoreMetadataRequestHttpHeaders));

    // verify
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "this keyword doesn't match any product");
    // TODO confirm that doesn't contain the 3 locations
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class), is(empty()));
  }

  @Test
  public void testQueryMultipleResults() throws Exception {
    // given
    // and store first product
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultipartBodyBuilder firstMultipartBodyBuilder = new MultipartBodyBuilder();
    firstMultipartBodyBuilder.part("fileSize", String.valueOf(firstFileSize));
    firstMultipartBodyBuilder.part("mimeType", "text/plain");
    firstMultipartBodyBuilder.part(
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
    firstMultipartBodyBuilder.part("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstMultipartBodyBuilder.build(), firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultipartBodyBuilder firstStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    firstStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBodyBuilder.part("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(firstMetadataInputStream) {

          @Override
          public long contentLength() {
            return firstMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return firstMetadataFileName;
          }
        });
    firstStoreMetadataRequestBodyBuilder.part("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(
            firstStoreMetadataRequestBodyBuilder.build(), firstStoreMetadataRequestHttpHeaders));

    // and store second product
    final InputStream secondInputStream =
        IOUtils.toInputStream("second product contents", StandardCharsets.UTF_8);
    final long secondFileSize = (long) secondInputStream.available();
    final String secondFileName = "test_file_name.txt";
    final MultipartBodyBuilder secondMultipartBodyBuilder = new MultipartBodyBuilder();
    secondMultipartBodyBuilder.part("fileSize", String.valueOf(secondFileSize));
    secondMultipartBodyBuilder.part("mimeType", "text/plain");
    secondMultipartBodyBuilder.part(
        "file",
        new InputStreamResource(secondInputStream) {

          @Override
          public long contentLength() {
            return secondFileSize;
          }

          @Override
          public String getFilename() {
            return secondFileName;
          }
        });
    secondMultipartBodyBuilder.part("fileName", secondFileName);
    final HttpHeaders secondHttpHeaders = new HttpHeaders();
    secondHttpHeaders.set("Accept-Version", "0.1.0");
    final URI secondLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(secondMultipartBodyBuilder.build(), secondHttpHeaders));

    final String secondMetadataContents = "{contents:\"second product metadata\"";
    final String secondMetadataEncoding = "UTF-8";
    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(secondMetadataContents, secondMetadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final String secondMetadataFileName = "test_file_name.txt";
    final String secondMetadataMimeType = "text/plain";
    final MultipartBodyBuilder secondStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    secondStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBodyBuilder.part("mimeType", secondMetadataMimeType);
    secondStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(secondMetadataInputStream) {

          @Override
          public long contentLength() {
            return secondMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return secondMetadataFileName;
          }
        });
    secondStoreMetadataRequestBodyBuilder.part("fileName", secondMetadataFileName);
    final HttpHeaders secondStoreMetadataRequestHttpHeaders = new HttpHeaders();
    secondStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        secondLocation + "/cst",
        new HttpEntity<>(
            secondStoreMetadataRequestBodyBuilder.build(), secondStoreMetadataRequestHttpHeaders));

    // and store third product
    final InputStream thirdInputStream =
        IOUtils.toInputStream("third product contents", StandardCharsets.UTF_8);
    final long thirdFileSize = (long) thirdInputStream.available();
    final String thirdFileName = "test_file_name.txt";
    final MultipartBodyBuilder thirdMultipartBodyBuilder = new MultipartBodyBuilder();
    thirdMultipartBodyBuilder.part("fileSize", String.valueOf(thirdFileSize));
    thirdMultipartBodyBuilder.part("mimeType", "text/plain");
    thirdMultipartBodyBuilder.part(
        "file",
        new InputStreamResource(thirdInputStream) {

          @Override
          public long contentLength() {
            return thirdFileSize;
          }

          @Override
          public String getFilename() {
            return thirdFileName;
          }
        });
    thirdMultipartBodyBuilder.part("fileName", thirdFileName);
    final HttpHeaders thirdHttpHeaders = new HttpHeaders();
    thirdHttpHeaders.set("Accept-Version", "0.1.0");
    final URI thirdLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(thirdMultipartBodyBuilder.build(), thirdHttpHeaders));

    final String thirdMetadataContents = "{contents:\"third product metadata\"";
    final String thirdMetadataEncoding = "UTF-8";
    final InputStream thirdMetadataInputStream =
        IOUtils.toInputStream(thirdMetadataContents, thirdMetadataEncoding);
    final long thirdMetadataFileSize = (long) thirdMetadataInputStream.available();
    final String thirdMetadataFileName = "test_file_name.txt";
    final String thirdMetadataMimeType = "text/plain";
    final MultipartBodyBuilder thirdStoreMetadataRequestBodyBuilder = new MultipartBodyBuilder();
    thirdStoreMetadataRequestBodyBuilder.part("fileSize", String.valueOf(thirdMetadataFileSize));
    thirdStoreMetadataRequestBodyBuilder.part("mimeType", thirdMetadataMimeType);
    thirdStoreMetadataRequestBodyBuilder.part(
        "file",
        new InputStreamResource(thirdMetadataInputStream) {

          @Override
          public long contentLength() {
            return thirdMetadataFileSize;
          }

          @Override
          public String getFilename() {
            return thirdMetadataFileName;
          }
        });
    thirdStoreMetadataRequestBodyBuilder.part("fileName", thirdMetadataFileName);
    final HttpHeaders thirdStoreMetadataRequestHttpHeaders = new HttpHeaders();
    thirdStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        thirdLocation + "/cst",
        new HttpEntity<>(
            thirdStoreMetadataRequestBodyBuilder.build(), thirdStoreMetadataRequestHttpHeaders));

    // verify
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "first");
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        contains(firstLocation.toString()));
  }

  @Test
  public void testQueryWhenSolrIsEmpty() throws Exception {
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "nothing is in solr so this won't match anything");
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class), is(empty()));
  }

  private static void emptySolrCore(final SolrClient client, final String collection)
      throws Exception {
    client.deleteByQuery(collection, "*:*");
  }
}
