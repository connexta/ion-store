/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import static com.connexta.store.controllers.StoreController.METACARD_MEDIA_TYPE;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.connexta.store.exceptions.StoreMetacardException;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3MetacardStorageAdaptor implements MetacardStorageAdaptor {

  private final String bucket;
  private final AmazonS3 amazonS3;
  private final TransferManager transferManager;

  public S3MetacardStorageAdaptor(@NotNull final AmazonS3 amazonS3, @NotBlank final String bucket) {
    this.amazonS3 = amazonS3;
    this.transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
    this.bucket = bucket;
  }

  @Override
  public void store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotNull final InputStream inputStream,
      @NotBlank final String key)
      throws StoreMetacardException {
    // TODO check if id already exists

    final ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(METACARD_MEDIA_TYPE.toString());
    objectMetadata.setContentLength(fileSize);

    log.info("Storing metacard in bucket \"{}\" with key \"{}\"", bucket, key);
    try {
      final Upload upload = transferManager.upload(bucket, key, inputStream, objectMetadata);
      log.info(String.format("Transfer state: %s", upload.getState()));
      upload.waitForCompletion();
      log.info(String.format("Transfer state: %s", upload.getState()));
    } catch (RuntimeException | InterruptedException e) {
      throw new StoreMetacardException(
          String.format("Unable to store metacard in bucket \"%s\" with key \"%s\"", bucket, key),
          e);
    }

    log.info("Successfully stored metacard in bucket \"{}\" with key \"{}\"", bucket, key);
  }

  /** The caller is responsible for closing the returned {@link InputStream}. */
  @Override
  @NotNull
  public InputStream retrieve(@NotBlank final String key) throws StoreMetacardException {
    log.info("Retrieving product in bucket \"{}\" with key \"{}\"", bucket, key);

    S3Object s3Object;
    InputStream productInputStream = null;
    try {
      try {
        s3Object = amazonS3.getObject(new GetObjectRequest(bucket, key));
      } catch (SdkClientException e) {
        throw new StoreMetacardException("Unable to retrieve metacard with key " + key, e);
      }

      productInputStream = s3Object.getObjectContent();

      return productInputStream;
    } catch (Throwable t) {
      if (productInputStream != null) {
        try {
          productInputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving metacard with key \"{}\".", key, e);
        }
      }

      throw t;
    }
  }
}
