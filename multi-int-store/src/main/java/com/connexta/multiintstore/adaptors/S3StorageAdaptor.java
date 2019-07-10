/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.adaptors;

import com.connexta.multiintstore.common.exceptions.StorageException;
import java.io.IOException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.ImmutableMap;

@Slf4j
@Service
public class S3StorageAdaptor implements StorageAdaptor {

  private static final String FILE_NAME_METADATA_KEY = "filename";

  private final String s3BucketQuarantine;
  private final S3Client s3Client;

  public S3StorageAdaptor(final S3Client s3Client, final String s3BucketQuarantine) {
    this.s3Client = s3Client;
    this.s3BucketQuarantine = s3BucketQuarantine;
  }

  @Override
  public void store(
      @NotEmpty final String mimeType,
      @NotNull final MultipartFile file,
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotEmpty final String fileName,
      @NotEmpty final String key)
      throws IOException, StorageException {
    final PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(s3BucketQuarantine)
            .key(key)
            .contentType(mimeType)
            .contentLength(fileSize)
            .metadata(ImmutableMap.of(FILE_NAME_METADATA_KEY, fileName))
            .build();
    final RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), fileSize);

    log.info("Storing {} in bucket \"{}\" with key \"{}\"", fileName, s3BucketQuarantine, key);
    try {
      s3Client.putObject(putObjectRequest, requestBody);
    } catch (SdkServiceException e) {
      throw new StorageException(
          "S3 was unable to store " + key + " in bucket " + s3BucketQuarantine, e);
    } catch (SdkClientException e) {
      throw new StorageException(
          "S3 was unavailable and could not store " + key + " in bucket " + s3BucketQuarantine, e);
    } catch (RuntimeException e) {
      throw new StorageException("Error storing " + key + " in bucket " + s3BucketQuarantine, e);
    }
    log.info(
        "Successfully stored \"{}\" in bucket \"{}\" with key \"{}\"",
        fileName,
        s3BucketQuarantine,
        key);
  }

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * RetrieveResponse}.
   */
  @Override
  @NotNull
  public RetrieveResponse retrieve(@NotEmpty final String key) throws StorageException {
    log.info("Retrieving product in bucket \"{}\" with key \"{}\"", s3BucketQuarantine, key);

    ResponseInputStream<GetObjectResponse> getObjectResponseResponseInputStream = null;
    try {
      try {
        getObjectResponseResponseInputStream =
            s3Client.getObject(
                GetObjectRequest.builder().bucket(s3BucketQuarantine).key(key).build());
      } catch (SdkException e) {
        throw new StorageException("Unable to retrieve product with key " + key, e);
      }
      final GetObjectResponse getObjectResponse = getObjectResponseResponseInputStream.response();

      final String fileName = getObjectResponse.metadata().get(FILE_NAME_METADATA_KEY);
      if (StringUtils.isEmpty(fileName)) {
        throw new StorageException(
            String.format(
                "Expected S3 object to have a non-null metadata value for %s",
                FILE_NAME_METADATA_KEY));
      }

      return new RetrieveResponse(
          MediaType.valueOf(getObjectResponse.contentType()),
          getObjectResponseResponseInputStream,
          fileName);
    } catch (Throwable t) {
      if (getObjectResponseResponseInputStream != null) {
        try {
          getObjectResponseResponseInputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", key, e);
        }
      }

      throw t;
    }
  }
}
