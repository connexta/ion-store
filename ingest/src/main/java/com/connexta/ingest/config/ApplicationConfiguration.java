/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.config;

import com.connexta.ingest.client.StoreClient;
import com.connexta.ingest.client.TransformClient;
import com.connexta.ingest.service.api.IngestService;
import com.connexta.ingest.service.impl.IngestServiceImpl;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@EnableAutoConfiguration
public class ApplicationConfiguration {

  @Bean
  public RestTemplate restTemplate(@NotNull RestTemplateBuilder builder) {
    return builder.errorHandler(new DefaultResponseErrorHandler()).build();
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    final CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeClientInfo(true);
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setIncludeHeaders(true);
    filter.setAfterMessagePrefix("Inbound Request: ");
    filter.setMaxPayloadLength(5120);
    return filter;
  }

  @Bean
  public IngestService ingestService(
      @NotNull StoreClient storeClient, @NotNull TransformClient transformClient) {
    return new IngestServiceImpl(storeClient, transformClient);
  }

  @Bean
  public StoreClient storeClient(
      @NotNull RestTemplate restTemplate,
      @NotEmpty @Value("${endpointUrl.store}") String storeEndpoint) {
    return new StoreClient(restTemplate, storeEndpoint);
  }

  @Bean
  public TransformClient transformClient(
      @NotNull RestTemplate restTemplate,
      @NotEmpty @Value("${endpointUrl.transform}") String transformEndpoint,
      @NotEmpty @Value("${endpoints.transform.version}") String transformApiVersion) {
    return new TransformClient(restTemplate, transformEndpoint, transformApiVersion);
  }
}
