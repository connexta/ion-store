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
package com.connexta.multiintstore;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = MultiIntStoreIntegrationTestContainers.Initializer.class)
@EnableConfigurationProperties
public class MultiIntStoreIntegrationTestContainers extends MultiIntStoreIntegrationTest {
  @ClassRule
  public static GenericContainer cassandra =
      new GenericContainer("cassandra:3").withExposedPorts(9042);

  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
              "cassandra.host=" + cassandra.getContainerIpAddress(),
              "cassandra.port=" + cassandra.getMappedPort(9042))
          .applyTo(configurableApplicationContext.getEnvironment());
    }
  }
}
