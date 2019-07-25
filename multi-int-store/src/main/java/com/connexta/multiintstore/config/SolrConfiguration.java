/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
public class SolrConfiguration {

  @Bean
  @Profile("solrProduction")
  public SolrClient solrClient(
      @NotBlank @Value("${solr.host}") final String solrHost,
      @Value("${solr.port}") final int solrPort) {
    return new HttpSolrClient.Builder(String.format("http://%s:%d/solr", solrHost, solrPort))
        .build();
  }

  @Bean
  public SolrTemplate solrTemplate(@NotNull SolrClient client) {
    return new SolrTemplate(client);
  }
}
