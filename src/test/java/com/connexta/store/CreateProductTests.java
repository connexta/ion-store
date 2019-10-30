/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

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
import com.connexta.store.controllers.StoreController;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
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

@SpringBootTest
@AutoConfigureMockMvc
public class CreateProductTests {

  @MockBean private AmazonS3 mockAmazonS3;

  @Inject private MockMvc mockMvc;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @Value("${s3.bucket}")
  private String s3Bucket;

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
            multipart("/mis/product")
                .header(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testBadAcceptVersion() throws Exception {
    mockMvc
        .perform(
            multipart("/mis/product")
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
  @Disabled("TODO")
  public void testCantReadAttachment() {
    // TODO verify 400
  }

  @Test
  public void testS3BucketDoesNotExist() throws Exception {
    when(mockAmazonS3.doesBucketExistV2(s3Bucket)).thenReturn(false);
    assertErrorResponse();
  }

  @ParameterizedTest
  @ValueSource(
      classes = {SdkClientException.class, AmazonServiceException.class, RuntimeException.class})
  public void testS3ThrowableTypes(final Class<? extends Throwable> throwableType)
      throws Exception {
    when(mockAmazonS3.doesBucketExistV2(s3Bucket)).thenReturn(true);
    when(mockAmazonS3.putObject(any(PutObjectRequest.class))).thenThrow(throwableType);
    assertErrorResponse();
  }

  private void assertErrorResponse() throws Exception {
    mockMvc
        .perform(
            multipart("/mis/product")
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