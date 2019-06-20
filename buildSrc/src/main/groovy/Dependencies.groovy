/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
/* Default Package */
class Dependencies {

  //  Dependencies
  static def awsBom = { it=Versions.aws -> "software.amazon.awssdk:bom:${it}" }
  static def awsS3 = { it=Versions.aws -> "software.amazon.awssdk:s3:${it}" }
  static def commonsIO = { it=Versions.commonsIO -> "commons-io:commons-io:${it}" }
  static def guava = { it=Versions.guava -> "com.google.guava:guava:${it}" }
  static def ingestAPI = { it=Versions.ingestAPI -> "com.connexta.ingest:ingest-api-rest-spring-stubs:${it}" }
  static def jacksonDatabind = { it=Versions.jacksonCore -> "com.fasterxml.jackson.core:jackson-databind:${it}" }
  static def json = { it=Versions.json -> "org.json:json:${it}" }
  static def lombok = { it=Versions.lombok -> "org.projectlombok:lombok:${it}" }
  static def springBootWebfluxStarter = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-starter-webflux:${it}" }
  static def springBootWebStarter = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-starter-web:${it}" }
  static def springFrameworkSolr = { it=Versions.springData -> "org.springframework.data:spring-data-solr:${it}" }
  static def tomcatCore = { it=Versions.tomcat -> "org.apache.tomcat.embed:tomcat-embed-core:${it}" }
  static def tomcatWebsocket = { it=Versions.tomcat -> "org.apache.tomcat.embed:tomcat-embed-websocket:${it}" }
  static def transformAPI = { it=Versions.transformAPI -> "com.connexta.transformation:transformation-api-rest-spring-stubs:${it}" }
  static def zookeeper = { it=Versions.zookeeper -> "org.apache.zookeeper:zookeeper:${it}" }

  //  Test Dependencies
  static def hamcrestOptionals = { it=Versions.hamcrest -> "com.github.npathai:hamcrest-optional:${it}" }
  static def junit = { it=Versions.junit -> "junit:junit:${it}" }
  static def mockito = { it= Versions.mockito -> "org.mockito:mockito-core:${it}" }
  static def restito = { it=Versions.restito -> "com.xebialabs.restito:restito:${it}" }
  static def springBootTestStarter = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-starter-test:${it}" }
  static def testContainers = { it=Versions.testContainers -> "org.testcontainers:testcontainers:${it}" }

  //  Plugin Dependencies
  static def palantir = { it=Versions.palantir -> "gradle.plugin.com.palantir.gradle.docker:gradle-docker:${it}" }
  static def springBootGradlePlugin = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-gradle-plugin:${it}" }
}
