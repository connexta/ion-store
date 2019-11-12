/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.poller.service.StatusService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TransformConfiguration {
  @Bean
  public String transformApiVersion(
      @NotBlank @Value("${endpoints.transform.version}") String transformApiVersion) {
    return transformApiVersion;
  }

  // TODO: For now the polling library and the async jobs can share the same thread pool
  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(64);
  }

  @Bean
  public StatusService statusService(@NotNull final ExecutorService executorService) {
    return new StatusService(1, 20, executorService, WebClient.create());
  }
}
