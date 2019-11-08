/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.clients.StoreClient;
import javax.inject.Named;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StoreClientConfiguration {

  @Bean("storeClientRestTemplate")
  public RestTemplate storeClientRestTemplate() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setBufferRequestBody(false);
    return new RestTemplate(requestFactory);
  }

  @Bean
  public StoreClient storeClient(
      @NotNull @Named("storeClientRestTemplate") RestTemplate restTemplate,
      @NotBlank @Value("${endpointUrl.store}") String storeEndpoint) {
    return new StoreClient(restTemplate, storeEndpoint);
  }
}
