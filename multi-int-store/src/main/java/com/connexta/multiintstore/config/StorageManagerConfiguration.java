/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import com.connexta.multiintstore.adaptors.StorageAdaptor;
import com.connexta.multiintstore.common.IndexManager;
import com.connexta.multiintstore.common.ProductStorageManager;
import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.services.api.Dao;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageManagerConfiguration {
  @Bean
  public ProductStorageManager productStorageManager(
      @NotBlank @Value("${endpointUrl.retrieve}") String retrieveEndpoint,
      @NotNull StorageAdaptor storageAdapter) {
    return new ProductStorageManager(retrieveEndpoint, storageAdapter);
  }

  @Bean
  public IndexManager metadataStorageManager(
      @NotNull Dao<IndexedProductMetadata, String> cstDao, @NotNull StorageAdaptor storageAdaptor) {
    return new IndexManager(cstDao, storageAdaptor);
  }
}
