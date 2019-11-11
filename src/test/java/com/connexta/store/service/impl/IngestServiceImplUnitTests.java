/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.connexta.store.adaptors.MetacardStorageAdaptor;
import com.connexta.store.clients.StoreClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.exceptions.StoreException;
import com.connexta.store.exceptions.StoreMetacardException;
import com.connexta.store.exceptions.TransformException;
import com.connexta.store.service.api.IngestService;
import java.io.InputStream;
import java.net.URI;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IngestServiceImplUnitTests {

  @Mock private StoreClient mockStoreClient;
  @Mock private MetacardStorageAdaptor mockMetacardStorageAdaptor;
  @Mock private TransformClient mockTransformClient;

  // TODO test invalid constructor arguments

  @Test
  public void testIngest() throws Exception {
    // given
    final IngestService ingestService =
        new IngestServiceImpl(
            mockStoreClient,
            mockMetacardStorageAdaptor,
            "http://localhost:9040/ingest/",
            mockTransformClient);

    final Long fileSize = 1L;
    final String mimeType = "mimeType";
    final InputStream inputStream = mock(InputStream.class);
    final String fileName = "fileName";
    final long metacardFileSize = 1L;
    final InputStream metacardInputStream = mock(InputStream.class);
    final URI location = new URI("http://localhost:9041/mis/product/1234");
    when(mockStoreClient.store(fileSize, mimeType, inputStream, fileName)).thenReturn(location);

    // when
    assertDoesNotThrow(
        () ->
            ingestService.ingest(
                fileSize, mimeType, inputStream, fileName, metacardFileSize, metacardInputStream));

    // then
    final InOrder inOrder =
        Mockito.inOrder(mockStoreClient, mockMetacardStorageAdaptor, mockTransformClient);
    // TODO Verify the key was generated as expected instead of anyString() and any(URI.class) here.
    // Maybe extract a key generator.
    inOrder
        .verify(mockMetacardStorageAdaptor)
        .store(eq(metacardFileSize), eq(metacardInputStream), anyString());
    inOrder
        .verify(mockTransformClient)
        .requestTransform(eq(location), eq(mimeType), any(URI.class));
  }

  /* START ingest StoreClient tests */

  @Test
  public void testIngestWhenStoreClientThrowsStoreException() {
    // given
    final IngestService ingestService =
        new IngestServiceImpl(
            mockStoreClient,
            mockMetacardStorageAdaptor,
            "http://localhost:9040/ingest/",
            mockTransformClient);

    final Long fileSize = 1L;
    final String mimeType = "mimeType";
    final InputStream inputStream = mock(InputStream.class);
    final String fileName = "fileName";
    final StoreException storeException = mock(StoreException.class);
    when(mockStoreClient.store(fileSize, mimeType, inputStream, fileName))
        .thenThrow(storeException);

    // when
    final StoreException thrown =
        assertThrows(
            StoreException.class,
            () ->
                ingestService.ingest(
                    fileSize, mimeType, inputStream, fileName, 1L, mock(InputStream.class)));

    // then
    assertThat(thrown, is(storeException));
  }

  @Test
  public void testIngestWhenStoreClientThrowsThrowable() {
    // given
    final IngestService ingestService =
        new IngestServiceImpl(
            mockStoreClient,
            mockMetacardStorageAdaptor,
            "http://localhost:9040/ingest/",
            mockTransformClient);

    final Long fileSize = 1L;
    final String mimeType = "mimeType";
    final InputStream inputStream = mock(InputStream.class);
    final String fileName = "fileName";
    final Throwable throwable = mock(StoreException.class);
    when(mockStoreClient.store(fileSize, mimeType, inputStream, fileName)).thenThrow(throwable);

    // when
    final Throwable thrown =
        assertThrows(
            Throwable.class,
            () ->
                ingestService.ingest(
                    fileSize, mimeType, inputStream, fileName, 1L, mock(InputStream.class)));

    // then
    assertThat(thrown, is(throwable));
  }

  /* END ingest StoreClient tests */

  /* START ingest MetacardStorageAdaptor tests */

  @Test
  public void testIngestWhenMetacardStorageAdaptorThrowsStoreMetacardException() throws Exception {
    // given
    final IngestService ingestService =
        new IngestServiceImpl(
            mockStoreClient,
            mockMetacardStorageAdaptor,
            "http://localhost:9040/ingest/",
            mockTransformClient);

    final Long fileSize = 1L;
    final String mimeType = "mimeType";
    final InputStream inputStream = mock(InputStream.class);
    final String fileName = "fileName";
    final long metacardFileSize = 1L;
    final InputStream metacardInputStream = mock(InputStream.class);
    when(mockStoreClient.store(fileSize, mimeType, inputStream, fileName))
        .thenReturn(new URI("http://localhost:9041/mis/product/1234"));

    final StoreMetacardException storeMetacardException = mock(StoreMetacardException.class);
    doThrow(storeMetacardException)
        .when(mockMetacardStorageAdaptor)
        .store(eq(metacardFileSize), eq(metacardInputStream), anyString());

    // when
    final Throwable thrown =
        assertThrows(
            Throwable.class,
            () ->
                ingestService.ingest(
                    fileSize,
                    mimeType,
                    inputStream,
                    fileName,
                    metacardFileSize,
                    metacardInputStream));

    // then
    assertThat(thrown, is(storeMetacardException));
  }

  @Test
  public void testIngestWhenMetacardStorageAdaptorThrowsThrowable() throws Exception {
    // given
    final IngestService ingestService =
        new IngestServiceImpl(
            mockStoreClient,
            mockMetacardStorageAdaptor,
            "http://localhost:9040/ingest/",
            mockTransformClient);

    final Long fileSize = 1L;
    final String mimeType = "mimeType";
    final InputStream inputStream = mock(InputStream.class);
    final String fileName = "fileName";
    final long metacardFileSize = 1L;
    final InputStream metacardInputStream = mock(InputStream.class);
    when(mockStoreClient.store(fileSize, mimeType, inputStream, fileName))
        .thenReturn(new URI("http://localhost:9041/mis/product/1234"));

    final Throwable throwable = mock(StoreException.class);
    doThrow(throwable)
        .when(mockMetacardStorageAdaptor)
        .store(eq(metacardFileSize), eq(metacardInputStream), anyString());

    // when
    final Throwable thrown =
        assertThrows(
            Throwable.class,
            () ->
                ingestService.ingest(
                    fileSize,
                    mimeType,
                    inputStream,
                    fileName,
                    metacardFileSize,
                    metacardInputStream));

    // then
    assertThat(thrown, is(throwable));
  }

  /* END ingest MetacardStorageAdaptor tests */

  /* START ingest retrieveEndpoint tests */

  @Test
  public void testIngestWhenRetrieveEndpointCreatesInvalidURI() throws Exception {
    // given
    final IngestService ingestService =
        new IngestServiceImpl(
            mockStoreClient,
            mockMetacardStorageAdaptor,
            "this will cause the metacardLocation to be an invalid URI",
            mockTransformClient);

    final Long fileSize = 1L;
    final String mimeType = "mimeType";
    final InputStream inputStream = mock(InputStream.class);
    final String fileName = "fileName";
    final long metacardFileSize = 1L;
    final InputStream metacardInputStream = mock(InputStream.class);
    when(mockStoreClient.store(fileSize, mimeType, inputStream, fileName))
        .thenReturn(new URI("http://localhost:9041/mis/product/1234"));

    // when
    final StoreMetacardException storeMetacardException =
        assertThrows(
            StoreMetacardException.class,
            () ->
                ingestService.ingest(
                    fileSize,
                    mimeType,
                    inputStream,
                    fileName,
                    metacardFileSize,
                    metacardInputStream));

    // then
    assertThat(storeMetacardException.getReason(), equalTo("Unable to construct retrieve URI"));
  }

  /* END ingest retrieveEndpoint tests */

  /* START ingest TransformClient tests */

  @Test
  public void testIngestWhenTransformClientThrowsTransformException() throws Exception {
    // given
    final IngestService ingestService =
        new IngestServiceImpl(
            mockStoreClient,
            mockMetacardStorageAdaptor,
            "http://localhost:9040/ingest/",
            mockTransformClient);

    final Long fileSize = 1L;
    final String mimeType = "mimeType";
    final InputStream inputStream = mock(InputStream.class);
    final String fileName = "fileName";
    final long metacardFileSize = 1L;
    final InputStream metacardInputStream = mock(InputStream.class);
    final URI location = new URI("http://localhost:9041/mis/product/1234");
    when(mockStoreClient.store(fileSize, mimeType, inputStream, fileName)).thenReturn(location);

    final TransformException transformException = mock(TransformException.class);
    doThrow(transformException)
        .when(mockTransformClient)
        .requestTransform(eq(location), eq(mimeType), any(URI.class));

    // when
    final TransformException thrown =
        assertThrows(
            TransformException.class,
            () ->
                ingestService.ingest(
                    fileSize,
                    mimeType,
                    inputStream,
                    fileName,
                    metacardFileSize,
                    metacardInputStream));

    // then
    assertThat(thrown, is(transformException));
  }

  @Disabled("TODO")
  @Test
  public void testIngestWhenTransformClientThrowsThrowable() {}

  /* END ingest TransformClient tests */
}
