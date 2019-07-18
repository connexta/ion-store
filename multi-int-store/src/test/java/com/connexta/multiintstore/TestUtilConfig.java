/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
public class TestUtilConfig {

  @Bean
  @Profile("test")
  public TransferManager s3Client() {
    return mock(TransferManager.class);
  }

  @Bean
  @Profile("test")
  public Upload uploadObject() {
    return mock(Upload.class);
  }

  @Bean
  @Profile("solrDev")
  public SolrClient solrClient() {
    return mock(SolrClient.class);
  }
}
