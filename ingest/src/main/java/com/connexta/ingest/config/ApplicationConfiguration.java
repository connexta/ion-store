/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.config;

import com.connexta.multiintstore.storageadaptor.impl.s3.spring.S3StorageAdaptorConfiguration;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration
@Profile("default")
@Import({S3StorageAdaptorConfiguration.class})
public class ApplicationConfiguration {

  @NotNull
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @NotEmpty
  @Value("${aws.s3.bucket}")
  private String s3Bucket;

  @NotNull
  @Bean
  public String s3Bucket() {
    return s3Bucket;
  }
}
