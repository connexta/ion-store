/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.mockito.Mockito.mock;

import com.connexta.multiintstore.config.CallbackAcceptVersion;
import com.connexta.multiintstore.config.EndpointUrlRetrieve;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

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
  public EndpointUrlRetrieve endpointUrlRetrieve() {
    return new EndpointUrlRetrieve("https://localhost:1234/");
  }

  @Bean
  @Primary
  public CallbackAcceptVersion callbackAcceptVersion() {
    return new CallbackAcceptVersion("https://localhost:1222/");
  }
}
