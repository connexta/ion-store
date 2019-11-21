/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import static com.connexta.store.adaptors.StoreStatus.STAGED;
import static com.connexta.store.adaptors.StoreStatus.STATUS_KEY;
import static com.connexta.store.adaptors.StoreStatus.STORED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.connexta.store.config.AmazonS3Configuration;
import com.connexta.store.exceptions.DatasetNotFoundException;
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
import org.springframework.http.MediaType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class S3StorageAdaptorITests {

  public static final String ASDF = "asdf";
  public static final String DATASET_ID = "123e4567e89b12d3a456426655440000";
  public static final String ACCESS_KEY = "access-key";
  public static final String SECRET_KEY = "secret-key";
  private static final String DOCKER_IMAGE_NAME = "localstack/localstack:0.10.5";
  private static final int LOCALSTACK_PORT = 4572;
  private static String BUCKET = "metacard-quarantine";
  private static StorageAdaptor storageAdaptor;
  private static AmazonS3Configuration configuration;

  private static AmazonS3 amazonS3;

  @Container
  public static final GenericContainer s3Container =
      new GenericContainer(DOCKER_IMAGE_NAME)
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
            ACCESS_KEY,
            SECRET_KEY);

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
  public void testSuccessfulStoreRequest() {
    storageAdaptor.store(
        4L,
        MediaType.APPLICATION_XML_VALUE,
        new ByteArrayInputStream(ASDF.getBytes()),
        DATASET_ID,
        Map.of());

    assertTrue(amazonS3.doesObjectExist(BUCKET, DATASET_ID));
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

  @Test
  void testUpdatingStoreStatus() {
    // Store a file and set status as staged
    storageAdaptor.store(
        4L,
        MediaType.APPLICATION_XML_VALUE,
        new ByteArrayInputStream(ASDF.getBytes()),
        DATASET_ID,
        Map.of());
    assertEquals(STAGED, getStatusTag(DATASET_ID));

    // Promote file to stored status
    storageAdaptor.updateStatus(DATASET_ID, STORED);
    assertEquals(STORED, getStatusTag(DATASET_ID));
  }

  @Test
  void testUpdatingStoreStatusInvalidDatasetId() {
    assertThrows(
        DatasetNotFoundException.class, () -> storageAdaptor.updateStatus(DATASET_ID, STORED));
  }

  private String getStatusTag(String datasetId) {
    return amazonS3
        .getObjectTagging(new GetObjectTaggingRequest(BUCKET, datasetId))
        .getTagSet()
        .get(0)
        .withKey(STATUS_KEY)
        .getValue();
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
