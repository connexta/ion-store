/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
/* Default Package */
class Dependencies {

  //  Dependencies
  static def awsS3 = { it=Versions.aws -> "software.amazon.awssdk:s3:${it}" }
  static def commonsIO = { it=Versions.commonsIO -> "commons-io:commons-io:${it}" }
  static def ingestAPI = { it=Versions.ingestAPI -> "com.connexta.ion.ingest:ingest-api-rest-spring-stubs:${it}" }
  static def lombok = { it=Versions.lombok -> "org.projectlombok:lombok:${it}" }
  static def reactiveStreams = { it=Versions.reactiveStreams -> "org.reactivestreams:reactive-streams:${it}" }
  static def springActuator = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-starter-actuator:${it}" }
  static def springBootStarterWeb = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-starter-web:${it}" }
  static def springDataSolr = { it=Versions.springData -> "org.springframework.data:spring-data-solr:${it}" }
  static def swagger = { it=Versions.swagger -> "io.springfox:springfox-swagger2:${it}" }
  static def swaggerUi = { it=Versions.swagger -> "io.springfox:springfox-swagger-ui:${it}" }
  static def transformAPI = { it=Versions.transformAPI -> "com.connexta.transformation:transformation-api-rest-spring-stubs:${it}" }

  //  Test Dependencies
  static def hamcrestOptional = { it=Versions.npathai -> "com.github.npathai:hamcrest-optional:${it}" }
  static def junit = { it=Versions.junit -> "junit:junit:${it}" }
  static def mockito = { it= Versions.mockito -> "org.mockito:mockito-core:${it}" }
  static def restito = { it=Versions.restito -> "com.xebialabs.restito:restito:${it}" }
  static def springBootStarterTest = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-starter-test:${it}" }
  static def testContainers = { it=Versions.testContainers -> "org.testcontainers:testcontainers:${it}" }

  //  Plugin Dependencies
  static def palantir = { it=Versions.palantir -> "gradle.plugin.com.palantir.gradle.docker:gradle-docker:${it}" }
  static def springBootGradlePlugin = { it=Versions.springBoot -> "org.springframework.boot:spring-boot-gradle-plugin:${it}" }

  // OWASP Override Transitive Dependencies
  // CVE-2018-10237
  static def zookeeper = "org.apache.zookeeper:zookeeper:]3.4.14,)"
  // CVE-2019-0232
  static def guava = "com.google.guava:guava:]24.1.1,)"
  // CVE-2019-12384 and CVE-2019-1281
  static def jacksonDatabind = "com.fasterxml.jackson.core:jackson-databind:[2.9.9.1,)"

}
