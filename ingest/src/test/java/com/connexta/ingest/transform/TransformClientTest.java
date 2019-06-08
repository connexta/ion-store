/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransformClientTest {

  @Autowired RestTemplate restTemplate;

  @Autowired TransformClient client;

  private MockRestServiceServer mockServer;

  @Before
  public void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  public void testPerformTransform() {
    mockServer
        .expect(requestTo("http://localhost:8000/transform"))
        .andExpect(jsonPath("$.id").value("30f14c6c1fc85cba12bfd093aa8f90e3"))
        .andRespond(withSuccess("<put json response here>", MediaType.TEXT_PLAIN));

    String result = client.requestTransform();
    System.err.println("Test output: " + result);

    mockServer.verify();
    // assertEquals("hello", result);
  }

  /* @Before
  public void setup() throws Exception {
    restTemplate = new ApplicationConfiguration().restTemplate();
    mockTransformationService = MockRestServiceServer.createServer(restTemplate);
    client = new TransformServiceClient(restTemplate);
  }

  @Test
  public void sendRequest() {
    DefaultResponseCreator response = MockRestResponseCreators.withSuccess();
    HttpHeaders headers = new HttpHeaders();
    headers.addAll("Allow", List.of("POST"));
    response.headers(headers);
    mockTransformationService
        .expect(requestTo("http://localhost:8000/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(response);
    client.postRequest();
    mockTransformationService.verify();

    //    HttpHeaders headers = new HttpHeaders();
    //    headers.addAll("Allow", List.of("POST", "OPTIONS"));
    //    response.headers(headers);
    //    mockTransformationService
    //        .expect(requestTo("http://localhost:8080/"))
    //        .andExpect(method(HttpMethod.POST))
    //        .andRespond(response);
  }*/
}
