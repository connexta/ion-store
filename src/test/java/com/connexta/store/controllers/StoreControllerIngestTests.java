/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static com.connexta.store.controllers.MultipartFileValidator.MAX_FILE_BYTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.store.config.StoreControllerConfiguration;
import com.connexta.store.exceptions.StoreException;
import com.connexta.store.exceptions.common.DetailedErrorAttributes;
import com.connexta.store.service.api.StoreService;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(StoreController.class)
@Import({DetailedErrorAttributes.class, StoreControllerConfiguration.class})
@AutoConfigureMockMvc
public class StoreControllerIngestTests {
  private StoreController storeController;
  private static final String ACCEPT_VERSION = "0.1.0";
  private static final String CORRELATION_ID = "90210";
  private static final String LAST_MODIFIED_DATE = "1984-04-20T08:08:08Z";
  private static final MockMultipartFile FILE =
      new MockMultipartFile(
          "file", "originalFilename.txt", MediaType.TEXT_PLAIN_VALUE, "file_content".getBytes());
  private static final MockMultipartFile METACARD =
      new MockMultipartFile("metacard", "ignored.xml", "application/xml", "content".getBytes());

  @MockBean private StoreService mockStoreService;
  @Inject private MockMvc mockMvc;

  @Mock MultipartFile mockFile;

