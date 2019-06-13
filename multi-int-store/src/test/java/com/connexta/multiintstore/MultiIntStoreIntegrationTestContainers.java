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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = MultiIntStoreIntegrationTestContainers.Initializer.class)
@EnableConfigurationProperties
public abstract class MultiIntStoreIntegrationTestContainers {

  @ClassRule
  public static final GenericContainer solr =
      new GenericContainer("solr:8")
          .withCommand("solr-create -c searchTerms")
          .withExposedPorts(8983);

  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
              "solr.host=" + solr.getContainerIpAddress(), "solr.port=" + solr.getMappedPort(8983))
          .applyTo(configurableApplicationContext.getEnvironment());
    }
  }
}
