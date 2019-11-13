/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.poller.service.StatusService;
import com.connexta.poller.service.StatusServiceImpl;
import java.util.concurrent.ExecutorService;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TransformConfiguration {
  @Bean
  public String transformApiVersion(
      @NotBlank @Value("${endpoints.transform.version}") String transformApiVersion) {
    return transformApiVersion;
  }

  @Bean
  public ExecutorService threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(32);
    executor.setThreadNamePrefix("polling threadpool");
    executor.initialize();
    return new ExecutorServiceAdapter(executor);
  }

  @Bean
  public StatusService statusService(@NotNull final ExecutorService executorService) {
    return new StatusServiceImpl(1, 20, executorService, WebClient.create());
  }
}
