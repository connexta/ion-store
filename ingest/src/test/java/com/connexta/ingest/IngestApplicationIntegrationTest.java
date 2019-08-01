/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest;

import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
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
import javax.inject.Inject;
import javax.inject.Named;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class IngestApplicationIntegrationTest {

  private static final byte[] TEST_FILE = "some-content".getBytes();
  private static final int TEST_FILE_SIZE = TEST_FILE.length;
  private static final String TEST_MIME_TYPE = "text/plain";

  @Value("${endpointUrl.store}")
  private String endpointUrlStore;

  @Value("${endpointUrl.transform}")
  private String endpointUrlTransform;

  @Value("${endpoints.transform.version}")
  private String endpointsTransformVersion;

  @Inject private MockMvc mvc;
  @Inject private RestTemplate restTemplate;

  @Inject
  @Named("nonBufferingRestTemplate")
  private RestTemplate nonBufferingRestTemplate;

  private MockRestServiceServer storeServer;
  private MockRestServiceServer transformServer;

  @Before
  public void beforeEach() {
    storeServer = MockRestServiceServer.createServer(nonBufferingRestTemplate);
    transformServer = MockRestServiceServer.createServer(restTemplate);
  }

  @After
  public void afterEach() {
    storeServer.verify();
    storeServer.reset();
    transformServer.verify();
    transformServer.reset();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testSuccessfulIngestRequest() throws Exception {
    final String location = "http://localhost:1232/store/1234";
    storeServer
        .expect(requestTo(endpointUrlStore))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withCreatedEntity(new URI(location)));

    transformServer
        .expect(requestTo(endpointUrlTransform))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Accept-Version", endpointsTransformVersion))
        .andExpect(jsonPath("$.location").value(location))
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
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isAccepted());
  }

  /* START store request tests */

  @Test
  public void testStoreRequestBadRequest() throws Exception {
    storeServer
        .expect(requestTo(endpointUrlStore))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withBadRequest());

    transformServer.expect(never(), requestTo(endpointUrlTransform));

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testStoreRequestUnauthorizedRequest() throws Exception {
    storeServer
        .expect(requestTo(endpointUrlStore))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withUnauthorizedRequest());

    transformServer.expect(never(), requestTo(endpointUrlTransform));

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
    storeServer
        .expect(requestTo(endpointUrlStore))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    transformServer.expect(never(), requestTo(endpointUrlTransform));

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
    storeServer
        .expect(requestTo(endpointUrlStore))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED));

    transformServer.expect(never(), requestTo(endpointUrlTransform));

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
    storeServer
        .expect(requestTo(endpointUrlStore))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withServerError());

    transformServer.expect(never(), requestTo(endpointUrlTransform));

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
    final String location = "http://localhost:1232/store/1234";
    storeServer
        .expect(requestTo(endpointUrlStore))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withCreatedEntity(new URI(location)));

    transformServer
        .expect(requestTo(endpointUrlTransform))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Accept-Version", endpointsTransformVersion))
        .andExpect(jsonPath("$.location").value(location))
        .andRespond(withServerError());

    mvc.perform(
            multipart("/ingest")
                .file("file", TEST_FILE)
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }
  /* END transform request tests */

}
