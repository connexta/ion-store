/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.adaptors;

import com.connexta.ingest.config.S3StorageConfiguration;
import com.connexta.ingest.service.api.IngestRequest;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3Adaptor implements Adaptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3Adaptor.class);
  private S3Client s3Client;

  private String s3BucketQuarantine;

  private String s3Endpoint;

  private String s3Region;

  private String s3SecretKey;

  private String s3AccessKey;

  private S3StorageConfiguration configuration;

  @Autowired
  public S3Adaptor(S3StorageConfiguration configuration) {
    this.configuration = configuration;
    s3AccessKey = configuration.getS3AccessKey();
    s3SecretKey = configuration.getS3SecretKey();
    s3Endpoint = configuration.getS3Endpoint();
    s3Region = configuration.getS3Region();
    s3BucketQuarantine = configuration.getS3BucketQuarantine();

    initializeS3();
  }

  private void initializeS3() {
    AwsCredentials credentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);

    s3Client =
        S3Client.builder()
            .endpointOverride(URI.create(s3Endpoint))
            .region(Region.of(s3Region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    LOGGER.info("S3 Client has been initialized");
  }

  @Override
  public UUID upload(IngestRequest ingestRequest) {
    UUID key = UUID.randomUUID();

    try {
      PutObjectRequest putRequest =
          PutObjectRequest.builder().bucket(s3BucketQuarantine).key(key.toString()).build();
      LOGGER.debug("Storing in bucket \"{}\" with key \"{}\"", s3BucketQuarantine, key);

      s3Client.putObject(
          putRequest,
          RequestBody.fromInputStream(
              ingestRequest.getFile().getInputStream(), ingestRequest.getFile().getSize()));
      LOGGER.info("{} has been successfully stored in S3.", key);

    } catch (IOException | S3Exception | SdkClientException e) {
      // Handle put request failures
      // TODO: Actually handle errors and retries
      LOGGER.error(e.getMessage());
    }
    return key;
  }
}
