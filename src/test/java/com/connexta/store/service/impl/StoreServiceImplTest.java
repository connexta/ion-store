/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.clients.IndexDatasetClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.poller.TransformStatusTask;
import com.connexta.store.service.api.StoreService;
import com.google.common.collect.Queues;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {
  private StoreService storeService;
  private URI storeUrl;
  @Mock StorageAdaptor fileStorageAdaptor;
  @Mock StorageAdaptor irmStorageAdaptor;
  @Mock StorageAdaptor metacardStorageAdaptor;
  @Mock IndexDatasetClient indexDatasetClient;
  @Mock TransformClient transformClient;
  private BlockingQueue<TransformStatusTask> taskQueue;

  @BeforeEach
  void setUp() throws Exception {
    storeUrl = new URI("http://test:1234");
    taskQueue = Queues.newLinkedBlockingQueue();
    storeService =
        new StoreServiceImpl(
            storeUrl,
            fileStorageAdaptor,
            irmStorageAdaptor,
            metacardStorageAdaptor,
            indexDatasetClient,
            transformClient,
            taskQueue,
            mock(WebClient.class));
  }

  @AfterEach
  void after() {
    verifyNoMoreInteractions(
        fileStorageAdaptor,
        irmStorageAdaptor,
        metacardStorageAdaptor,
        indexDatasetClient,
        transformClient);
  }

  @Test
  void testSuccessfulIngest(
      @Mock final StorageAdaptor mockFileStorageAdaptor,
      @Mock final StorageAdaptor mockIrmStorageAdaptor,
      @Mock final StorageAdaptor mockMetacardStorageAdaptor,
      @Mock final IndexDatasetClient mockIndexDatasetClient,
      @Mock final TransformClient mockTransformClient,
      @Mock final InputStream mockFileInputStream,
      @Mock final InputStream mockMetacardInputStream,
      @Mock final WebClient transformWebClient)
      throws Exception {
    // given
    final StoreService storeService =
        new StoreServiceImpl(
            storeUrl,
            mockFileStorageAdaptor,
            mockIrmStorageAdaptor,
            mockMetacardStorageAdaptor,
            mockIndexDatasetClient,
            mockTransformClient,
            taskQueue,
            transformWebClient);

    final long fileSize = 1L;
    final String mimeType = "mimeType";
    final String filename = "filename";
    final long metacardSize = 1L;

    final URL transformUrl = new URL("http://someurl:8080/abcwee");
    when(mockTransformClient.requestTransform(
            anyString(), any(URL.class), any(URL.class), any(URL.class)))
        .thenReturn(transformUrl);

    // when
    storeService.ingest(
        fileSize, mimeType, mockFileInputStream, filename, metacardSize, mockMetacardInputStream);

    // then
    final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockFileStorageAdaptor)
        .store(
            eq(fileSize),
            eq(mimeType),
            eq(mockFileInputStream),
            argumentCaptor.capture(),
            argThat(
                map ->
                    StringUtils.equals(
                        map.get(StoreServiceImpl.FILE_NAME_METADATA_KEY), filename)));

    final String datasetId = argumentCaptor.getValue();
    verify(mockMetacardStorageAdaptor)
        .store(
            eq(metacardSize),
            eq(MediaType.APPLICATION_XML_VALUE),
            eq(mockMetacardInputStream),
            eq(datasetId),
            eq(Map.of()));

    final URL fileUrl = new URL(storeUrl.toASCIIString() + "/dataset/" + datasetId + "/file");
    final URL metacardUrl =
        new URL(storeUrl.toASCIIString() + "/dataset/" + datasetId + "/metacard");
    verify(mockTransformClient).requestTransform(datasetId, fileUrl, fileUrl, metacardUrl);

    assertThat(taskQueue.size(), Matchers.is(1));
    final TransformStatusTask task = taskQueue.take();
    assertThat(task.getDatasetId(), Matchers.is((datasetId)));
    assertThat(task.getTransformStatusUrl(), Matchers.is(transformUrl));
  }

  @Test
  void testSuccessfulQuarantine() {
    String datasetId = "x";
    storeService.quarantine(datasetId);
    for (StorageAdaptor adaptor :
        List.of(fileStorageAdaptor, irmStorageAdaptor, metacardStorageAdaptor)) {
      verify(adaptor, times(1)).delete(datasetId);
    }
  }

  @Test
  void testFileQuarantineException() {
    String datasetId = "x";
    doThrow(new QuarantineException(" ")).when(fileStorageAdaptor).delete(datasetId);
    assertThrows(QuarantineException.class, () -> storeService.quarantine(datasetId));
  }

  @Test
  void testIrmQuarantineException() {
    String datasetId = "x";
    doNothing().when(fileStorageAdaptor).delete(datasetId);
    doThrow(new QuarantineException(" ")).when(irmStorageAdaptor).delete(datasetId);
    assertThrows(QuarantineException.class, () -> storeService.quarantine(datasetId));
  }

  @Test
  void testMetacardQuarantineException() {
    String datasetId = "x";
    doNothing().when(fileStorageAdaptor).delete(datasetId);
    doNothing().when(irmStorageAdaptor).delete(datasetId);
    doThrow(new QuarantineException(" ")).when(metacardStorageAdaptor).delete(datasetId);
    assertThrows(QuarantineException.class, () -> storeService.quarantine(datasetId));
  }
}
