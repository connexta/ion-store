/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.connexta.store.adaptors.MetacardStorageAdaptor;
import com.connexta.store.adaptors.S3MetacardStorageAdaptor;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MetacardStorageAdaptorConfiguration {

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

  @Bean
  public MetacardStorageAdaptor metacardStorageAdaptor(
      @NotNull final AmazonS3 amazonS3, @Value("${s3.bucket}") @NotBlank final String bucket) {
    return new S3MetacardStorageAdaptor(amazonS3, bucket);
  }

  @Bean
  @Profile("production")
  public AmazonS3Configuration amazonS3Configuration(
      @Value("${s3.endpointUrl}") @NotBlank final String endpoint,
      @Value("${s3.region}") @NotBlank final String region,
      @Value("${s3.secret.file}") @NotBlank final String secretKeyFile,
      @Value("${s3.access.file}") @NotBlank final String accessKeyFile)
      throws IOException {
    return new AmazonS3Configuration(
        endpoint,
        region,
        FileUtils.readFileToString(new File(accessKeyFile), StandardCharsets.UTF_8),
        FileUtils.readFileToString(new File(secretKeyFile), StandardCharsets.UTF_8));
  }
}
