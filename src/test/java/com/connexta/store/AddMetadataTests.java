/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.connexta.store.clients.IndexClient;
import com.connexta.store.controllers.StoreController;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
public class AddMetadataTests {

  @MockBean private AmazonS3 mockAmazonS3;

  @Inject private MockMvc mockMvc;

  @Inject
  @Named("nonBufferingRestTemplate")
  private RestTemplate nonBufferingRestTemplate;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @Value("${endpoints.index.version}")
  private String indexApiVersion;

  @Value("${endpointUrl.index}")
  private String endpointUrlIndex;

  private MockRestServiceServer indexMockRestServiceServer;

  @BeforeEach
  public void beforeEach() {
    indexMockRestServiceServer = MockRestServiceServer.createServer(nonBufferingRestTemplate);
  }

  @AfterEach
  public void afterEach() {
    indexMockRestServiceServer.verify();
    indexMockRestServiceServer.reset();
    verifyNoMoreInteractions(ignoreStubs(mockAmazonS3));
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testMissingFile() throws Exception {
    mockMvc
        .perform(
            multipart("/mis/product/341d6c1ce5e0403a99fe86edaed66eea/cst")
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
            multipart("/mis/product/341d6c1ce5e0403a99fe86edaed66eea/cst")
                .file(
                    new MockMultipartFile(
                        "file",
                        "this originalFilename is ignored",
                        "application/json",
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

  @Test
  @Disabled("TODO")
  public void testCantReadAttachment() {
    // TODO verify 400
  }

  @ParameterizedTest(name = "status is {1} when metadataType is {0}")
  @MethodSource("badMetadataTypes")
  public void testBadMetadataTypes(final String metadataType, final HttpStatus expectedStatus)
      throws Exception {
    mockMvc
        .perform(
            multipart("/mis/product/341d6c1ce5e0403a99fe86edaed66eea/" + metadataType)
                .file(
                    new MockMultipartFile(
                        "file",
                        "this originalFilename is ignored",
                        "application/json",
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

  @Test
  public void testAddMetadata() throws Exception {
    final String datasetId = "341d6c1ce5e0403a99fe86edaed66eea";

    indexMockRestServiceServer
        .expect(requestTo(endpointUrlIndex + datasetId))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header(IndexClient.ACCEPT_VERSION_HEADER_NAME, indexApiVersion))
        .andRespond(withSuccess());

    mockMvc
        .perform(
            multipart("/mis/product/" + datasetId + "/cst")
                .file(
                    new MockMultipartFile(
                        "file",
                        "this originalFilename is ignored",
                        "application/json",
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
        .andExpect(status().isOk());
  }

  private static Stream<Arguments> badMetadataTypes() {
    return Stream.of(
        Arguments.of("   ", HttpStatus.BAD_REQUEST),
        Arguments.of("some string that doesn't match the regex", HttpStatus.BAD_REQUEST),
        Arguments.of("somethingOtherThanCst", HttpStatus.NOT_IMPLEMENTED));
  }
}
