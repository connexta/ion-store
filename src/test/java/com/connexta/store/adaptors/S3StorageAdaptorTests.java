/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.StoreException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S3StorageAdaptorTests {
  private static final String BUCKET = "mr-test-bucket";
  private static final Long FILE_SIZE = 100L;
  private static final String MEDIA_TYPE = "text/plain";
  private static final InputStream INPUT_STREAM = mock(InputStream.class);
  private static final String DATASET_ID = "123e4567e89b12d3a456426655440000";
  private static final Map<String, String> OBJECT_METADATA = Map.of("filename", "text.txt");

  private static AmazonS3 mockAmazonS3;
  private static StorageAdaptor storageAdaptor;

  @BeforeAll
  static void setUp() {
    mockAmazonS3 = mock(AmazonS3.class);
    storageAdaptor = new S3StorageAdaptor(mockAmazonS3, BUCKET);
  }

  @Test
  void testStoringWhenBucketDoesNotExist() {
    assertThrows(
        StoreException.class,
        () ->
            storageAdaptor.store(FILE_SIZE, MEDIA_TYPE, INPUT_STREAM, DATASET_ID, OBJECT_METADATA));
  }

  @Test
  void testRetrieveFailsGettingInputStream() throws IOException {
    // given
    S3Object mockS3Object = mock(S3Object.class);
    InputStream mockInputStream = mock(InputStream.class);
    when(mockAmazonS3.doesObjectExist(eq(BUCKET), anyString())).thenReturn(true);
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Object);
    when(mockAmazonS3.doesBucketExistV2(eq(BUCKET))).thenReturn(true);
    when(mockS3Object.getObjectContent()).thenThrow(new SdkClientException("test"));

    // verify
    assertThrows(RetrieveException.class, () -> storageAdaptor.retrieve(DATASET_ID));
  }

  @Test
  void testRetrieveFailsGettingS3Object() throws IOException {
    // given
    S3Object mockS3Object = mock(S3Object.class);
    InputStream mockInputStream = mock(InputStream.class);
    when(mockAmazonS3.doesObjectExist(eq(BUCKET), anyString())).thenReturn(true);
    when(mockAmazonS3.doesBucketExistV2(eq(BUCKET))).thenReturn(true);
    when(mockAmazonS3.getObject(any(GetObjectRequest.class)))
        .thenThrow(new SdkClientException("test"));

    // verify
    assertThrows(RetrieveException.class, () -> storageAdaptor.retrieve(DATASET_ID));
  }

  @Test
  void testInputStreamIsAlwaysClosed() throws IOException {
    // given
    S3Object mockS3Object = mock(S3Object.class);
    S3ObjectInputStream mockInputStream = mock(S3ObjectInputStream.class);
    when(mockAmazonS3.doesObjectExist(eq(BUCKET), anyString())).thenReturn(true);
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Object);
    when(mockAmazonS3.doesBucketExistV2(eq(BUCKET))).thenReturn(true);
    when(mockS3Object.getObjectContent()).thenReturn(mockInputStream);

    // when
    assertThrows(RetrieveException.class, () -> storageAdaptor.retrieve(DATASET_ID));

    // verify
    verify(mockInputStream, times(1)).close();
  }

  @Test
  void testS3ReturnsNullObject() throws IOException {
    // given
    S3ObjectInputStream mockInputStream = mock(S3ObjectInputStream.class);
    when(mockAmazonS3.doesObjectExist(eq(BUCKET), anyString())).thenReturn(true);
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(null);
    when(mockAmazonS3.doesBucketExistV2(eq(BUCKET))).thenReturn(true);

    // verify
    assertThrows(RetrieveException.class, () -> storageAdaptor.retrieve(DATASET_ID));
  }

  @Test
  void testDeleteSdkException() {
    // given
    AmazonS3 mockS3 = mock(AmazonS3.class);
    when(mockS3.doesBucketExistV2(BUCKET)).thenReturn(true);
    when(mockS3.doesObjectExist(BUCKET, DATASET_ID)).thenReturn(true);
    doThrow(new SdkClientException("test exception")).when(mockS3).deleteObject(BUCKET, DATASET_ID);
    StorageAdaptor sdkExceptionStorageAdaptor = new S3StorageAdaptor(mockS3, BUCKET);

    // verify
    assertThrows(QuarantineException.class, () -> sdkExceptionStorageAdaptor.delete(DATASET_ID));
  }
}
