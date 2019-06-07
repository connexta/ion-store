package com.connexta.ingest.transform;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class TransformServiceClientTest {
  RestTemplate restTemplate;

  MockRestServiceServer mockServer;

  TransformServiceClient client;

  @Before
  public void setup() throws Exception {
    //    restTemplate = new RestTemplate();
    //    mockServer = MockRestServiceServer.createServer(restTemplate);
    client = new TransformServiceClient();
  }

  @Test
  public void sendRequest() {
    client.postRequest();
    //    DefaultResponseCreator response = MockRestResponseCreators.withSuccess();
    //    HttpHeaders headers = new HttpHeaders();
    //    headers.addAll("Allow", List.of("POST", "OPTIONS"));
    //    response.headers(headers);
    //    mockServer
    //        .expect(requestTo("http://localhost:8080/"))
    //        .andExpect(method(HttpMethod.POST))
    //        .andRespond(response);
  }
}
