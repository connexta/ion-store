/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common.adaptors;

import com.connexta.multiintstore.common.exceptions.StorageException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.ImmutableMap;

/** TODO one adaptor for quarantine and one adaptor for multi-int-store */
@Service
public class S3StorageAdaptor implements StorageAdaptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageAdaptor.class);
  private final String s3BucketQuarantine;
  private final S3Client s3Client;

  public S3StorageAdaptor(final S3Client s3Client, final String s3BucketQuarantine) {
    this.s3Client = s3Client;
    this.s3BucketQuarantine = s3BucketQuarantine;
  }

  @Override
  public void store(
      final String mimeType,
      final MultipartFile file,
      final Long fileSize,
      final String fileName,
      final String key)
      throws IOException, StorageException {
    final PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(s3BucketQuarantine)
            .key(key)
            .contentType(mimeType)
            .contentLength(fileSize)
            .metadata(ImmutableMap.of("filename", fileName))
            .build();
    final RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), fileSize);

    LOGGER.info("Storing {} in bucket \"{}\" with key \"{}\"", fileName, s3BucketQuarantine, key);
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
  }

  // Uncomment when the MIS implements retrieval
  //  @Override
  //  public RetrieveResponse retrieve(String ingestId) throws IOException {
  //    LOGGER.info(
  //        "Retrieving product in bucket \"{}\" with key \"{}\"", s3BucketQuarantine, ingestId);
  //
  //    final ResponseInputStream<GetObjectResponse> getObjectResponseResponseInputStream =
  //        s3Client.getObject(
  //            GetObjectRequest.builder().bucket(s3BucketQuarantine).key(ingestId).build());
  //    final GetObjectResponse getObjectResponse = getObjectResponseResponseInputStream.response();
  //
  //    return new RetrieveResponse(
  //        MediaType.valueOf(getObjectResponse.contentType()),
  //        new ByteArrayResource(getObjectResponseResponseInputStream.readAllBytes()) {
  //          @Override
  //          public String getFilename() {
  //            final Map<String, String> metadata = getObjectResponse.metadata();
  //            final String filename = metadata.get("filename");
  //            if (StringUtils.isEmpty(filename)) {
  //              return ingestId;
  //            }
  //            return filename;
  //          }
  //        });
  //  }
}
