/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import com.connexta.multiintstore.services.api.SearchService;
import com.connexta.multiintstore.services.impl.SearchServiceImpl;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchServiceConfiguration {
  @Bean
  public SearchService searchService(
      @NotNull IndexedMetadataRepository indexedMetadataRepository,
      @NotEmpty @Value("${endpointUrl.retrieve}") String retrieveEndpoint) {
    return new SearchServiceImpl(indexedMetadataRepository, retrieveEndpoint);
  }
}
