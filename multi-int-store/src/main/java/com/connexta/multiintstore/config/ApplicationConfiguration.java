/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import com.connexta.multiintstore.adaptors.StorageAdaptor;
import com.connexta.multiintstore.common.MetadataStorageManager;
import com.connexta.multiintstore.common.ProductStorageManager;
import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import com.connexta.multiintstore.services.api.Dao;
import com.connexta.multiintstore.services.api.SearchService;
import com.connexta.multiintstore.services.impl.IndexedMetadataDao;
import com.connexta.multiintstore.services.impl.SearchServiceImpl;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public RestTemplate restTemplate(@NotEmpty RestTemplateBuilder builder) {
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
  public SearchService searchService(
      @NotNull IndexedMetadataRepository indexedMetadataRepository,
      @NotEmpty @Value("${endpointUrl.retrieve}") String retrieveEndpoint) {
    return new SearchServiceImpl(indexedMetadataRepository, retrieveEndpoint);
  }

  @Bean
  public IndexedMetadataDao indexedMetadataDao(@NotNull IndexedMetadataRepository repository) {
    return new IndexedMetadataDao(repository);
  }

  @Bean
  public ProductStorageManager productStorageManager(
      @NotEmpty @Value("${endpointUrl.retrieve}") String retrieveEndpoint,
      @NotNull StorageAdaptor storageAdapter) {
    return new ProductStorageManager(retrieveEndpoint, storageAdapter);
  }

  @Bean
  public MetadataStorageManager metadataStorageManager(
      @NotNull Dao<IndexedProductMetadata, String> cstDao) {
    return new MetadataStorageManager(cstDao);
  }
}
