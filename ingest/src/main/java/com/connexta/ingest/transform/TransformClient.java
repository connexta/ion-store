/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransformClient {

  String jsonBody =
      "{"
          + "\"id\": \"30f14c6c1fc85cba12bfd093aa8f90e3\",\n"
          + "\"stagedLocation\": \"https://<host>:<port>/my/temp/location/e1bd38ddb07444958cf3a18dd6291518\",\n"
          + "\"productLocation\": \"https://<host>:<port>/my/final/location/30f14c6c1fc85cba12bfd093aa8f90e3\",\n"
          + "\"mimeType\": \"img/nitf\",\n"
          + "\"bytes\": \"2147483648\",\n"
          + "\"callbackUrl\": \"https://<host>:<port>/a/callback\"\n"
          + "}";

  @Autowired private RestTemplate restTemplate;

  private URI transformEndpoint;

  public String requestTransform() {

    return restTemplate.postForObject(getTransformEndpoint(), jsonBody, String.class);
  }

  public URI getTransformEndpoint() {
    return transformEndpoint;
  }

  public void setTransformEndpoint(URI transformEndpoint) {
    this.transformEndpoint = transformEndpoint;
  }

  public void setTransformEndpoint(String transformEndpoint) throws URISyntaxException {
    this.transformEndpoint = new URI(transformEndpoint);
  }
}
