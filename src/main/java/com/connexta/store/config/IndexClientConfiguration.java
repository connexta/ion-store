/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.clients.IndexClient;
import com.connexta.store.clients.IndexClientImpl;
import javax.inject.Named;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class IndexClientConfiguration {

  public static final String INDEX_WEB_CLIENT_BEAN = "indexWebClient";

  @Bean(IndexClientConfiguration.INDEX_WEB_CLIENT_BEAN)
  public WebClient indexWebClient(
      @NotBlank @Value("${endpoints.index.url}") final String indexEndpoint,
      @NotBlank @Value("${endpoints.index.version}") final String indexApiVersion) {
    return WebClient.builder()
        .baseUrl(indexEndpoint)
        .defaultHeader("Accept-Version", indexApiVersion)
        .build();
  }

  @Bean
  public IndexClient indexDatasetClient(
      @NotNull @Named(IndexClientConfiguration.INDEX_WEB_CLIENT_BEAN)
          final WebClient indexWebClient) {
    return new IndexClientImpl(indexWebClient);
  }
}
