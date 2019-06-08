/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URISyntaxException;
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
  private String transformEndpoint;
  private TransformSuccessResponse expectedSuccessResponse = new TransformSuccessResponse();
  private TransformRequest mockRequest = new TransformRequest();

  @Before
  public void setUp() throws URISyntaxException {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    transformEndpoint = "http://localhost:8000/transform";
    client.setTransformEndpoint(transformEndpoint);

    // Mock data
    mockRequest.setId("1");
    mockRequest.setBytes("2");
    mockRequest.setCallbackUrl("callback-url");
    mockRequest.setMimeType(MediaType.IMAGE_JPEG_VALUE);
    mockRequest.setProductLocation("product-location");
    mockRequest.setStagedLocation("staged-location");
    expectedSuccessResponse.setId("1");
    expectedSuccessResponse.setMessage("success-message");
  }

  @Test
  public void testSuccessfulRequest() throws JsonProcessingException {

    mockServer
        .expect(requestTo(transformEndpoint))
        .andExpect(jsonPath("$.id").value("1"))
        .andExpect(jsonPath("$.bytes").value("2"))
        .andExpect(jsonPath("$.callbackUrl").value("callback-url"))
        .andExpect(jsonPath("$.mimeType").value(MediaType.IMAGE_JPEG_VALUE))
        .andExpect(jsonPath("$.productLocation").value("product-location"))
        .andExpect(jsonPath("$.stagedLocation").value("staged-location"))
        .andRespond(withSuccess(asJson(expectedSuccessResponse), MediaType.APPLICATION_JSON));

    TransformSuccessResponse result = client.requestTransform(mockRequest);

    mockServer.verify();
    assertThat(result.getId(), equalTo("1"));
    assertThat(result.getMessage(), equalTo("success-message"));
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

  private String asJson(Object object) throws JsonProcessingException {
    return (new ObjectMapper()).writeValueAsString(object);
    //     JsonGenerator jsonGenerator = null;
    //     ObjectMapper objectMapper = null;
    //      objectMapper = new ObjectMapper();
    //      try{
    //        jsonGenerator = objectMapper.getFactory().createJsonGenerator(System.out,
    // JsonEncoding.UTF8);
    //        jsonGenerator.writeObject(bean);
    //        objectMapper.writeValue(System.out, bean);
    //      }catch (IOException e) {
    //        e.printStackTrace();
    //      }
    //      jsonGenerator.flush();
    //      jsonGenerator.close();
  }
}
