/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.adaptors;

import com.connexta.ingest.config.S3StorageConfiguration;
import com.connexta.ingest.service.api.RetrieveResponse;
import com.connexta.ingest.service.api.StoreRequest;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** TODO one adaptor for quarantine and one adaptor for multi-int-store */
@Service
public class S3StorageAdaptor implements StorageAdaptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageAdaptor.class);

  private final String s3AccessKey;
  private final String s3SecretKey;
  private final String s3Endpoint;
  private final String s3Region;
  private final String s3BucketQuarantine;

  private S3Client s3Client;

  @Autowired
  public S3StorageAdaptor(S3StorageConfiguration configuration) {
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
  public void store(StoreRequest storeRequest, String key) throws IOException {
    final String fileName = storeRequest.getFileName();

    LOGGER.info("Storing {} in bucket \"{}\" with key \"{}\"", fileName, s3BucketQuarantine, key);
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(s3BucketQuarantine)
            .key(key)
            .contentType(storeRequest.getMimeType())
            .contentLength(storeRequest.getFileSize())
            .metadata(ImmutableMap.of("filename", fileName))
            .build(),
        RequestBody.fromInputStream(
            storeRequest.getFile().getInputStream(), storeRequest.getFile().getSize()));
  }

  @Override
  public RetrieveResponse retrieve(String ingestId) throws IOException {
    LOGGER.info(
        "Retrieving product in bucket \"{}\" with key \"{}\"", s3BucketQuarantine, ingestId);

    final ResponseInputStream<GetObjectResponse> getObjectResponseResponseInputStream =
        s3Client.getObject(
            GetObjectRequest.builder().bucket(s3BucketQuarantine).key(ingestId).build());
    final GetObjectResponse getObjectResponse = getObjectResponseResponseInputStream.response();

    return new RetrieveResponse(
        MediaType.valueOf(getObjectResponse.contentType()),
        new ByteArrayResource(getObjectResponseResponseInputStream.readAllBytes()) {
          @Override
          public String getFilename() {
            final Map<String, String> metadata = getObjectResponse.metadata();
            final String filename = metadata.get("filename");
            if (StringUtils.isEmpty(filename)) {
              return ingestId;
            }
            return filename;
          }
        });
  }
}
