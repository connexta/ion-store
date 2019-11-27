/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.connexta.store.exceptions.DatasetNotFoundException;
import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.StoreException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

@Slf4j
public class S3StorageAdaptor implements StorageAdaptor {

  private static final String STATUS_KEY = "status";

  private final String bucket;
  private final AmazonS3 amazonS3;
  private final TransferManager transferManager;

  public S3StorageAdaptor(@NotNull final AmazonS3 amazonS3, @NotBlank final String bucket) {
    this.amazonS3 = amazonS3;
    this.transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
    this.bucket = bucket;
  }

  @Override
  public void store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      final String mediaType,
      @NotNull final InputStream inputStream,
      @NotBlank final String key,
      Map<String, String> metadata)
      throws StoreException {
    // TODO check if id already exists

    final ObjectMetadata objectMetadata = new ObjectMetadata();
    if (!mediaType.isEmpty()) {
      objectMetadata.setContentType(mediaType);
    }
    if (!metadata.isEmpty()) {
      objectMetadata.setUserMetadata(metadata);
    }
    objectMetadata.setContentLength(fileSize);

    log.info("Storing item in bucket \"{}\" with key \"{}\"", bucket, key);

    if (!amazonS3.doesBucketExistV2(bucket)) {
      throw new StoreException(String.format("Bucket %s does not exist", bucket));
    }

    try {
      PutObjectRequest putObjectRequest =
          new PutObjectRequest(bucket, key, inputStream, objectMetadata);
      putObjectRequest.withTagging(
          new ObjectTagging(ImmutableList.of(new Tag(STATUS_KEY, StoreStatus.STAGED))));
      final Upload upload = transferManager.upload(putObjectRequest);
      log.info(String.format("Transfer state: %s", upload.getState()));
      upload.waitForCompletion();
      log.info(String.format("Transfer state: %s", upload.getState()));
    } catch (RuntimeException | InterruptedException e) {
      throw new StoreException(
          String.format("Unable to store item in bucket \"%s\" with key \"%s\"", bucket, key), e);
    }

    log.info("Successfully stored item in bucket \"{}\" with key \"{}\"", bucket, key);
  }

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * StorageAdaptorRetrieveResponse}.
   */
  @Override
  @NotNull
  public StorageAdaptorRetrieveResponse retrieve(@NotBlank final String key)
      throws RetrieveException {
    log.info("Retrieving item in bucket \"{}\" with key \"{}\"", bucket, key);

    final S3Object s3Object = getS3Object(key);
    final ObjectMetadata objectMetadata = s3Object.getObjectMetadata();

    InputStream inputStream = null;
    try {
      inputStream = s3Object.getObjectContent();
      return new StorageAdaptorRetrieveResponse(
          MediaType.valueOf(objectMetadata.getContentType()),
          inputStream,
          objectMetadata.getUserMetadata());
    } catch (final Throwable t) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (final IOException e) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", key, e);
        }
      }
      throw t;
    }
  }

  @Override
  public void updateStatus(@NotBlank String key, @NotBlank String status) {
    if (s3ObjectExists(bucket, key)) {
      final Tag updatedTag = new Tag(STATUS_KEY, status);
      final ObjectTagging objectTagging = new ObjectTagging(ImmutableList.of(updatedTag));
      amazonS3.setObjectTagging(new SetObjectTaggingRequest(bucket, key, objectTagging));
    } else {
      throw new DatasetNotFoundException(key);
    }
  }

  @Override
  @Nullable
  public String getStatus(@NotBlank String key) throws DatasetNotFoundException {
    if (!s3ObjectExists(bucket, key)) {
      throw new DatasetNotFoundException(key);
    }
    GetObjectTaggingResult taggingResult =
        amazonS3.getObjectTagging(new GetObjectTaggingRequest(bucket, key));
    List<Tag> tags = taggingResult.getTagSet();

    Optional<Tag> statusTag =
        tags.stream().filter(tag -> StringUtils.equals(tag.getKey(), STATUS_KEY)).findFirst();
    return statusTag.get().getValue();
  }

  public void delete(@NotBlank String key) {
    log.info("Deleting item in bucket \"{}\" with key \"{}\"", bucket, key);
    try {
      if (!s3ObjectExists(bucket, key)) {
        throw new QuarantineException(String.format("Bucket %s does not exist", bucket));
      }
      amazonS3.deleteObject(bucket, key);
    } catch (final SdkClientException e) {
      throw new QuarantineException(
          String.format("Unable to delete item with key %s: %s", key, e.getMessage()), e);
    }
  }

  @NotNull
  private S3Object getS3Object(@NotBlank final String key) throws RetrieveException {
    final S3Object s3Object;
    try {
      if (!s3ObjectExists(bucket, key)) {
        throw new DatasetNotFoundException(key);
      }
      s3Object = amazonS3.getObject(new GetObjectRequest(bucket, key));
    } catch (final SdkClientException e) {
      throw new RetrieveException(
          String.format("Unable to retrieve item with key %s: %s", key, e.getMessage()), e);
    }

    if (null == s3Object) {
      throw new RetrieveException(
          String.format(
              "Unable to retrieve item with key %s: constraints were specified but not met", key));
    }

    return s3Object;
  }

  private boolean s3ObjectExists(@NotBlank String bucket, @NotBlank String key) {
    if (!amazonS3.doesBucketExistV2(bucket)) {
      throw new RetrieveException(String.format("Bucket %s does not exist", bucket));
    }
    return amazonS3.doesObjectExist(bucket, key);
  }
}
