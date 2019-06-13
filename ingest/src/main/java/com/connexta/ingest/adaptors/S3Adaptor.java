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
import javax.annotation.PostConstruct;
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

  private final String s3AccessKey;
  private final String s3SecretKey;
  private final String s3Endpoint;
  private final String s3Region;
  private final String s3BucketQuarantine;

  private S3Client s3Client;

  @Autowired
  public S3Adaptor(S3StorageConfiguration configuration) {
    s3AccessKey = configuration.getS3AccessKey();
    s3SecretKey = configuration.getS3SecretKey();
    s3Endpoint = configuration.getS3Endpoint();
    s3Region = configuration.getS3Region();
    s3BucketQuarantine = configuration.getS3BucketQuarantine();
  }

  @PostConstruct
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
    final UUID ingestId = UUID.randomUUID();

    try {
      final PutObjectRequest putRequest =
          PutObjectRequest.builder().bucket(s3BucketQuarantine).key(ingestId.toString()).build();
      LOGGER.info("Storing in bucket \"{}\" with key \"{}\"", s3BucketQuarantine, ingestId);

      s3Client.putObject(
          putRequest,
          RequestBody.fromInputStream(
              ingestRequest.getFile().getInputStream(), ingestRequest.getFile().getSize()));
      LOGGER.info("{} has been successfully stored in S3.", ingestId);

    } catch (IOException | S3Exception | SdkClientException e) {
      // Handle put request failures
      // TODO: Actually handle errors and retries
      LOGGER.error(e.getMessage());
    }
    return ingestId;
  }
}
