/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static com.connexta.store.controllers.MultipartFileValidator.MAX_FILE_BYTES;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.connexta.store.controllers.StoreController;
import com.connexta.store.rest.spring.StoreApi;
import com.connexta.store.service.api.StoreService;
import java.io.IOException;
import javax.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

// TODO: These test can be changed to test the MultipartFileValidator instead of the controller.
@ExtendWith(MockitoExtension.class)
public class StoreControllerUnitTests {

  private static final String STORE_API_VERSION = "testStoreApiVersion";

  @Mock private StoreService mockStoreService;

  private StoreApi storeApi;

  @BeforeEach
  public void beforeEach() {
    storeApi = new StoreController(mockStoreService, STORE_API_VERSION);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  public void testBadContentType(
      final String contentType, @Mock final MultipartFile mockMultipartFile) {
    when(mockMultipartFile.getContentType()).thenReturn(contentType);
    assertBadRequest(mockMultipartFile);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  public void testBadFilename(final String filename, @Mock final MultipartFile mockMultipartFile) {
    when(mockMultipartFile.getContentType()).thenReturn("testContentType");
    when(mockMultipartFile.getOriginalFilename()).thenReturn(filename);
    assertBadRequest(mockMultipartFile);
  }

  @Test
  void testFileTooLarge(@Mock final MultipartFile mockMultipartFile) {
    when(mockMultipartFile.getContentType()).thenReturn("testContentType");
    when(mockMultipartFile.getOriginalFilename()).thenReturn("testFilename");
    when(mockMultipartFile.getSize()).thenReturn(MAX_FILE_BYTES + 1);
    assertBadRequest(mockMultipartFile);
  }

  @Test
  void testCannotReadAttachment(@Mock final MultipartFile mockMultipartFile) throws Exception {
    when(mockMultipartFile.getContentType()).thenReturn("testContentType");
    when(mockMultipartFile.getOriginalFilename()).thenReturn("testFilename");
    when(mockMultipartFile.getSize()).thenReturn(MAX_FILE_BYTES);
    when(mockMultipartFile.getInputStream()).thenThrow(IOException.class);
    assertBadRequest(mockMultipartFile);
  }

  private void assertBadRequest(@Mock MultipartFile mockMultipartFile) {
    assertThrows(
        ValidationException.class,
        () -> storeApi.createDataset(STORE_API_VERSION, mockMultipartFile));
  }
}
