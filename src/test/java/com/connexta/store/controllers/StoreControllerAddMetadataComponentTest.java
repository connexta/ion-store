/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static com.connexta.store.controllers.StoreController.ADD_METADATA_URL_TEMPLATE;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.store.config.StoreControllerConfiguration;
import com.connexta.store.exceptions.common.DetailedErrorAttributes;
import com.connexta.store.service.api.StoreService;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreController.class)
@Import({DetailedErrorAttributes.class, StoreControllerConfiguration.class})
@AutoConfigureMockMvc
public class StoreControllerAddMetadataComponentTest {

  @MockBean private StoreService mockStoreService;

  @Inject private MockMvc mockMvc;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @AfterEach
  public void afterEach() {
    verifyNoMoreInteractions(mockStoreService);
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testMissingFile() throws Exception {
    mockMvc
        .perform(
            multipart(
                    ADD_METADATA_URL_TEMPLATE,
                    "341d6c1ce5e0403a99fe86edaed66eea",
                    StoreController.SUPPORTED_METADATA_TYPE)
                .header(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(
                    request -> {
                      request.setMethod("PUT");
                      return request;
                    }))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testBadAcceptVersion() throws Exception {
    mockMvc
        .perform(
            multipart(
                    ADD_METADATA_URL_TEMPLATE,
                    "341d6c1ce5e0403a99fe86edaed66eea",
                    StoreController.SUPPORTED_METADATA_TYPE)
                .file(
                    new MockMultipartFile(
                        "file",
                        "this originalFilename is ignored",
                        MediaType.APPLICATION_JSON_VALUE,
                        IOUtils.toInputStream(
                            "{\"ext.extracted.text\" : \"All the color had been leached from Winterfell until only grey and white remained\"}",
                            StandardCharsets.UTF_8)))
                .header(
                    StoreController.ACCEPT_VERSION_HEADER_NAME,
                    "this accept version is not supported")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(
                    request -> {
                      request.setMethod("PUT");
                      return request;
                    }))
        .andExpect(status().isNotImplemented());
  }

  @ParameterizedTest(name = "status is {1} when metadataType is {0}")
  @MethodSource("badMetadataTypes")
  public void testBadMetadataTypes(final String metadataType, final HttpStatus expectedStatus)
      throws Exception {
    mockMvc
        .perform(
            multipart(ADD_METADATA_URL_TEMPLATE, "341d6c1ce5e0403a99fe86edaed66eea", metadataType)
                .file(
                    new MockMultipartFile(
                        "file",
                        "this originalFilename is ignored",
                        MediaType.APPLICATION_JSON_VALUE,
                        IOUtils.toInputStream(
                            "{\"ext.extracted.text\" : \"All the color had been leached from Winterfell until only grey and white remained\"}",
                            StandardCharsets.UTF_8)))
                .header(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(
                    request -> {
                      request.setMethod("PUT");
                      return request;
                    }))
        .andExpect(status().is(expectedStatus.value()));
  }

  private static Stream<Arguments> badMetadataTypes() {
    return Stream.of(
        Arguments.of("   ", HttpStatus.BAD_REQUEST),
        Arguments.of("some string that doesn't match the regex", HttpStatus.BAD_REQUEST),
        Arguments.of("somethingOtherThanIrm", HttpStatus.NOT_IMPLEMENTED));
  }
}
