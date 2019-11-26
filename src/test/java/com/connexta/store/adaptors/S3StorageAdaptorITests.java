/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import static com.connexta.store.adaptors.StoreStatus.QUARANTINED;
import static com.connexta.store.adaptors.StoreStatus.STAGED;
import static com.connexta.store.adaptors.StoreStatus.STORED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.exceptions.DatasetNotFoundException;
import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.StoreException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class S3StorageAdaptorITests {

  public static final String ASDF = "asdf";
  public static final String DATASET_ID = "123e4567e89b12d3a456426655440000";
  private static final int LOCALSTACK_PORT = 4572;
  private static String BUCKET = "metacard-quarantine";
  public static final String KEY = "1234";
  private static StorageAdaptor storageAdaptor;
  private static AmazonS3Configuration configuration;

  private static AmazonS3 amazonS3;

  @Container
  public static final GenericContainer s3Container =
      new GenericContainer("localstack/localstack:0.10.5")
          .withExposedPorts(LOCALSTACK_PORT)
          .withEnv("SERVICES", "s3")
          .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Ready\\.\n"));

  @BeforeAll
  public static void setUp() {
    configuration =
        new AmazonS3Configuration(
            String.format(
                "http://%s:%d",
                s3Container.getContainerIpAddress(), s3Container.getMappedPort(LOCALSTACK_PORT)),
            "local",
            "access-key",
            "secret-key");

    amazonS3 =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    configuration.getEndpoint(), configuration.getRegion()))
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                        configuration.getAccessKey(), configuration.getSecretKey())))
            .enablePathStyleAccess()
            .build();

    storageAdaptor = new S3StorageAdaptor(amazonS3, BUCKET);
  }

  @BeforeEach
  public void beforeEach() {
    amazonS3.createBucket(BUCKET);
  }

  @AfterEach
  public void afterEach() {
    // Clear out S3
    amazonS3.listObjects(BUCKET).getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .forEach(key -> amazonS3.deleteObject(BUCKET, key));
    amazonS3.deleteBucket(BUCKET);
  }

  @Test
  public void testSuccessfulStoreRequest() throws IOException {
    storageAdaptor.store(
        4L,
        MediaType.APPLICATION_XML_VALUE,
        new ByteArrayInputStream(ASDF.getBytes()),
        DATASET_ID,
        Map.of());

    assertThat(storageAdaptor.retrieve(DATASET_ID).getInputStream(), hasContents(ASDF));
    assertThat(storageAdaptor.getStatus(DATASET_ID), equalTo(STAGED));
  }

  @Test
  public void testSuccessfulRetrieveRequest() {
    final String key = DATASET_ID;
    final String metacardContents = ASDF;
    storageAdaptor.store(
        4L,
        MediaType.APPLICATION_XML_VALUE,
        new ByteArrayInputStream(metacardContents.getBytes()),
        key,
        Map.of());
    assertThat(storageAdaptor.retrieve(key).getInputStream(), hasContents(metacardContents));
  }

  @Test
  public void testRetrieveRequestWrongKey() {
    String key = DATASET_ID;
    storageAdaptor.store(
        4L,
        MediaType.APPLICATION_XML_VALUE,
        new ByteArrayInputStream(ASDF.getBytes()),
        key,
        Map.of());
    assertThrows(DatasetNotFoundException.class, () -> storageAdaptor.retrieve("wrong_key"));
  }

  @Test
  public void testStoringDuplicateKey() {
    // TODO add code for checking duplicate products.
  }

  @Test
  public void testStoreWithContentLengthNotMatching() {
    assertThrows(
        StoreException.class,
        () -> {
          storageAdaptor.store(
              10L,
              MediaType.APPLICATION_XML_VALUE,
              new ByteArrayInputStream(ASDF.getBytes()),
              DATASET_ID,
              Map.of());
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {STORED, QUARANTINED})
  void testUpdatingStoreStatus(String status) {
    // Store a file and set status as staged
    storageAdaptor.store(
        4L,
        MediaType.APPLICATION_XML_VALUE,
        new ByteArrayInputStream(ASDF.getBytes()),
        DATASET_ID,
        Map.of());

    // Promote file to stored status
    storageAdaptor.updateStatus(DATASET_ID, status);

    // Confirm the new status
    assertThat(storageAdaptor.getStatus(DATASET_ID), equalTo(status));
  }

  @Test
  void testUpdatingStoreStatusInvalidDatasetId() {
    assertThrows(
        DatasetNotFoundException.class, () -> storageAdaptor.updateStatus(DATASET_ID, STORED));
  }

  @Test
  void testDeleteSuccess() {
    storageAdaptor.store(
        4L,
        MediaType.APPLICATION_XML_VALUE,
        new ByteArrayInputStream(ASDF.getBytes()),
        KEY,
        Map.of());
    assertThat(storageAdaptor.retrieve(KEY).getInputStream(), hasContents(ASDF));
    storageAdaptor.delete(KEY);
    assertThrows(DatasetNotFoundException.class, () -> storageAdaptor.retrieve(KEY));
  }

  @Test
  void testDeleteBucketDoesNotExist() {
    amazonS3.deleteBucket(BUCKET);
    assertThrows(RetrieveException.class, () -> storageAdaptor.delete(KEY));
    amazonS3.createBucket(BUCKET);
  }

  @Test
  void testDeleteSdkException() {
    AmazonS3 mockS3 = mock(AmazonS3.class);
    when(mockS3.doesBucketExistV2(BUCKET)).thenReturn(true);
    when(mockS3.doesObjectExist(BUCKET, KEY)).thenReturn(true);
    doThrow(new SdkClientException("test exception")).when(mockS3).deleteObject(BUCKET, KEY);
    StorageAdaptor sdkExceptionStorageAdaptor = new S3StorageAdaptor(mockS3, BUCKET);
    assertThrows(QuarantineException.class, () -> sdkExceptionStorageAdaptor.delete(KEY));
  }

  @NotNull
  private static Matcher<InputStream> hasContents(final String expectedContents) {
    return new TypeSafeMatcher<InputStream>() {
      @Override
      protected boolean matchesSafely(InputStream actual) {
        try {
          return IOUtils.contentEquals(
              new ByteArrayInputStream(expectedContents.getBytes()), actual);
        } catch (final IOException e) {
          fail("Unable to compare input streams", e);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("contents is " + expectedContents);
      }
    };
  }
}
