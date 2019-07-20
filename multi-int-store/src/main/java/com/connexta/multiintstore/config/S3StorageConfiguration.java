/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.connexta.multiintstore.adaptors.S3StorageAdaptor;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Slf4j
@Configuration
public class S3StorageConfiguration {

  @Bean
  public AmazonS3 amazonS3(@NotNull final AmazonS3Configuration configuration) {
    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(
            new EndpointConfiguration(configuration.getEndpoint(), configuration.getRegion()))
        .withCredentials(
            new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(
                    configuration.getAccessKey(), configuration.getSecretKey())))
        .enablePathStyleAccess()
        .build();
  }

  // TODO does this need to be a separate bean?
  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public TransferManager transferManager(@NotNull AmazonS3 amazonS3) {
    return TransferManagerBuilder.standard().withS3Client(amazonS3).build();
  }

  @Bean
  public S3StorageAdaptor s3StorageAdaptor(
      @NotNull final AmazonS3 amazonS3,
      @NotNull TransferManager transferManager,
      @Value("${aws.s3.bucket.quarantine}") @NotBlank final String bucket) {
    return new S3StorageAdaptor(amazonS3, transferManager, bucket);
  }

  @Profile("production")
  private AmazonS3Configuration amazonS3Configuration(
      @Value("${aws.s3.endpointUrl}") @NotBlank final String endpoint,
      @Value("${aws.s3.region}") @NotBlank final String region,
      @Value("${aws.s3.secret.file}") @NotBlank final String secretKeyFile,
      @Value("${aws.s3.access.file}") @NotBlank final String accessKeyFile)
      throws IOException {
    return new AmazonS3Configuration(
        endpoint,
        region,
        FileUtils.readFileToString(new File(accessKeyFile), StandardCharsets.UTF_8),
        FileUtils.readFileToString(new File(secretKeyFile), StandardCharsets.UTF_8));
  }
}
