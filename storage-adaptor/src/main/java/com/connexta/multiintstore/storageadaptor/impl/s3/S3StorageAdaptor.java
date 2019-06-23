/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storageadaptor.impl.s3;

import com.connexta.multiintstore.storageadaptor.RetrieveResponse;
import com.connexta.multiintstore.storageadaptor.StorageAdaptor;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** TODO one adaptor for quarantine and one adaptor for multi-int-store */
public class S3StorageAdaptor implements StorageAdaptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageAdaptor.class);

  @NotNull private final S3Client s3Client;
  @NotEmpty private final String s3Bucket;

  public S3StorageAdaptor(@NotNull final S3Client s3Client, @NotEmpty final String s3Bucket) {
    this.s3Client = s3Client;
    this.s3Bucket = s3Bucket;
  }

  @Override
  public void store(
      @NotEmpty String mimeType,
      @NotNull MultipartFile file,
      @NotEmpty String fileName,
      @NotEmpty String key)
      throws IOException {
    LOGGER.info("Storing {} in bucket \"{}\" with key \"{}\"", fileName, s3Bucket, key);
    final long size = file.getSize();

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .contentType(mimeType)
            .contentLength(size)
            .metadata(ImmutableMap.of("filename", fileName))
            .build(),
        RequestBody.fromInputStream(file.getInputStream(), size));
  }

  @Override
  public RetrieveResponse retrieve(String ingestId) throws IOException {
    LOGGER.info("Retrieving product in bucket \"{}\" with key \"{}\"", s3Bucket, ingestId);

    final ResponseInputStream<GetObjectResponse> getObjectResponseResponseInputStream =
        s3Client.getObject(GetObjectRequest.builder().bucket(s3Bucket).key(ingestId).build());
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
