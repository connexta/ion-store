/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.adaptors.S3StorageAdaptor;
import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.clients.IndexClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.poller.TransformStatusTask;
import com.connexta.store.service.api.StoreService;
import com.connexta.store.service.impl.StoreServiceImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import javax.inject.Named;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class StoreServiceConfiguration {

  @Bean
  public StoreService storeService(
      @NotBlank @Value("${endpoints.store.url}") final String retrieveEndpoint,
      @NotNull @Named("fileStorageAdaptor") final StorageAdaptor fileStorageAdapter,
      @NotNull @Named("irmStorageAdaptor") final StorageAdaptor irmStorageAdapter,
      @NotNull @Named("metacardStorageAdaptor") final S3StorageAdaptor metacardStorageAdapter,
      @NotNull final IndexClient indexClient,
      @NotNull final TransformClient transformClient,
      @NotNull final BlockingQueue<TransformStatusTask> transformStatusQueue,
      @Qualifier(value = "transformWebClient") WebClient transformWebClient)
      throws URISyntaxException {
    return new StoreServiceImpl(
        new URI(retrieveEndpoint),
        fileStorageAdapter,
        irmStorageAdapter,
        metacardStorageAdapter,
        indexClient,
        transformClient,
        transformStatusQueue,
        transformWebClient);
  }
}
