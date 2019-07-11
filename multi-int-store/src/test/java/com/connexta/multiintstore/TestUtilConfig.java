/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.mockito.Mockito.mock;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class TestUtilConfig {

  @Bean
  @Profile("test")
  public S3Client s3Client() {
    return mock(S3Client.class);
  }

  //  @Bean
  //  @Profile("test")
  //  public S3StorageAdaptor s3StorageAdaptor() {
  //    return new S3StorageAdaptor(mock(S3Client.class), "http://tests3");
  //    // return new S3StorageAdaptor(s3Client(), "http://tests3");
  //  }

  @Bean
  @Profile("solrDev")
  public SolrClient solrClient() {
    return mock(SolrClient.class);
  }
}
