/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest;

import static org.mockito.Mockito.when;

import com.connexta.ingest.config.S3StorageConfiguration;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestUtilConfig {

  @Bean
  @Primary
  public S3StorageConfiguration getStorageConfig() {

    S3StorageConfiguration storageConfig = Mockito.mock(S3StorageConfiguration.class);

    when(storageConfig.getS3AccessKey()).thenReturn("password");
    when(storageConfig.getS3SecretKey()).thenReturn("password");
    when(storageConfig.getS3Endpoint()).thenReturn("http://foobar.me/1234");
    when(storageConfig.getS3Region()).thenReturn("us-west-1");
    when(storageConfig.getS3BucketQuarantine()).thenReturn("Leaky");

    return storageConfig;
  }
}
