/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import javax.validation.constraints.NotEmpty;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@EnableSolrRepositories(basePackages = "com.connexta.multiintstore.repositories")
@Configuration
@Profile("default")
public class SolrConfiguration {

  @NotEmpty
  @Value("${solr.host}")
  private String solrHost;

  @Value("${solr.port}")
  private int solrPort;

  @Bean
  public SolrClient solrClient() {
    String url = String.format("http://%s:%d/solr", solrHost, solrPort);
    return new HttpSolrClient.Builder(url).build();
  }

  @Bean
  public SolrTemplate solrTemplate(SolrClient client) {
    return new SolrTemplate(client);
  }
}
