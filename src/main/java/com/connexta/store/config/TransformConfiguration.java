/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.clients.TransformClient;
import com.connexta.store.poller.TransformPollerFactory;
import com.connexta.store.poller.TransformStatusTask;
import com.google.common.collect.Queues;
import java.util.concurrent.BlockingQueue;
import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TransformConfiguration {

  public static final String TRANSFORM_STATUS_REQUEST_TEMPLATE = "/transform";
  public static final String TRANSFORM_STATUS_DELETE_TEMPLATE = "/transform/{datasetId}";

  public static final String TRANSFORM_CLIENT_BEAN = "transformWebClient";
  public static final String STORE_CLIENT_BEAN = "storeWebClient";

  @Bean
  public String transformApiVersion(
      @NotBlank @Value("${endpoints.transform.version}") String transformApiVersion) {
    return transformApiVersion;
  }

  @Bean(name = TRANSFORM_CLIENT_BEAN)
  public WebClient transformWebClient(
      @Value("${endpoints.transform.url}") String transformBaseUrl,
      @Value("${endpoints.transform.version}") String transformApiVersion) {
    return WebClient.builder()
        .baseUrl(transformBaseUrl)
        .defaultHeader("Accept-Version", transformApiVersion)
        .build();
  }

  @Bean
  public TransformClient transformClient(
      @Qualifier(TransformConfiguration.TRANSFORM_CLIENT_BEAN) WebClient transformWebClient) {
    return new TransformClient(transformWebClient);
  }

  @Bean
  public TransformPollerFactory pollerFactory(
      @Qualifier(TransformConfiguration.TRANSFORM_CLIENT_BEAN) WebClient transformWebClient,
      @Qualifier(TransformConfiguration.STORE_CLIENT_BEAN) WebClient storeWebClient) {
    return new TransformPollerFactory(transformWebClient, storeWebClient);
  }

  @Bean(name = "storeWebClient")
  public WebClient storeWebClient(
      @Value("${endpoints.store.url}") final String storeHost,
      @Value("${endpoints.store.version}") final String storeVersion) {
    return WebClient.builder()
        .baseUrl(storeHost)
        .defaultHeader("Accept-Version", storeVersion)
        .build();
  }

  @Bean
  public BlockingQueue<TransformStatusTask> transformStatusQueue() {
    return Queues.newLinkedBlockingDeque(10000);
  }
}
