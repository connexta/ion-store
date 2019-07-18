/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.adaptors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.connexta.multiintstore.common.exceptions.StorageException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3StorageAdaptor implements StorageAdaptor {

  private static final String FILE_NAME_METADATA_KEY = "filename";

  private final String s3BucketQuarantine;
  private final TransferManager s3TransferManager;

  public S3StorageAdaptor(
      @NotNull final TransferManager s3TransferManager, @NotEmpty final String s3BucketQuarantine) {
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
  //  @Override
  //  @NotNull
  //  public RetrieveResponse retrieve(@NotEmpty final String key) throws StorageException {
  //    log.info("Retrieving product in bucket \"{}\" with key \"{}\"", s3BucketQuarantine, key);
  //
  //    ResponseInputStream<GetObjectResponse> getObjectResponseResponseInputStream = null;
  //    try {
  //      try {
  //        getObjectResponseResponseInputStream =
  //                s3Client.getObject(
  //                        GetObjectRequest.builder().bucket(s3BucketQuarantine).key(key).build());
  //      } catch (SdkException e) {
  //        throw new StorageException("Unable to retrieve product with key " + key, e);
  //      }
  //      final GetObjectResponse getObjectResponse =
  // getObjectResponseResponseInputStream.response();
  //
  //      final String fileName = getObjectResponse.metadata().get(FILE_NAME_METADATA_KEY);
  //      if (StringUtils.isEmpty(fileName)) {
  //        throw new StorageException(
  //                String.format(
  //                        "Expected S3 object to have a non-null metadata value for %s",
  //                        FILE_NAME_METADATA_KEY));
  //      }
  //
  //      return new RetrieveResponse(
  //              MediaType.valueOf(getObjectResponse.contentType()),
  //              getObjectResponseResponseInputStream,
  //              fileName);
  //    } catch (Throwable t) {
  //      if (getObjectResponseResponseInputStream != null) {
  //        try {
  //          getObjectResponseResponseInputStream.close();
  //        } catch (IOException e) {
  //          log.warn("Unable to close InputStream when retrieving key \"{}\".", key, e);
  //        }
  //      }
  //
  //      throw t;
  //    }
  //  }
}
