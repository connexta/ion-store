/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreControllerConfiguration {

  @Bean
  public String storeApiVersion(
      @NotBlank @Value("${endpoints.store.version}") String storeApiVersion) {
    return storeApiVersion;
  }

  @Bean
  public String ingestApiVersion(
      @NotBlank @Value("${endpoints.ingest.version}") String ingestApiVersion) {
    return ingestApiVersion;
  }
}
