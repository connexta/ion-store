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
import com.amazonaws.services.s3.model.GetObjectRequest;
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
  private final AmazonS3 s3Client;
  private final TransferManager transferManager;

  public S3StorageAdaptor(
      @NotNull final AmazonS3 s3Client,
      @NotNull final TransferManager transferManager,
      @NotEmpty final String s3BucketQuarantine) {
    this.s3Client = s3Client;
    this.transferManager = transferManager;
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
      Upload upload = transferManager.upload(s3BucketQuarantine, key, inputStream, objectMetadata);
      log.info(String.format("Transfer state: %s", upload.getState()));
      upload.waitForCompletion();
      log.info(String.format("Transfer state: %s", upload.getState()));
    } catch (AmazonServiceException e) {
      throw new StorageException(
          "S3 was unable to store " + key + " in bucket " + s3BucketQuarantine, e);
    } catch (AmazonClientException e) {
      throw new StorageException(
          "S3 was unavailable and could not store " + key + " in bucket " + s3BucketQuarantine, e);
    } catch (InterruptedException e) {
      throw new StorageException(
          "An error occurred while waiting to store " + key + " in bucket " + s3BucketQuarantine,
          e);
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

    S3Object s3Object;
    InputStream productInputStream = null;
    try {
      try {
        s3Object = s3Client.getObject(new GetObjectRequest(s3BucketQuarantine, key));
      } catch (SdkClientException e) {
        throw new StorageException("Unable to retrieve product with key " + key, e);
      }

      final String fileName =
          s3Object.getObjectMetadata().getUserMetaDataOf(FILE_NAME_METADATA_KEY);
      if (StringUtils.isEmpty(fileName)) {
        throw new StorageException(
            String.format(
                "Expected S3 object to have a non-null metadata value for %s",
                FILE_NAME_METADATA_KEY));
      }
      productInputStream = s3Object.getObjectContent();

      return new RetrieveResponse(
          MediaType.valueOf(s3Object.getObjectMetadata().getContentType()),
          productInputStream,
          fileName);
    } catch (Throwable t) {
      if (productInputStream != null) {
        try {
          productInputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", key, e);
        }
      }

      throw t;
    }
  }
}
