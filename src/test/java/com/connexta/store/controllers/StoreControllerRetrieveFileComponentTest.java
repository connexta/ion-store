/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static com.connexta.store.controllers.StoreController.RETRIEVE_FILE_URL_TEMPLATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.connexta.store.StoreITests;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TODO Use {@link org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest} or {@link
 * org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest} instead.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class StoreControllerRetrieveFileComponentTest {

  private static final String DATASET_ID = "341d6c1ce5e0403a99fe86edaed66eea";

  @MockBean private AmazonS3 mockAmazonS3;

  @Inject private MockMvc mockMvc;

  @Value("${s3.bucket.file}")
  private String fileBucket;

  @AfterEach
  public void after() {
    verifyNoMoreInteractions(ignoreStubs(mockAmazonS3));
  }

  @Test
  @SuppressWarnings("squid:S2699") // Test case missing assertion
  public void testContextLoads() {}

  @ParameterizedTest(name = "400 Bad Request if ID is {0}")
  @ValueSource(
      strings = {"    ", "1234567890123456789012345678901234", "+0067360b70e4acfab561fe593ad3f7a"})
  void testBadIds(final String datasetId) throws Exception {
    mockMvc.perform(get(RETRIEVE_FILE_URL_TEMPLATE, datasetId)).andExpect(status().isBadRequest());
  }

  @Test
  public void testS3BucketDoesNotExist() throws Exception {
    final String key = DATASET_ID;
    when(mockAmazonS3.doesBucketExistV2(fileBucket)).thenReturn(false);
    assertErrorResponse();
  }

  /**
   * @see StoreITests#testRetrieveFileNotFound()
   * @see StoreITests#testRetrieveFileWhenS3IsEmpty()
   */
  @Test
  public void testS3KeyDoesNotExist() throws Exception {
    final String key = DATASET_ID;
    when(mockAmazonS3.doesBucketExistV2(fileBucket)).thenReturn(true);
    when(mockAmazonS3.doesObjectExist(fileBucket, key)).thenReturn(false);

    mockMvc
        .perform(get(RETRIEVE_FILE_URL_TEMPLATE, key).header("Accept-Version", "'0.1.0"))
        .andExpect(status().isNotFound());
  }

  /** @see AmazonS3#getObject(GetObjectRequest) */
  @Test
  public void testS3ConstraintsWerentMet() throws Exception {
    when(mockAmazonS3.doesBucketExistV2(fileBucket)).thenReturn(true);
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(null);
    assertErrorResponse();
  }

  @ParameterizedTest
  @ValueSource(classes = {SdkClientException.class, AmazonServiceException.class})
  public void testS3ThrowableTypes(final Class<? extends Throwable> throwableType)
      throws Exception {
    when(mockAmazonS3.doesBucketExistV2(fileBucket)).thenReturn(true);
    when(mockAmazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenThrow(throwableType);
    assertErrorResponse();
  }

  private void assertErrorResponse() throws Exception {
    mockMvc
        .perform(get(RETRIEVE_FILE_URL_TEMPLATE, DATASET_ID))
        .andExpect(status().isInternalServerError());
  }
}
