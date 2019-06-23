/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storageadaptor.impl.s3.spring;

import com.connexta.multiintstore.storageadaptor.StorageAdaptor;
import com.connexta.multiintstore.storageadaptor.impl.s3.S3StorageAdaptor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("default")
public class S3StorageAdaptorConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageAdaptorConfiguration.class);

  @NotEmpty
  @Value("${aws.s3.endpointUrl}")
  private String s3Endpoint;

  @NotEmpty
  @Value("${aws.s3.region}")
  private String s3Region;

  @NotEmpty
  @Value("${aws.s3.secret.file}")
  private String awsSecretKeyFile;

  @NotEmpty
  @Value("${aws.s3.access.file}")
  private String awsAccessKeyFile;

  @NotEmpty private String s3SecretKey;

  @NotEmpty private String s3AccessKey;

  @Bean
  public StorageAdaptor storageAdaptor(final String s3Bucket) {
    final S3Client s3Client =
        S3Client.builder()
            .endpointOverride(URI.create(s3Endpoint))
            .region(Region.of(s3Region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3AccessKey, s3SecretKey)))
            .build();
    LOGGER.info("S3 Client has been initialized.");
    LOGGER.info("Region: {}", s3Region);
    LOGGER.info("Endpoint: {}", s3Endpoint);

    return new S3StorageAdaptor(s3Client, s3Bucket);
  }

  @PostConstruct
  public void readKeysFromFiles() throws IOException {
    s3AccessKey = FileUtils.readFileToString(new File(awsAccessKeyFile), StandardCharsets.UTF_8);
    s3SecretKey = FileUtils.readFileToString(new File(awsSecretKeyFile), StandardCharsets.UTF_8);
  }
}
