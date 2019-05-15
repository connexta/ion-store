/*
 * Copyright (c) Connexta
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the
 * GNU Lesser General Public License is distributed along with this
 * program and can be found at http://www.gnu.org/licenses/lgpl.html.
 */
package com.connexta.multiintstore.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
  @Value("${cassandra.host}")
  private String host;

  @Value("${cassandra.port}")
  private int port;

  @Bean
  public Session createSession() {
    return createSession(host, port);
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
}
