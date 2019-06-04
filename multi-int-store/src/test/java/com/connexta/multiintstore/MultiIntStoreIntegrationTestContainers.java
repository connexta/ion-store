/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
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
