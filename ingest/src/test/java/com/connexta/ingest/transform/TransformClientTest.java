/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

// TODO: Fix these tests once we bring in the real Transformation API

// @RunWith(SpringRunner.class)
// @SpringBootTest
// public class TransformClientTest {
  //
  //  @Autowired RestTemplate restTemplate;
  //
  //  @Autowired TransformClient client;
  //
  //  private MockRestServiceServer mockServer;
  //  private String transformEndpoint;
  //  private TransformResponse expectedSuccessResponse;
  //  private TransformRequest transformRequest;
  //  private TransformResponse expectedFailedResponse;
  //
  //  @Before
  //  public void setUp() throws URISyntaxException {
  //    mockServer = MockRestServiceServer.createServer(restTemplate);
  //    transformEndpoint = "http://localhost:8000/transform";
  //    client.setTransformEndpoint(transformEndpoint);
  //
  //    // Mock data
  //    transformRequest = new TransformRequest();
  //    transformRequest.setId("1");
  //    transformRequest.setBytes("2");
  //    transformRequest.setCallbackUrl("callback-url");
  //    transformRequest.setMimeType(MediaType.IMAGE_JPEG_VALUE);
  //    transformRequest.setProductLocation("product-location");
  //    transformRequest.setStagedLocation("staged-location");
  //    expectedSuccessResponse = new TransformResponse();
  //    expectedSuccessResponse.setId("1");
  //    expectedSuccessResponse.setMessage("success");
  //    expectedFailedResponse = new TransformResponse();
  //    expectedFailedResponse.setId("1");
  //    expectedFailedResponse.setMessage("failure");
  //    expectedFailedResponse.setDetails(List.of("A", "B"));
  //  }
  //
  //  @Test
  //  public void testSuccessfulRequest() throws JsonProcessingException {
  //
  //    mockServer
  //        .expect(requestTo(transformEndpoint))
  //        .andExpect(method(HttpMethod.POST))
  //        .andExpect(jsonPath("$.id").value("1"))
  //        .andExpect(jsonPath("$.bytes").value("2"))
  //        .andExpect(jsonPath("$.callbackUrl").value("callback-url"))
  //        .andExpect(jsonPath("$.mimeType").value(MediaType.IMAGE_JPEG_VALUE))
  //        .andExpect(jsonPath("$.productLocation").value("product-location"))
  //        .andExpect(jsonPath("$.stagedLocation").value("staged-location"))
  //        .andRespond(withSuccess(expectedSuccessResponse));
  //    TransformResponse transformResponse = client.requestTransform(transformRequest);
  //    mockServer.verify();
  //    assertThat(transformResponse.getId(), equalTo("1"));
  //    assertThat(transformResponse.getMessage(), equalTo("success"));
  //    assertThat(transformResponse.isError(), is(false));
  //  }
  //
  //  @Test
  //  public void testMalformedRequest() throws JsonProcessingException {
  //    mockServer.expect(anything()).andRespond(withBadRequest(expectedFailedResponse));
  //    TransformResponse transformResponse = client.requestTransform(transformRequest);
  //    mockServer.verify();
  //    assertThat(transformResponse.getDetails(), hasSize(2));
  //    assertThat(transformResponse.isError(), is(true));
  //  }
  //
  //  @Test
  //  public void testServerError() throws JsonProcessingException {
  //    mockServer.expect(anything()).andRespond(withServerError(expectedFailedResponse));
  //    TransformResponse transformResponse = client.requestTransform(transformRequest);
  //    mockServer.verify();
  //    assertThat(transformResponse.isError(), is(true));
  //  }
  //
  //  @Test
  //  public void testNoJsonResponse() {
  //    mockServer.expect(anything()).andRespond(withUnauthorizedRequest());
  //    TransformResponse transformResponse = client.requestTransform(transformRequest);
  //    assertThat(transformResponse.isError(), is(true));
  //    assertThat(transformResponse.getId(), equalTo("1"));
  //  }
// }
