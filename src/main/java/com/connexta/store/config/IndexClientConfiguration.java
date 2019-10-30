/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.clients.IndexDatasetClient;
import com.connexta.store.clients.IndexDatasetClientImpl;
import javax.inject.Named;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class IndexClientConfiguration {

  @Bean("nonBufferingRestTemplate")
  public RestTemplate nonBufferingRestTemplate() {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setBufferRequestBody(false);
    return new RestTemplate(requestFactory);
  }

  @Bean
  public IndexDatasetClient indexDatasetClient(
      @NotNull @Named("nonBufferingRestTemplate") final RestTemplate restTemplate,
      @NotBlank @Value("${endpointUrl.index}") final String indexEndpoint,
      @NotBlank @Value("${endpoints.index.version}") final String indexApiVersion) {
    return new IndexDatasetClientImpl(restTemplate, indexEndpoint, indexApiVersion);
  }
}
