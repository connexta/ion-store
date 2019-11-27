/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import com.connexta.store.config.StoreControllerConfiguration;
import com.connexta.store.exceptions.common.DetailedErrorAttributes;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(StoreController.class)
@Import({DetailedErrorAttributes.class, StoreControllerConfiguration.class})
@AutoConfigureMockMvc
public class StoreControllerAddMetadataComponentTest {

  //  @MockBean private StoreService mockStoreService;
  //
  //  @Inject private MockMvc mockMvc;
  //
  //  @Value("${endpoints.store.version}")
  //  private String storeApiVersion;
  //
  //  @AfterEach
  //  public void afterEach() {
  //    verifyNoMoreInteractions(mockStoreService);
  //  }
  //
  //  @Test
  //  public void testContextLoads() {}
  //
  //  @Test
  //  public void testMissingFile() throws Exception {
  //    mockMvc
  //        .perform(
  //            multipart(
  //                    ADD_METADATA_URL_TEMPLATE,
  //                    "341d6c1ce5e0403a99fe86edaed66eea",
  //                    StoreController.SUPPORTED_METADATA_TYPE)
  //                .header(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion)
  //                .accept(MediaType.APPLICATION_JSON)
  //                .contentType(MediaType.MULTIPART_FORM_DATA)
  //                .with(
  //                    request -> {
  //                      request.setMethod("PUT");
  //                      return request;
  //                    }))
  //        .andExpect(status().isBadRequest());
  //  }
  //
  //  @Test
  //  public void testBadAcceptVersion() throws Exception {
  //    mockMvc
  //        .perform(
  //            multipart(
  //                    ADD_METADATA_URL_TEMPLATE,
  //                    "341d6c1ce5e0403a99fe86edaed66eea",
  //                    StoreController.SUPPORTED_METADATA_TYPE)
  //                .file(
  //                    new MockMultipartFile(
  //                        "file",
  //                        "this originalFilename is ignored",
  //                        MediaType.APPLICATION_JSON_VALUE,
  //                        IOUtils.toInputStream(
  //                            "{\"ext.extracted.text\" : \"All the color had been leached from
  // Winterfell until only grey and white remained\"}",
  //                            StandardCharsets.UTF_8)))
  //                .header(
  //                    StoreController.ACCEPT_VERSION_HEADER_NAME,
  //                    "this accept version is not supported")
  //                .accept(MediaType.APPLICATION_JSON)
  //                .contentType(MediaType.MULTIPART_FORM_DATA)
  //                .with(
  //                    request -> {
  //                      request.setMethod("PUT");
  //                      return request;
  //                    }))
  //        .andExpect(status().isNotImplemented());
  //  }
  //
  //  @ParameterizedTest(name = "status is {1} when metadataType is {0}")
  //  @MethodSource("badMetadataTypes")
  //  public void testBadMetadataTypes(final String metadataType, final HttpStatus expectedStatus)
  //      throws Exception {
  //    mockMvc
  //        .perform(
  //            multipart(ADD_METADATA_URL_TEMPLATE, "341d6c1ce5e0403a99fe86edaed66eea",
  // metadataType)
  //                .file(
  //                    new MockMultipartFile(
  //                        "file",
  //                        "this originalFilename is ignored",
  //                        MediaType.APPLICATION_JSON_VALUE,
  //                        IOUtils.toInputStream(
  //                            "{\"ext.extracted.text\" : \"All the color had been leached from
  // Winterfell until only grey and white remained\"}",
  //                            StandardCharsets.UTF_8)))
  //                .header(StoreController.ACCEPT_VERSION_HEADER_NAME, storeApiVersion)
  //                .accept(MediaType.APPLICATION_JSON)
  //                .contentType(MediaType.MULTIPART_FORM_DATA)
  //                .with(
  //                    request -> {
  //                      request.setMethod("PUT");
  //                      return request;
  //                    }))
  //        .andExpect(status().is(expectedStatus.value()));
  //  }
  //
  //  private static Stream<Arguments> badMetadataTypes() {
  //    return Stream.of(
  //        Arguments.of("   ", HttpStatus.BAD_REQUEST),
  //        Arguments.of("some string that doesn't match the regex", HttpStatus.BAD_REQUEST),
  //        Arguments.of("somethingOtherThanIrm", HttpStatus.NOT_IMPLEMENTED));
  //  }
}
