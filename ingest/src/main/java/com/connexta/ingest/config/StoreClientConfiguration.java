/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.config;

import com.connexta.ingest.client.StoreClient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StoreClientConfiguration {
  @Bean
  public StoreClient storeClient(
      @NotNull RestTemplate restTemplate,
      @NotBlank @Value("${endpointUrl.store}") String storeEndpoint) {
    return new StoreClient(restTemplate, storeEndpoint);
  }
}
