/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.mockito.Mockito.mock;

import com.connexta.multiintstore.config.CallbackAcceptVersion;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("test")
public class TestUtilConfig {

  @Bean
  @Primary
  public SolrClient solrClient() {
    return mock(SolrClient.class);
  }

  @Bean
  @Primary
  public S3Client s3Client() {
    return mock(S3Client.class);
  }

  @Bean
  public String s3BucketQuarantine() {
    return "test-bucket";
  }

  @Bean
  @Primary
  public CallbackAcceptVersion callbackAcceptVersion() {
    return new CallbackAcceptVersion("4.2.x");
  }

  @Bean
  @Primary
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return mock(RestTemplate.class);
  }
}
