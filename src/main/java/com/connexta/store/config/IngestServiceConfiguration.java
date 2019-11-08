/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.adaptors.MetacardStorageAdaptor;
import com.connexta.store.clients.StoreClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.service.api.IngestService;
import com.connexta.store.service.impl.IngestServiceImpl;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestServiceConfiguration {

  @Bean
  public IngestService ingestService(
      @NotBlank @Value("${endpointUrl.retrieve}") final String retrieveEndpoint,
      @NotNull final StoreClient storeClient,
      @NotNull final TransformClient transformClient,
      @NotNull final MetacardStorageAdaptor metacardStorageAdaptor) {
    return new IngestServiceImpl(
        storeClient, metacardStorageAdaptor, retrieveEndpoint, transformClient);
  }
}