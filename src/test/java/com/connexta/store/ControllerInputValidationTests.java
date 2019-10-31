/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3;
import com.connexta.store.controllers.StoreController;
import java.io.IOException;
import javax.inject.Inject;
import javax.validation.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerInputValidationTests {

  @MockBean private AmazonS3 mockAmazonS3;

  @Inject private StoreController storeController;

  @Inject private MockMvc mockMvc;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @Test
  void testCannotReadAttachment() throws IOException {
    MultipartFile file = spy(newValidMultipartFile());
    doThrow(new IOException("Cannot read attachment")).when(file).getInputStream();
    assertBadRequest(file);
    verify(file).getInputStream();
  }

  @Test
  void testFileTooLarge() {
    MultipartFile file = spy(newValidMultipartFile());
    doReturn((10 * 1L << 30) + 1).when(file).getSize();
    assertBadRequest(file);
    verify(file).getSize();
  }

  @Test
  void testNoMediaType() {
    MultipartFile file = spy(newValidMultipartFile());
    doReturn(null).when(file).getContentType();
    assertBadRequest(file);
    verify(file).getContentType();
  }

  @Test
  void testNoFilename() {
    MultipartFile file = spy(newValidMultipartFile());
    doReturn(null).when(file).getOriginalFilename();
    assertBadRequest(file);
    verify(file).getOriginalFilename();
  }

  @NotNull
  private MockMultipartFile newValidMultipartFile() {
    return new MockMultipartFile("name", "fname", "mediatype", "x".getBytes());
  }

  private void assertBadRequest(MultipartFile file) {
    Assertions.assertThrows(
        ValidationException.class, () -> storeController.createDataset(storeApiVersion, file));
  }
}
