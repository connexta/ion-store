/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.transformation.rest.models.TransformResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import software.amazon.awssdk.services.s3.S3Client;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class IngestApplicationIntegrationTest {

  @Autowired S3Client mockS3Client;

  @Autowired RestTemplate restTemplate;

  @Autowired WebApplicationContext wac;

  private MockMvc mvc;

  @Before
  public void beforeEach() {
    mvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testSuccessfulTransformRequest() throws Exception {
    when(restTemplate.postForObject(
            eq("https://localhost/transform"), any(HttpEntity.class), eq(TransformResponse.class)))
        .thenReturn(new TransformResponse());

    mvc.perform(
            multipart("/ingest")
                .file("file", "some-content".getBytes())
                .param("fileSize", "10")
                .param("fileName", "file")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isAccepted());

    verify(restTemplate, times(1))
        .postForObject(
            eq("https://localhost/transform"), any(HttpEntity.class), eq(TransformResponse.class));
  }

  // TODO Test what happens when an error occurs when hitting transform service
  @Test
  public void testUnsuccessfulRequest() {}

  @Ignore("TODO Add a stub S3 server or expect that the status is not 400 ")
  @Test
  public void correctlyFormattedIngestRequestTest() throws Exception {

    mvc.perform(
            multipart("/ingest")
                .file("file", "some-content".getBytes())
                .param("fileSize", "10")
                .param("fileName", "file")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isAccepted());
  }

  @Test
  public void incorrectlyFormattedIngestRequestTest() throws Exception {

    mvc.perform(
            multipart("/ingest")
                .file("file", "some-content".getBytes())
                .param("filename", "file")
                .param("title", "qualityTitle")
                .param("mimeType", "plain/text")
                .header("Accept-Version", "1.2.1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().is4xxClientError());
  }
}
