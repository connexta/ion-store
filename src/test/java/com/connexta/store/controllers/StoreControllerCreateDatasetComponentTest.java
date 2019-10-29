/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static com.connexta.store.controllers.StoreController.CREATE_DATASET_URL_TEMPLATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TODO Use {@link org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest} or {@link
 * org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest} instead.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class StoreControllerCreateDatasetComponentTest {

  @MockBean private AmazonS3 mockAmazonS3;

  @Inject private MockMvc mockMvc;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @Value("${s3.bucket.file}")
  private String fileBucket;

  @AfterEach
  public void after() {
    verifyNoMoreInteractions(ignoreStubs(mockAmazonS3));
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testMissingFile() throws Exception {
    mockMvc
        .perform(
            multipart(CREATE_DATASET_URL_TEMPLATE)
                .header(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testBadAcceptVersion() throws Exception {
    mockMvc
        .perform(
            multipart(CREATE_DATASET_URL_TEMPLATE)
                .file(
                    new MockMultipartFile(
                        "file",
                        "test_file_name.txt",
                        "text/plain",
                        IOUtils.toInputStream(
                            "All the color had been leached from Winterfell until only grey and white remained",
                            StandardCharsets.UTF_8)))
                .header(
                    StoreController.ACCEPT_VERSION_HEADER_NAME,
                    "this accept version is not supported")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isNotImplemented());
  }

  @Test
  public void testS3BucketDoesNotExist() throws Exception {
    when(mockAmazonS3.doesBucketExistV2(fileBucket)).thenReturn(false);
    assertErrorResponse();
  }

  @ParameterizedTest
  @ValueSource(
      classes = {SdkClientException.class, AmazonServiceException.class, RuntimeException.class})
  public void testS3ThrowableTypes(final Class<? extends Throwable> throwableType)
      throws Exception {
    when(mockAmazonS3.doesBucketExistV2(fileBucket)).thenReturn(true);
    when(mockAmazonS3.putObject(any(PutObjectRequest.class))).thenThrow(throwableType);
    assertErrorResponse();
  }

  private void assertErrorResponse() throws Exception {
    mockMvc
        .perform(
            multipart(CREATE_DATASET_URL_TEMPLATE)
                .file(
                    new MockMultipartFile(
                        "file",
                        "test_file_name.txt",
                        "text/plain",
                        IOUtils.toInputStream(
                            "All the color had been leached from Winterfell until only grey and white remained",
                            StandardCharsets.UTF_8)))
                .header(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }
}
