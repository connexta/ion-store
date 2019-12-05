/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static com.connexta.store.controllers.StoreController.ADD_METADATA_URL_TEMPLATE;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.store.config.StoreControllerConfiguration;
import com.connexta.store.exceptions.common.DetailedErrorAttributes;
import com.connexta.store.service.api.StoreService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreController.class)
@Import({DetailedErrorAttributes.class, StoreControllerConfiguration.class})
@AutoConfigureMockMvc
class StoreControllerAddMetadataComponentTest {

  private static final String ADD_METADATA_REQUEST_FILE_PATH =
      "/com/connexta/store/controllers/addMetadataRequest.json";

  private static final String ACCEPT_VERSION = "Accept-Version";

  @MockBean private StoreService mockStoreService;

  @Inject private MockMvc mockMvc;

  @Value("${endpoints.store.version}")
  private String storeApiVersion;

  @AfterEach
  void afterEach() {
    verifyNoMoreInteractions(mockStoreService);
  }

  @Test
  void testMissingAddMetadataRequest() throws Exception {
    mockMvc
        .perform(
            put(ADD_METADATA_URL_TEMPLATE, UUID.randomUUID())
                .header(ACCEPT_VERSION, storeApiVersion)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testBadAcceptVersion() throws Exception {
    mockMvc
        .perform(
            put(ADD_METADATA_URL_TEMPLATE, UUID.randomUUID())
                .header(ACCEPT_VERSION, "1.2.3-badVersionThatWillNeverBe")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    request -> {
                      request.setContent(
                          getTestResourceAsString(ADD_METADATA_REQUEST_FILE_PATH).getBytes());
                      return request;
                    }))
        .andExpect(status().isNotImplemented());
  }

  private String getTestResourceAsString(String testResourcePath) {
    InputStream inputStream = this.getClass().getResourceAsStream(testResourcePath);

    if (inputStream != null) {
      try {
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new AssertionError(
            String.format("Failed to read test resource %s as string.", testResourcePath), e);
      }
    }

    throw new AssertionError(String.format("Missing test resource %s.", testResourcePath));
  }
}
