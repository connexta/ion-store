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
package com.connexta.multiintstore.storage.persistence.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(
    basePackages = "com.connexta.multiintstore.storage.persistence.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {
  public static final String KEYSPACE = "multiintstore_keyspace";

  @Override
  public SchemaAction getSchemaAction() {
    return SchemaAction.CREATE_IF_NOT_EXISTS;
  }

  @Override
  protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
    CreateKeyspaceSpecification specification =
        CreateKeyspaceSpecification.createKeyspace(KEYSPACE).ifNotExists();

    return Arrays.asList(specification);
  }

  @Override
  protected String getKeyspaceName() {
    return KEYSPACE;
  }

  @Override
  public String[] getEntityBasePackages() {
    return new String[] {"com.connexta.multiintstore.storage.persistence.models"};
  }

  // https://stackoverflow.com/questions/53101753/spring-boot-data-cassandra-reactive-jmxreporter-problem
  @Override
  protected boolean getMetricsEnabled() {
    return false;
  }
}
