/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest;

import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class IngestApplicationIntegrationTest {

  private static final byte[] TEST_FILE = "some-content".getBytes();
  private static final int TEST_FILE_SIZE = TEST_FILE.length;
  private static final String TEST_MIME_TYPE = "text/plain";
  private static final String ENDPOINT_URL_TRANSFORM = "http://localhost:1231/transform/";
  private static final String ENDPOINT_URL_STORE = "http://localhost:1232/store/";

  @Autowired private RestTemplate restTemplate;
  @Autowired private WebApplicationContext wac;

  private MockRestServiceServer server;
  private MockMvc mvc;

  @Before
  public void beforeEach() {
    mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    server = MockRestServiceServer.createServer(restTemplate);
  }

  @After
  public void afterEach() {
    server.reset();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testSuccessfulIngestRequest() throws Exception {
    final String location = "http://localhost:1232/store/1234";
    server
        .expect(requestTo(ENDPOINT_URL_STORE))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withCreatedEntity(new URI(location)));

    server
        .expect(requestTo(ENDPOINT_URL_TRANSFORM))
        .andExpect(method(HttpMethod.POST))
        .andExpect(jsonPath("$.bytes").value(TEST_FILE_SIZE))
        .andExpect(jsonPath("$.mimeType").value(TEST_MIME_TYPE))
        .andExpect(jsonPath("$.productLocation").value(location))
        .andRespond(
            withStatus(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    new JSONObject()
                        .put("id", "asdf")
                        .put("message", "The ID asdf has been accepted")
                        .toString()));

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", TEST_MIME_TYPE)
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isAccepted());
  }

  /* START store request tests */

  @Test
  public void testStoreRequestBadRequest() throws Exception {
    server
        .expect(requestTo(ENDPOINT_URL_STORE))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withBadRequest());

    server.expect(never(), requestTo(ENDPOINT_URL_TRANSFORM));

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testStoreRequestUnauthorizedRequest() throws Exception {
    server
        .expect(requestTo(ENDPOINT_URL_STORE))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withUnauthorizedRequest());

    server.expect(never(), requestTo(ENDPOINT_URL_TRANSFORM));

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testStoreRequestForbidden() throws Exception {
    server
        .expect(requestTo(ENDPOINT_URL_STORE))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    server.expect(never(), requestTo(ENDPOINT_URL_TRANSFORM));

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testStoreRequestNotImplemented() throws Exception {
    server
        .expect(requestTo(ENDPOINT_URL_STORE))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED));

    server.expect(never(), requestTo(ENDPOINT_URL_TRANSFORM));

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testStoreRequestServerError() throws Exception {
    server
        .expect(requestTo(ENDPOINT_URL_STORE))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withServerError());

    server.expect(never(), requestTo(ENDPOINT_URL_TRANSFORM));

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  /* END store request tests */

  /* START transform request tests */

  // The error handler throws the same exception for all non-202 status codes returned by the
  // transformation endpoint.
  @Test
  public void testUnsuccessfulTransformRequest() throws Exception {
    server
        .expect(requestTo(ENDPOINT_URL_STORE))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withCreatedEntity(new URI("http://localhost:1232/store/1234")));

    server.expect(requestTo(ENDPOINT_URL_TRANSFORM)).andRespond(withBadRequest());

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", TEST_MIME_TYPE)
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  /* END transform request tests */

  /* START ingest request tests */

  @Test
  public void testIncorrectlyFormattedIngestRequest() throws Exception {
    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("filename", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", TEST_MIME_TYPE)
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testIngestRequestFileSizeMismatch() throws Exception {
    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .param("fileSize", String.valueOf(TEST_FILE_SIZE + 1))
                .param("fileName", "testFile.txt")
                .param("title", "qualityTitle")
                .param("mimeType", TEST_MIME_TYPE)
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  /* END ingest request tests */
}
