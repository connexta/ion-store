/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.poller.service.TransformPollingService;
import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransformPollerConfiguration {

  @Bean
  public TransformPollingService transformPollingService() {
    return new TransformPollingService();
  }

  @Bean
  public String transformApiVersion(
      @NotBlank @Value("${endpoints.transform.version}") String transformApiVersion) {
    return transformApiVersion;
  }
}
