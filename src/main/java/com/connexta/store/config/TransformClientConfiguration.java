/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.clients.TransformClient;
import javax.inject.Named;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TransformClientConfiguration {

  @Bean("transformClientRestTemplate")
  public RestTemplate restTemplate(@NotNull RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public TransformClient transformClient(
      @NotNull @Named("transformClientRestTemplate") RestTemplate restTemplate,
      @NotBlank @Value("${endpointUrl.transform}") String transformEndpoint,
      @NotBlank @Value("${endpoints.transform.version}") String transformApiVersion) {
    return new TransformClient(restTemplate, transformEndpoint, transformApiVersion);
  }
}
