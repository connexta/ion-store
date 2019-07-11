/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
@Profile("s3Production")
public class S3StorageConfiguration {

  @Bean
  public S3Client s3ClientFactory(
      @Value("${aws.s3.endpointUrl}") @NotEmpty final String s3Endpoint,
      @Value("${aws.s3.region}") @NotEmpty final String s3Region,
      @Value("${aws.s3.secret.file}") @NotEmpty final String awsSecretKeyFile,
      @Value("${aws.s3.access.file}") @NotEmpty final String awsAccessKeyFile)
      throws IOException {
    final AwsCredentials credentials =
        AwsBasicCredentials.create(
            FileUtils.readFileToString(new File(awsAccessKeyFile), StandardCharsets.UTF_8),
            FileUtils.readFileToString(new File(awsSecretKeyFile), StandardCharsets.UTF_8));
    final S3Client s3Client =
        S3Client.builder()
            .endpointOverride(URI.create(s3Endpoint))
            .region(Region.of(s3Region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    log.info("S3 Client has been initialized.");
    log.info("Region: {}", s3Region);
    log.info("Endpoint: {}", s3Endpoint);
    return s3Client;
  }
}
