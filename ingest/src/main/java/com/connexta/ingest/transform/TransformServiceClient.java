/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

public class TransformServiceClient {

  @Autowired RestTemplate restTemplate;

  String jsonBody =
      "{"
          + "\"id\": \"30f14c6c1fc85cba12bfd093aa8f90e3\",\n"
          + "\"stagedLocation\": \"https://<host>:<port>/my/temp/location/e1bd38ddb07444958cf3a18dd6291518\",\n"
          + "\"productLocation\": \"https://<host>:<port>/my/final/location/30f14c6c1fc85cba12bfd093aa8f90e3\",\n"
          + "\"mimeType\": \"img/nitf\",\n"
          + "\"bytes\": \"2147483648\",\n"
          + "\"callbackUrl\": \"https://<host>:<port>/a/callback\"\n"
          + "}";

  public void postRequest() {

    String walletBalanceUrl = "http://localhost:8000/";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, httpHeaders);

    RestTemplate restTemplate = new RestTemplate();
    String response = restTemplate.postForObject(walletBalanceUrl, httpEntity, String.class);
    JSONObject jsonObj = new JSONObject(response);
    String balance = jsonObj.get("data").toString();
  }

  TransformationResponse getTransformationObject() {
    return restTemplate.postForObject(
        "http://localhost:8000/", jsonBody, TransformationResponse.class);
  }
}
