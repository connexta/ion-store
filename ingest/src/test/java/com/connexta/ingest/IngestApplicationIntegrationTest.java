/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.ingest.config.S3StorageConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class IngestApplicationIntegrationTest {

  @Autowired WebApplicationContext wac;

  private MockMvc mvc;

  @Autowired S3StorageConfiguration s3StorageConfiguration;

  @Before
  public void beforeEach() {
    mvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void correctlyFormattedIngestRequestTest() throws Exception {

    mvc.perform(
            multipart("/ingest")
                .file("file", "some-content".getBytes())
                .param("fileSize", "10")
                .param("filename", "file")
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
