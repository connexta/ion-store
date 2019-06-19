/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@Configuration
@EnableSolrRepositories(basePackages = "com.connexta.multiintstore.repositories")
@ComponentScan
public class ApplicationConfiguration {

  @Value("${solr.host}")
  private String solrHost;

  @Value("${solr.port}")
  private int solrPort;

  /** Used to check the Accept-Version in the callback request */
  @Value("${callback-accept-version}")
  private String callbackAcceptVersion;

  @Bean
  public SolrClient solrClient() {
    String url = String.format("http://%s:%d/solr", solrHost, solrPort);
    return new HttpSolrClient.Builder(url).build();
  }

  @Bean
  public SolrTemplate solrTemplate(SolrClient client) throws Exception {
    return new SolrTemplate(client);
  }

  public String getCallbackAcceptVersion() {
    return callbackAcceptVersion;
  }
}
