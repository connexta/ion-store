/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.adaptors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.connexta.multiintstore.common.exceptions.StorageException;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

@Slf4j
public class S3StorageAdaptor implements StorageAdaptor {

  private static final String FILE_NAME_METADATA_KEY = "filename";

  private final String s3BucketQuarantine;
  private final TransferManager s3TransferManager;
  private final AmazonS3 s3Client;

  public S3StorageAdaptor(
      @NotNull final AmazonS3 s3Client,
      @NotNull final TransferManager s3TransferManager,
      @NotEmpty final String s3BucketQuarantine) {
    this.s3Client = s3Client;
    this.s3TransferManager = s3TransferManager;
    this.s3BucketQuarantine = s3BucketQuarantine;
  }

  @Override
  public void store(
      @NotEmpty final String mimeType,
      @NotNull final InputStream inputStream,
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotEmpty final String fileName,
      @NotEmpty final String key)
      throws StorageException {

    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(mimeType);
    objectMetadata.setContentLength(fileSize);
    objectMetadata.addUserMetadata(FILE_NAME_METADATA_KEY, fileName);

    log.info("Storing {} in bucket \"{}\" with key \"{}\"", fileName, s3BucketQuarantine, key);
    try {
      Upload upload =
          s3TransferManager.upload(s3BucketQuarantine, key, inputStream, objectMetadata);
      log.info("Waiting for the storing process to finish...");
      upload.waitForUploadResult();
    } catch (AmazonServiceException e) {
      throw new StorageException(
          "S3 was unable to store " + key + " in bucket " + s3BucketQuarantine, e);
    } catch (AmazonClientException e) {
      throw new StorageException(
          "S3 was unavailable and could not store " + key + " in bucket " + s3BucketQuarantine, e);
    } catch (RuntimeException | InterruptedException e) {
      throw new StorageException("Error storing " + key + " in bucket " + s3BucketQuarantine, e);
    } finally {
      s3TransferManager.shutdownNow();
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

    S3Object s3Object = null;
    InputStream objectInputStream = null;
    ObjectMetadata objectMetadata = null;

    try {
      try {
        s3Object = s3Client.getObject(s3BucketQuarantine, key);

        objectInputStream = s3Object.getObjectContent();
        objectMetadata = s3Object.getObjectMetadata();

      } catch (AmazonServiceException e) {
        throw new StorageException("Unable to retrieve the product from S3 with key " + key, e);
      } catch (SdkClientException e) {
        throw new StorageException(
            "Unable to get a response from S3 when retrieving the product with " + key, e);
      } finally {
        try {
          s3Object.close();
        } catch (IOException e) {
          log.warn("Unable to close S3Object when retrieving key \"{}\".", key, e);
        }
      }

      final String fileName =
          s3Object.getObjectMetadata().getUserMetaDataOf(FILE_NAME_METADATA_KEY);
      if (StringUtils.isEmpty(fileName)) {
        throw new StorageException(
            String.format(
                "Expected S3 object to have a non-null metadata value for %s",
                FILE_NAME_METADATA_KEY));
      }

      return new RetrieveResponse(
          MediaType.valueOf(objectMetadata.getContentType()), objectInputStream, fileName);
    } catch (Throwable t) {
      if (objectInputStream != null) {
        try {
          objectInputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", key, e);
        }
      }
      throw t;
    }
  }
}
