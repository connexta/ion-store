/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.connexta.multiintstore.adaptors.S3StorageAdaptor;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class S3StorageConfiguration {

  @Bean
  @Profile("s3Production")
  public AmazonS3 s3ClientFactory(
      @Value("${aws.s3.endpointUrl}") @NotEmpty final String s3Endpoint,
      @Value("${aws.s3.region}") @NotEmpty final String s3Region,
      @Value("${aws.s3.secret.file}") @NotEmpty final String awsSecretKeyFile,
      @Value("${aws.s3.access.file}") @NotEmpty final String awsAccessKeyFile)
      throws IOException {
    final AwsClientBuilder.EndpointConfiguration endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration(s3Endpoint, s3Region);
    final BasicAWSCredentials credentials =
        new BasicAWSCredentials(
            FileUtils.readFileToString(new File(awsAccessKeyFile), StandardCharsets.UTF_8),
            FileUtils.readFileToString(new File(awsSecretKeyFile), StandardCharsets.UTF_8));
    final AmazonS3 s3Client =
        AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(endpointConfiguration)
            .build();
    log.info("S3 Client has been initialized.");
    log.info("Region: {}", s3Region);
    log.info("Endpoint: {}", s3Endpoint);
    return s3Client;
  }

  @Bean
  public S3StorageAdaptor s3StorageAdaptor(
      @NotNull AmazonS3 s3Client,
      @NotNull TransferManager s3transferManager,
      @NotEmpty @Value("${aws.s3.bucket.quarantine}") String s3Bucket) {
    return new S3StorageAdaptor(s3Client, s3transferManager, s3Bucket);
  }

  @Bean
  @Profile("s3Production")
  public TransferManager s3TransferManager(AmazonS3 s3Client) {
    return TransferManagerBuilder.standard().withS3Client(s3Client).build();
  }
}
