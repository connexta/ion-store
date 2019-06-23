/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest;

import static org.mockito.Mockito.mock;

import com.connexta.multiintstore.storageadaptor.StorageAdaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@Profile("test")
public class TestUtilConfig implements WebMvcConfigurer {

  @Bean
  @Primary
  public StorageAdaptor storageAdaptor() {
    return mock(StorageAdaptor.class);
  }

  @Bean
  @Primary
  public String s3Bucket() {
    return "test-bucket";
  }

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    return mock(RestTemplate.class);
  }
}
