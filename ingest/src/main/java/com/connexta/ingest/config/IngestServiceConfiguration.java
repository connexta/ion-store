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
import javax.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestServiceConfiguration {
  @Bean
  public IngestService ingestService(
      @NotNull StoreClient storeClient, @NotNull TransformClient transformClient) {
    return new IngestServiceImpl(storeClient, transformClient);
  }
}
