/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static com.connexta.multiintstore.models.IndexedProductMetadata.SOLR_COLLECTION;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.connexta.multiintstore.config.SolrClientConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * This class contains tests for the store metadata endpoint and query endpoint that use a mocked
 * {@link AmazonS3}.
 */
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

  @TestConfiguration
  static class Config {

    @Bean
    public SolrClientConfiguration testSolrClientConfiguration() {
      return new SolrClientConfiguration(
          solrContainer.getContainerIpAddress(), solrContainer.getMappedPort(SOLR_PORT));
    }
  }

  @MockBean AmazonS3 mockAmazonS3;

  @Inject private TestRestTemplate restTemplate;
  @Inject private SolrClient solrClient;

  @Value("${aws.s3.bucket.quarantine}")
  private String s3Bucket;

  @Value("${endpointUrl.retrieve}")
  private String endpointUrlRetrieve;

  @Before
  public void before() throws IOException, SolrServerException {
    when(mockAmazonS3.putObject(any(PutObjectRequest.class)))
        .thenReturn(mock(PutObjectResult.class));

    solrClient.deleteByQuery(SOLR_COLLECTION, "*");
    solrClient.commit(SOLR_COLLECTION);
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testStoreMetadataCstWhenSolrIsEmpty() throws Exception {
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(Boolean.TRUE);

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
    final MultiValueMap<String, Object> storeProductRequestBody = new LinkedMultiValueMap<>();
    storeProductRequestBody.add("fileSize", String.valueOf(productFileSize));
    storeProductRequestBody.add("mimeType", productMimeType);
    storeProductRequestBody.add(
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
    storeProductRequestBody.add("fileName", productFileName);
    final HttpHeaders storeProductRequestHttpHeaders = new HttpHeaders();
    storeProductRequestHttpHeaders.set("Accept-Version", "0.1.0");

    final URI productLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(storeProductRequestBody, storeProductRequestHttpHeaders));

    final String metadataContents = "{contents:\"" + productContents + "\"";
    final String metadataEncoding = "UTF-8";
    final InputStream metadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long metadataFileSize = (long) metadataInputStream.available();
    final String metadataFileName = "test_file_name.txt";
    final String metadataMimeType = "text/plain";
    final MultiValueMap<String, Object> storeMetadataRequestBody = new LinkedMultiValueMap<>();
    storeMetadataRequestBody.add("fileSize", String.valueOf(metadataFileSize));
    storeMetadataRequestBody.add("mimeType", metadataMimeType);
    storeMetadataRequestBody.add(
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
    storeMetadataRequestBody.add("fileName", metadataFileName);
    final HttpHeaders storeMetadataRequestHttpHeaders = new HttpHeaders();
    storeMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // when
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(storeMetadataRequestBody, storeMetadataRequestHttpHeaders));

    // then
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", queryKeyword);
    assertThat(
        (List<String>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        hasItem(productLocation.toString()));
  }

  @Test
  public void testStoreMetadataCstWhenSolrIsNotEmpty() throws Exception {
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(Boolean.TRUE);

    // given
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> firstStoreProductRequestBody = new LinkedMultiValueMap<>();
    firstStoreProductRequestBody.add("fileSize", String.valueOf(firstFileSize));
    firstStoreProductRequestBody.add("mimeType", "text/plain");
    firstStoreProductRequestBody.add(
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
    firstStoreProductRequestBody.add("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstStoreProductRequestBody, firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> firstStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    firstStoreMetadataRequestBody.add("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBody.add("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBody.add(
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
    firstStoreMetadataRequestBody.add("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for an initial product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(firstStoreMetadataRequestBody, firstStoreMetadataRequestHttpHeaders));

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
    final MultiValueMap<String, Object> storeProductRequestBody = new LinkedMultiValueMap<>();
    storeProductRequestBody.add("fileSize", String.valueOf(productFileSize));
    storeProductRequestBody.add("mimeType", productMimeType);
    storeProductRequestBody.add(
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
    storeProductRequestBody.add("fileName", productFileName);
    final HttpHeaders storeProductRequestHttpHeaders = new HttpHeaders();
    storeProductRequestHttpHeaders.set("Accept-Version", "0.1.0");

    final URI productLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(storeProductRequestBody, storeProductRequestHttpHeaders));

    final String metadataContents = "{contents:\"" + productContents + "\"";
    final String metadataEncoding = "UTF-8";
    final InputStream metadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long metadataFileSize = (long) metadataInputStream.available();
    final String metadataFileName = "test_file_name.txt";
    final String metadataMimeType = "text/plain";
    final MultiValueMap<String, Object> storeMetadataRequestBody = new LinkedMultiValueMap<>();
    storeMetadataRequestBody.add("fileSize", String.valueOf(metadataFileSize));
    storeMetadataRequestBody.add("mimeType", metadataMimeType);
    storeMetadataRequestBody.add(
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
    storeMetadataRequestBody.add("fileName", metadataFileName);
    final HttpHeaders storeMetadataRequestHttpHeaders = new HttpHeaders();
    storeMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // when
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(storeMetadataRequestBody, storeMetadataRequestHttpHeaders));

    // then
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", queryKeyword);
    assertThat(
        (List<String>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        hasItem(equalTo(productLocation.toString())));
  }

  @Test
  public void testStoreMetadataProductIdNotFound() throws Exception {
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(Boolean.TRUE);

    // given
    final URI productLocation = new URI("http://localhost:8080/mis/product/12341");
    final String queryKeyword = "Winterfell";
    final String metadataContents =
        "All the color had been leached from "
            + queryKeyword
            + " until only grey and white remained";
    final String metadataEncoding = "UTF-8";
    final InputStream metadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long metadataFileSize = (long) metadataInputStream.available();
    final String metadataFileName = "test_file_name.txt";
    final String metadataMimeType = "text/plain";
    final MultiValueMap<String, Object> storeMetadataRequestBody = new LinkedMultiValueMap<>();
    storeMetadataRequestBody.add("fileSize", String.valueOf(metadataFileSize));
    storeMetadataRequestBody.add("mimeType", metadataMimeType);
    storeMetadataRequestBody.add(
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
    storeMetadataRequestBody.add("fileName", metadataFileName);
    final HttpHeaders storeMetadataRequestHttpHeaders = new HttpHeaders();
    storeMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // when
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(Boolean.FALSE);

    // attempt to store metadata
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(storeMetadataRequestBody, storeMetadataRequestHttpHeaders));

    // query for metadata keyword and verify no search results are returned
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", queryKeyword);
    assertThat(
        (List<String>) restTemplate.getForObject(queryUriBuilder.build(), List.class), empty());
  }

  @Test
  public void testStoreMetadataCstHasAlreadyBeenStored() throws Exception {
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(Boolean.TRUE);

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
    final MultiValueMap<String, Object> storeProductRequestBody = new LinkedMultiValueMap<>();
    storeProductRequestBody.add("fileSize", String.valueOf(productFileSize));
    storeProductRequestBody.add("mimeType", productMimeType);
    storeProductRequestBody.add(
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
    storeProductRequestBody.add("fileName", productFileName);
    final HttpHeaders storeProductRequestHttpHeaders = new HttpHeaders();
    storeProductRequestHttpHeaders.set("Accept-Version", "0.1.0");

    final URI productLocation =
        restTemplate.postForLocation(
            "/mis/product/",
            new HttpEntity<>(storeProductRequestBody, storeProductRequestHttpHeaders));

    final String metadataContents = "{contents:\"" + productContents + "\"";
    final String metadataEncoding = "UTF-8";
    final InputStream metadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long firstMetadataFileSize = (long) metadataInputStream.available();
    final String metadataFileName = "test_file_name.txt";
    final String metadataMimeType = "text/plain";
    final MultiValueMap<String, Object> firstStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    firstStoreMetadataRequestBody.add("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBody.add("mimeType", metadataMimeType);
    firstStoreMetadataRequestBody.add(
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
    firstStoreMetadataRequestBody.add("fileName", metadataFileName);
    final HttpHeaders storeMetadataRequestHttpHeaders = new HttpHeaders();
    storeMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and the cst metadata is stored
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(firstStoreMetadataRequestBody, storeMetadataRequestHttpHeaders));

    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(metadataContents, metadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final MultiValueMap<String, Object> secondStoreMetadataRequestBody =
        new LinkedMultiValueMap<>();
    secondStoreMetadataRequestBody.add("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBody.add("mimeType", metadataMimeType);
    secondStoreMetadataRequestBody.add(
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
    secondStoreMetadataRequestBody.add("fileName", storeMetadataRequestHttpHeaders);

    // when
    // TODO handle when CST has already been stored
    restTemplate.put(
        productLocation + "/cst",
        new HttpEntity<>(secondStoreMetadataRequestBody, storeMetadataRequestHttpHeaders));

    // then query should still work
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", queryKeyword);
    assertThat(
        (List<String>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        hasItem(productLocation.toString()));
  }

  @Test
  public void testQuery() throws Exception {
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(Boolean.TRUE);

    // given
    // and store first product
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> firstStoreProductRequstBody = new LinkedMultiValueMap<>();
    firstStoreProductRequstBody.add("fileSize", String.valueOf(firstFileSize));
    firstStoreProductRequstBody.add("mimeType", "text/plain");
    firstStoreProductRequstBody.add(
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
    firstStoreProductRequstBody.add("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstStoreProductRequstBody, firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> firstStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    firstStoreMetadataRequestBody.add("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBody.add("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBody.add(
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
    firstStoreMetadataRequestBody.add("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the first product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(firstStoreMetadataRequestBody, firstStoreMetadataRequestHttpHeaders));

    // and store second product
    final InputStream secondInputStream =
        IOUtils.toInputStream("second product contents", StandardCharsets.UTF_8);
    final long secondFileSize = (long) secondInputStream.available();
    final String secondFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> secondStoreProductRequestBody = new LinkedMultiValueMap<>();
    secondStoreProductRequestBody.add("fileSize", String.valueOf(secondFileSize));
    secondStoreProductRequestBody.add("mimeType", "text/plain");
    secondStoreProductRequestBody.add(
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
    secondStoreProductRequestBody.add("fileName", secondFileName);
    final HttpHeaders secondHttpHeaders = new HttpHeaders();
    secondHttpHeaders.set("Accept-Version", "0.1.0");
    final URI secondLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(secondStoreProductRequestBody, secondHttpHeaders));

    final String secondMetadataContents = "{contents:\"second product metadata\"";
    final String secondMetadataEncoding = "UTF-8";
    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(secondMetadataContents, secondMetadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final String secondMetadataFileName = "test_file_name.txt";
    final String secondMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> secondStoreMetadataRequestBody =
        new LinkedMultiValueMap<>();
    secondStoreMetadataRequestBody.add("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBody.add("mimeType", secondMetadataMimeType);
    secondStoreMetadataRequestBody.add(
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
    secondStoreMetadataRequestBody.add("fileName", secondMetadataFileName);
    final HttpHeaders secondStoreMetadataRequestHttpHeaders = new HttpHeaders();
    secondStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the second product
    restTemplate.put(
        secondLocation + "/cst",
        new HttpEntity<>(secondStoreMetadataRequestBody, secondStoreMetadataRequestHttpHeaders));

    // and store third product
    final InputStream thirdInputStream =
        IOUtils.toInputStream("third product contents", StandardCharsets.UTF_8);
    final long thirdFileSize = (long) thirdInputStream.available();
    final String thirdFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> thirdStoreProductRequestBody = new LinkedMultiValueMap<>();
    thirdStoreProductRequestBody.add("fileSize", String.valueOf(thirdFileSize));
    thirdStoreProductRequestBody.add("mimeType", "text/plain");
    thirdStoreProductRequestBody.add(
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
    thirdStoreProductRequestBody.add("fileName", thirdFileName);
    final HttpHeaders thirdHttpHeaders = new HttpHeaders();
    thirdHttpHeaders.set("Accept-Version", "0.1.0");
    final URI thirdLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(thirdStoreProductRequestBody, thirdHttpHeaders));

    final String thirdMetadataContents = "{contents:\"third product metadata\"";
    final String thirdMetadataEncoding = "UTF-8";
    final InputStream thirdMetadataInputStream =
        IOUtils.toInputStream(thirdMetadataContents, thirdMetadataEncoding);
    final long thirdMetadataFileSize = (long) thirdMetadataInputStream.available();
    final String thirdMetadataFileName = "test_file_name.txt";
    final String thirdMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> thirdStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    thirdStoreMetadataRequestBody.add("fileSize", String.valueOf(thirdMetadataFileSize));
    thirdStoreMetadataRequestBody.add("mimeType", thirdMetadataMimeType);
    thirdStoreMetadataRequestBody.add(
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
    thirdStoreMetadataRequestBody.add("fileName", thirdMetadataFileName);
    final HttpHeaders thirdStoreMetadataRequestHttpHeaders = new HttpHeaders();
    thirdStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the third product
    restTemplate.put(
        thirdLocation + "/cst",
        new HttpEntity<>(thirdStoreMetadataRequestBody, thirdStoreMetadataRequestHttpHeaders));

    // verify
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "contents");
    assertThat(
        (List<String>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        hasItems(firstLocation.toString(), secondLocation.toString(), thirdLocation.toString()));
  }

  @Test
  public void testQueryEmptySearchResults() throws Exception {
    // given
    // and store first product
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> firstStoreProductRequestBody = new LinkedMultiValueMap<>();
    firstStoreProductRequestBody.add("fileSize", String.valueOf(firstFileSize));
    firstStoreProductRequestBody.add("mimeType", "text/plain");
    firstStoreProductRequestBody.add(
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
    firstStoreProductRequestBody.add("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstStoreProductRequestBody, firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> firstStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    firstStoreMetadataRequestBody.add("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBody.add("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBody.add(
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
    firstStoreMetadataRequestBody.add("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the first product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(firstStoreMetadataRequestBody, firstStoreMetadataRequestHttpHeaders));

    // and store second product
    final InputStream secondInputStream =
        IOUtils.toInputStream("second product contents", StandardCharsets.UTF_8);
    final long secondFileSize = (long) secondInputStream.available();
    final String secondFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> secondStoreProductRequestBody = new LinkedMultiValueMap<>();
    secondStoreProductRequestBody.add("fileSize", String.valueOf(secondFileSize));
    secondStoreProductRequestBody.add("mimeType", "text/plain");
    secondStoreProductRequestBody.add(
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
    secondStoreProductRequestBody.add("fileName", secondFileName);
    final HttpHeaders secondHttpHeaders = new HttpHeaders();
    secondHttpHeaders.set("Accept-Version", "0.1.0");
    final URI secondLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(secondStoreProductRequestBody, secondHttpHeaders));

    final String secondMetadataContents = "{contents:\"second product metadata\"";
    final String secondMetadataEncoding = "UTF-8";
    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(secondMetadataContents, secondMetadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final String secondMetadataFileName = "test_file_name.txt";
    final String secondMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> secondStoreMetadataRequestBody =
        new LinkedMultiValueMap<>();
    secondStoreMetadataRequestBody.add("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBody.add("mimeType", secondMetadataMimeType);
    secondStoreMetadataRequestBody.add(
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
    secondStoreMetadataRequestBody.add("fileName", secondMetadataFileName);
    final HttpHeaders secondStoreMetadataRequestHttpHeaders = new HttpHeaders();
    secondStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the second product
    restTemplate.put(
        secondLocation + "/cst",
        new HttpEntity<>(secondStoreMetadataRequestBody, secondStoreMetadataRequestHttpHeaders));

    // and store third product
    final InputStream thirdInputStream =
        IOUtils.toInputStream("third product contents", StandardCharsets.UTF_8);
    final long thirdFileSize = (long) thirdInputStream.available();
    final String thirdFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> thirdStoreProductRequestBody = new LinkedMultiValueMap<>();
    thirdStoreProductRequestBody.add("fileSize", String.valueOf(thirdFileSize));
    thirdStoreProductRequestBody.add("mimeType", "text/plain");
    thirdStoreProductRequestBody.add(
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
    thirdStoreProductRequestBody.add("fileName", thirdFileName);
    final HttpHeaders thirdHttpHeaders = new HttpHeaders();
    thirdHttpHeaders.set("Accept-Version", "0.1.0");
    final URI thirdLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(thirdStoreProductRequestBody, thirdHttpHeaders));

    final String thirdMetadataContents = "{contents:\"third product metadata\"";
    final String thirdMetadataEncoding = "UTF-8";
    final InputStream thirdMetadataInputStream =
        IOUtils.toInputStream(thirdMetadataContents, thirdMetadataEncoding);
    final long thirdMetadataFileSize = (long) thirdMetadataInputStream.available();
    final String thirdMetadataFileName = "test_file_name.txt";
    final String thirdMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> thirdStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    thirdStoreMetadataRequestBody.add("fileSize", String.valueOf(thirdMetadataFileSize));
    thirdStoreMetadataRequestBody.add("mimeType", thirdMetadataMimeType);
    thirdStoreMetadataRequestBody.add(
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
    thirdStoreMetadataRequestBody.add("fileName", thirdMetadataFileName);
    final HttpHeaders thirdStoreMetadataRequestHttpHeaders = new HttpHeaders();
    thirdStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the third product
    restTemplate.put(
        thirdLocation + "/cst",
        new HttpEntity<>(thirdStoreMetadataRequestBody, thirdStoreMetadataRequestHttpHeaders));

    // verify
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "this keyword doesn't match any product");
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        allOf(
            not(hasItem(firstLocation)),
            not(hasItem(secondLocation)),
            not(hasItem(thirdLocation))));
  }

  @Test
  public void testQueryMultipleResults() throws Exception {
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(Boolean.TRUE);

    // given
    // and store first product
    final InputStream firstInputStream =
        IOUtils.toInputStream("first product contents", StandardCharsets.UTF_8);
    final long firstFileSize = (long) firstInputStream.available();
    final String firstFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> firstStoreProductRequestBody = new LinkedMultiValueMap<>();
    firstStoreProductRequestBody.add("fileSize", String.valueOf(firstFileSize));
    firstStoreProductRequestBody.add("mimeType", "text/plain");
    firstStoreProductRequestBody.add(
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
    firstStoreProductRequestBody.add("fileName", firstFileName);
    final HttpHeaders firstHttpHeaders = new HttpHeaders();
    firstHttpHeaders.set("Accept-Version", "0.1.0");
    final URI firstLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(firstStoreProductRequestBody, firstHttpHeaders));

    final String firstMetadataContents = "{contents:\"first product metadata\"";
    final String firstMetadataEncoding = "UTF-8";
    final InputStream firstMetadataInputStream =
        IOUtils.toInputStream(firstMetadataContents, firstMetadataEncoding);
    final long firstMetadataFileSize = (long) firstMetadataInputStream.available();
    final String firstMetadataFileName = "test_file_name.txt";
    final String firstMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> firstStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    firstStoreMetadataRequestBody.add("fileSize", String.valueOf(firstMetadataFileSize));
    firstStoreMetadataRequestBody.add("mimeType", firstMetadataMimeType);
    firstStoreMetadataRequestBody.add(
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
    firstStoreMetadataRequestBody.add("fileName", firstMetadataFileName);
    final HttpHeaders firstStoreMetadataRequestHttpHeaders = new HttpHeaders();
    firstStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored the first product
    restTemplate.put(
        firstLocation + "/cst",
        new HttpEntity<>(firstStoreMetadataRequestBody, firstStoreMetadataRequestHttpHeaders));

    // and store second product
    final InputStream secondInputStream =
        IOUtils.toInputStream("second product contents", StandardCharsets.UTF_8);
    final long secondFileSize = (long) secondInputStream.available();
    final String secondFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> secondStoreProductRequestBody = new LinkedMultiValueMap<>();
    secondStoreProductRequestBody.add("fileSize", String.valueOf(secondFileSize));
    secondStoreProductRequestBody.add("mimeType", "text/plain");
    secondStoreProductRequestBody.add(
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
    secondStoreProductRequestBody.add("fileName", secondFileName);
    final HttpHeaders secondHttpHeaders = new HttpHeaders();
    secondHttpHeaders.set("Accept-Version", "0.1.0");
    final URI secondLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(secondStoreProductRequestBody, secondHttpHeaders));

    final String secondMetadataContents = "{contents:\"second product metadata\"";
    final String secondMetadataEncoding = "UTF-8";
    final InputStream secondMetadataInputStream =
        IOUtils.toInputStream(secondMetadataContents, secondMetadataEncoding);
    final long secondMetadataFileSize = (long) secondMetadataInputStream.available();
    final String secondMetadataFileName = "test_file_name.txt";
    final String secondMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> secondStoreMetadataRequestBody =
        new LinkedMultiValueMap<>();
    secondStoreMetadataRequestBody.add("fileSize", String.valueOf(secondMetadataFileSize));
    secondStoreMetadataRequestBody.add("mimeType", secondMetadataMimeType);
    secondStoreMetadataRequestBody.add(
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
    secondStoreMetadataRequestBody.add("fileName", secondMetadataFileName);
    final HttpHeaders secondStoreMetadataRequestHttpHeaders = new HttpHeaders();
    secondStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored for the second product
    restTemplate.put(
        secondLocation + "/cst",
        new HttpEntity<>(secondStoreMetadataRequestBody, secondStoreMetadataRequestHttpHeaders));

    // and store third product
    final InputStream thirdInputStream =
        IOUtils.toInputStream("third product contents", StandardCharsets.UTF_8);
    final long thirdFileSize = (long) thirdInputStream.available();
    final String thirdFileName = "test_file_name.txt";
    final MultiValueMap<String, Object> thirdStoreProductRequestBody = new LinkedMultiValueMap<>();
    thirdStoreProductRequestBody.add("fileSize", String.valueOf(thirdFileSize));
    thirdStoreProductRequestBody.add("mimeType", "text/plain");
    thirdStoreProductRequestBody.add(
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
    thirdStoreProductRequestBody.add("fileName", thirdFileName);
    final HttpHeaders thirdHttpHeaders = new HttpHeaders();
    thirdHttpHeaders.set("Accept-Version", "0.1.0");
    final URI thirdLocation =
        restTemplate.postForLocation(
            "/mis/product/", new HttpEntity<>(thirdStoreProductRequestBody, thirdHttpHeaders));

    final String thirdMetadataContents = "{contents:\"third product metadata\"";
    final String thirdMetadataEncoding = "UTF-8";
    final InputStream thirdMetadataInputStream =
        IOUtils.toInputStream(thirdMetadataContents, thirdMetadataEncoding);
    final long thirdMetadataFileSize = (long) thirdMetadataInputStream.available();
    final String thirdMetadataFileName = "test_file_name.txt";
    final String thirdMetadataMimeType = "text/plain";
    final MultiValueMap<String, Object> thirdStoreMetadataRequestBody = new LinkedMultiValueMap<>();
    thirdStoreMetadataRequestBody.add("fileSize", String.valueOf(thirdMetadataFileSize));
    thirdStoreMetadataRequestBody.add("mimeType", thirdMetadataMimeType);
    thirdStoreMetadataRequestBody.add(
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
    thirdStoreMetadataRequestBody.add("fileName", thirdMetadataFileName);
    final HttpHeaders thirdStoreMetadataRequestHttpHeaders = new HttpHeaders();
    thirdStoreMetadataRequestHttpHeaders.set("Accept-Version", "0.1.0");

    // and metadata is stored the third product
    restTemplate.put(
        thirdLocation + "/cst",
        new HttpEntity<>(thirdStoreMetadataRequestBody, thirdStoreMetadataRequestHttpHeaders));

    // verify
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "first");
    assertThat(
        (List<String>) restTemplate.getForObject(queryUriBuilder.build(), List.class),
        hasItem(firstLocation.toString()));
  }

  @Test
  public void testQueryWhenSolrIsEmpty() throws Exception {
    final URIBuilder queryUriBuilder = new URIBuilder();
    queryUriBuilder.setPath("/search");
    queryUriBuilder.setParameter("q", "nothing is in solr so this won't match anything");
    assertThat(
        (List<URI>) restTemplate.getForObject(queryUriBuilder.build(), List.class), is(empty()));
  }
}
