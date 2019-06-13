/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@Configuration
@EnableSolrRepositories(
    basePackages = "com.connexta.multiintstore.storage.persistence.repository",
    namedQueriesLocation = "classpath:solr-named-queries.properties")
@ComponentScan
public class ApplicationConfiguration {
  @Value("${cassandra.host}")
  private String cassandraHost;

  @Value("${cassandra.port}")
  private int cassandraPort;

  @Bean
  public Session createSession() {
    return createSession(cassandraHost, cassandraPort);
  }

  public static Session createSession(String ip, int port) {
    Cluster cluster;

    cluster = Cluster.builder().addContactPoint(ip).withPort(port).build();

    Session session = cluster.connect();

    session.execute(
        "CREATE KEYSPACE IF NOT EXISTS multiintstore_keyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");
    session.execute("USE multiintstore_keyspace;");
    session.execute(
        "CREATE TABLE IF NOT EXISTS metadata(id UUID PRIMARY KEY, ddms2 text, ddms5 text);");

    return session;
  }

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
  public SolrTemplate solrTemplate(SolrClient client) throws Exception {
    return new SolrTemplate(client);
  }

  /**
   * We use this to check the Accept-Version in the callback request. The name of this should
   * probably be updated.
   */
  @Value("${ion-version}")
  private String ionVersion;

  public String getIonVersion() {
    return ionVersion;
  }
}
