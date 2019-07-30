/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.AmazonS3ExceptionBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import javax.inject.Inject;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

/**
 * This class contains tests for the retrieve endpoint that use a mocked {@link AmazonS3} and {@link
 * SolrClient}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@MockBean(SolrClient.class)
public class RetrieveProductTests {

  @MockBean private AmazonS3 mockAmazonS3;

  @Inject private MockMvc mockMvc;

  @Value("${aws.s3.bucket.quarantine}")
  private String s3Bucket;

  @After
  public void after() {
    verifyNoMoreInteractions(ignoreStubs(mockAmazonS3));
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testBadRequest() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/    "))
        .andExpect(status().isBadRequest());
  }

  /**
   * @see S3Tests#testRetrieveProductIdNotFound()
   * @see S3Tests#testRetrieveProductWhenS3IsEmpty()
   */
  @Test
  public void testS3KeyDoesNotExist() throws Exception {
    final String key = "1234";
    final AmazonS3ExceptionBuilder amazonS3ExceptionBuilder = new AmazonS3ExceptionBuilder();
    amazonS3ExceptionBuilder.setErrorCode("NoSuchKey");
    amazonS3ExceptionBuilder.setErrorMessage("The specified key does not exist.");
    amazonS3ExceptionBuilder.setStatusCode(404);
    amazonS3ExceptionBuilder.addAdditionalDetail("BucketName", s3Bucket);
    amazonS3ExceptionBuilder.addAdditionalDetail("Resource", "/" + s3Bucket + "/" + key);
    amazonS3ExceptionBuilder.addAdditionalDetail("Key", key);
    when(mockAmazonS3.getObject(
            argThat(
                getObjectRequest ->
                    StringUtils.equals(getObjectRequest.getBucketName(), s3Bucket)
                        && StringUtils.equals(getObjectRequest.getKey(), key))))
        .thenThrow(amazonS3ExceptionBuilder.build());

    // TODO return 404 if key doesn't exist
    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/" + key))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testS3BucketDoesNotExist() throws Exception {
    final String key = "1234";
    final AmazonS3ExceptionBuilder amazonS3ExceptionBuilder = new AmazonS3ExceptionBuilder();
    amazonS3ExceptionBuilder.setErrorCode("NoSuchBucket");
    amazonS3ExceptionBuilder.setErrorMessage("The specified bucket does not exist");
    amazonS3ExceptionBuilder.setStatusCode(404);
    amazonS3ExceptionBuilder.addAdditionalDetail("BucketName", s3Bucket);
    amazonS3ExceptionBuilder.addAdditionalDetail("Resource", "/" + s3Bucket + "/" + key);
    amazonS3ExceptionBuilder.addAdditionalDetail("Key", key);
    when(mockAmazonS3.getObject(
            argThat(
                getObjectRequest ->
                    StringUtils.equals(getObjectRequest.getBucketName(), s3Bucket))))
        .thenThrow(amazonS3ExceptionBuilder.build());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/" + key))
        .andExpect(status().isInternalServerError());
  }

  /** @see AmazonS3#getObject(GetObjectRequest) */
  @Test
  public void testS3ClientError() throws Exception {
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenThrow(SdkClientException.class);

    // TODO return 404 if key doesn't exist
    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/1234"))
        .andExpect(status().isInternalServerError());
  }

  /** @see AmazonS3#getObject(GetObjectRequest) */
  @Test
  public void testS3ServiceError() throws Exception {
    when(mockAmazonS3.getObject(any(GetObjectRequest.class)))
        .thenThrow(AmazonServiceException.class);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/1234"))
        .andExpect(status().isInternalServerError());
  }

  /** @see AmazonS3#getObject(GetObjectRequest) */
  @Test
  public void testS3ConstraintsWerentMet() throws Exception {
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(null);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/1234"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testS3ThrowsRuntimeException() throws Exception {
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenThrow(RuntimeException.class);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/1234"))
        .andExpect(status().isInternalServerError());
  }
}