  @BeforeEach
  public void beforeEach() {
    mockFile = mock(MultipartFile.class);
    storeController = new StoreController(mockStoreService, ACCEPT_VERSION);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "2017-06-11T14:32:28Z",
        "2007-07-04T09:09:09.120+00:00",
        "1985-10-25T17:32:28.101+00:00"
      })
  public void testSuccessfulIngestRequest(String lastModified) throws Exception {
    mockMvc
        .perform(
            multipart("/ingest")
                .file(FILE)
                .file(METACARD)
                .param("correlationId", CORRELATION_ID)
                .header(LAST_MODIFIED, lastModified)
                .header("Accept-Version", ACCEPT_VERSION))
        .andExpect(status().isAccepted());

    verify(mockStoreService)
        .ingest(
            eq(FILE.getSize()),
            eq(FILE.getContentType()),
            inputStreamContentEq(FILE.getInputStream()),
            eq(FILE.getOriginalFilename()),
            eq(METACARD.getSize()),
            inputStreamContentEq(METACARD.getInputStream()));
  }

  @Test
  public void testMissingFile() throws Exception {
    mockMvc
        .perform(
            multipart("/ingest")
                .file(METACARD)
                .param("correlationId", CORRELATION_ID)
                .header(LAST_MODIFIED, LAST_MODIFIED_DATE)
                .header("Accept-Version", ACCEPT_VERSION))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mockStoreService);
  }

  @Test
  public void testMissingMetacard() throws Exception {
    mockMvc
        .perform(
            multipart("/ingest")
                .file(FILE)
                .param("correlationId", CORRELATION_ID)
                .header(LAST_MODIFIED, LAST_MODIFIED_DATE)
                .header("Accept-Version", ACCEPT_VERSION))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mockStoreService);
  }

  @Test
  public void testMissingCorrelationID() throws Exception {
    mockMvc
        .perform(
            multipart("/ingest")
                .file(FILE)
                .file(METACARD)
                .header(LAST_MODIFIED, LAST_MODIFIED_DATE)
                .header("Accept-Version", ACCEPT_VERSION))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mockStoreService);
  }

  @Test
  public void testMissingAcceptVersionHeader() throws Exception {
    mockMvc
        .perform(
            multipart("/ingest")
                .file(FILE)
                .file(METACARD)
                .header(LAST_MODIFIED, LAST_MODIFIED_DATE)
                .param("correlationId", CORRELATION_ID))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mockStoreService);
  }

  @Test
  public void testMissingLastModifiedHeader() throws Exception {
    mockMvc
        .perform(
            multipart("/ingest")
                .file(FILE)
                .file(METACARD)
                .header("Accept-Version", ACCEPT_VERSION)
                .param("correlationId", CORRELATION_ID))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mockStoreService);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "2011-12-03", // ISO_LOCAL_DATE
        "2011-12-03+01:00", // ISO_OFFSET_DATE
        "10:15:30", // ISO_LOCAL_TIME
        "10:15:30+01:00", // ISO_OFFSET_TIME
        "2011-12-03T10:15:30", // ISO_LOCAL_DATE_TIME
        "2012-337", // ISO_ORDINAL_DATE
        "2012-W48-6", // ISO_WEEK_DATE
        "20111203", // BASIC_ISO_DATE
        "Sun, 11 Jun 2017 14:32:28 GMT", // RFC_1123_DATE_TIME
        "2017-06-11T14:32:28.120+0000", // ISO_DATE_TIME missing offset :
        "             ",
        ""
      })
  public void testInvalidLastModifiedHeader(String badDate) throws Exception {
    mockMvc
        .perform(
            multipart("/ingest")
                .file(FILE)
                .file(METACARD)
                .header("Accept-Version", ACCEPT_VERSION)
                .header(LAST_MODIFIED, badDate)
                .param("correlationId", CORRELATION_ID))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mockStoreService);
  }

  /**
   * TODO Test RuntimeException and Throwable thrown by {@link StoreService#ingest(Long, String,
   * InputStream, String, Long, InputStream)}
   */
  @ParameterizedTest(name = "{0} is the response status code when IngestService#ingest throws {1}")
  @MethodSource("exceptionThrownByIngestServiceAndExpectedResponseStatus")
  public void testIngestServiceExceptions(
      final Throwable throwable, final HttpStatus expectedResponseStatus) throws Exception {
    doThrow(throwable).when(mockStoreService).ingest(any(), any(), any(), any(), any(), any());

    mockMvc
        .perform(
            multipart("/ingest")
                .file(FILE)
                .file(METACARD)
                .param("correlationId", CORRELATION_ID)
                .header(LAST_MODIFIED, LAST_MODIFIED_DATE)
                .header("Accept-Version", ACCEPT_VERSION))
        .andExpect(status().is(expectedResponseStatus.value()));
  }

  private static Stream<Arguments> exceptionThrownByIngestServiceAndExpectedResponseStatus() {
    return Stream.of(
        Arguments.of(
            new StoreException("Test", new Throwable()), HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @NotNull
  private static InputStream inputStreamContentEq(@NotNull final InputStream expected) {
    return argThat(
        actual -> {
          try {
            return IOUtils.contentEquals(expected, actual);
          } catch (final IOException e) {
            fail("Unable to compare input streams", e);
            return false;
          }
        });
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = " ")
  public void testIngestBlankContentType(final String contentType) {
    when(mockFile.getContentType()).thenReturn(contentType);
    assertThat(
        storeController
            .ingest(
                ACCEPT_VERSION,
                OffsetDateTime.parse(LAST_MODIFIED_DATE),
                mockFile,
                CORRELATION_ID,
                METACARD)
            .getStatusCode(),
        is(HttpStatus.ACCEPTED));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  public void testBadFilename(final String filename) {
    when(mockFile.getContentType()).thenReturn("testContentType");
    when(mockFile.getOriginalFilename()).thenReturn(filename);
    assertThat(
        storeController
            .ingest(
                ACCEPT_VERSION,
                OffsetDateTime.parse(LAST_MODIFIED_DATE),
                mockFile,
                CORRELATION_ID,
                METACARD)
            .getStatusCode(),
        is(HttpStatus.ACCEPTED));
  }

  @Test
  void testIngestFileTooLarge() {
    when(mockFile.getContentType()).thenReturn("testContentType");
    when(mockFile.getOriginalFilename()).thenReturn("testFilename");
    when(mockFile.getSize()).thenReturn(MAX_FILE_BYTES + 1);
    assertThat(
        storeController
            .ingest(
                ACCEPT_VERSION,
                OffsetDateTime.parse(LAST_MODIFIED_DATE),
                mockFile,
                CORRELATION_ID,
                METACARD)
            .getStatusCode(),
        is(HttpStatus.ACCEPTED));
  }

  @Test
  void testIngestFileTooSmall() {
    when(mockFile.getContentType()).thenReturn("testContentType");
    when(mockFile.getOriginalFilename()).thenReturn("testFilename");
    when(mockFile.getSize()).thenReturn(0L);
    assertThat(
        storeController
            .ingest(
                ACCEPT_VERSION,
                OffsetDateTime.parse(LAST_MODIFIED_DATE),
                mockFile,
                CORRELATION_ID,
                METACARD)
            .getStatusCode(),
        is(HttpStatus.ACCEPTED));
  }

  @Test
  void testIngestCannotReadAttachment() throws Exception {
    when(mockFile.getContentType()).thenReturn("testContentType");
    when(mockFile.getOriginalFilename()).thenReturn("testFilename");
    when(mockFile.getSize()).thenReturn(MAX_FILE_BYTES);
    when(mockFile.getInputStream()).thenThrow(IOException.class);
    assertThrows(
        ValidationException.class,
        () ->
            storeController.ingest(
                ACCEPT_VERSION,
                OffsetDateTime.parse(LAST_MODIFIED_DATE),
                mockFile,
                CORRELATION_ID,
                METACARD));
  }
}
