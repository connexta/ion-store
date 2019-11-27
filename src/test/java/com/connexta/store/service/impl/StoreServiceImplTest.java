/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import static com.connexta.store.adaptors.StoreStatus.QUARANTINED;
import static com.connexta.store.adaptors.StoreStatus.STAGED;
import static com.connexta.store.adaptors.StoreStatus.STORED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.adaptors.StorageAdaptorRetrieveResponse;
import com.connexta.store.adaptors.StoreStatus;
import com.connexta.store.clients.IndexDatasetClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.controllers.StoreController;
import com.connexta.store.exceptions.DatasetNotFoundException;
import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.exceptions.RetrieveException;
import com.connexta.store.exceptions.common.DetailedResponseStatusException;
import com.connexta.store.poller.TransformStatusTask;
import com.connexta.store.rest.models.MetadataInfo;
import com.connexta.store.service.api.IonData;
import com.connexta.store.service.api.StoreService;
import com.google.common.collect.Queues;
import java.io.IOException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {
  private static final String DATASET_ID = "341d6c1ce5e0403a99fe86edaed66eea";
  private static final String RESOURCE_RESPONSE =
      "/com/connexta/store/service/impl/resourceResponse.xml";
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

  // Todo: Tests for adding metadata
  @Test
  void testAddingIrm() throws IOException {
    var mockInputStream = mock(InputStream.class);
    var mockResource = mock(Resource.class);
    var mockTransformWebClient = mockWebClient(mockResource);
    var resourceUri =
        UriComponentsBuilder.fromUri(storeUrl)
            .path(StoreController.RETRIEVE_DATA_URL_TEMPLATE)
            .build(DATASET_ID, "irm");
    var mockStoreService =
        new StoreServiceImpl(
            storeUrl,
            fileStorageAdaptor,
            irmStorageAdaptor,
            metacardStorageAdaptor,
            indexDatasetClient,
            transformClient,
            taskQueue,
            mockTransformWebClient);
    var metadataInfo = new MetadataInfo();
    metadataInfo.setLocation(new URL("http://location"));
    metadataInfo.setMetadataType("irm");

    when(mockResource.getInputStream()).thenReturn(mockInputStream);

    mockStoreService.addMetadata(DATASET_ID, List.of(metadataInfo));

    verify(irmStorageAdaptor)
        .store(
            anyLong(),
            eq(StoreController.IRM_MEDIA_TYPE_VALUE),
            eq(mockInputStream),
            eq(DATASET_ID),
            eq(Map.of()));
    verify(fileStorageAdaptor).updateStatus(eq(DATASET_ID), eq(STORED));
    verify(metacardStorageAdaptor).delete(eq(DATASET_ID));
    verify(irmStorageAdaptor).updateStatus(eq(DATASET_ID), eq(STORED));
    verify(indexDatasetClient).indexDataset(DATASET_ID, resourceUri);
  }

  @Test
  void testAddingMetacard() throws IOException {
    var mockInputStream = mock(InputStream.class);
    var mockResource = mock(Resource.class);
    var mockTransformWebClient = mockWebClient(mockResource);
    var resourceUri =
        UriComponentsBuilder.fromUri(storeUrl)
            .path(StoreController.RETRIEVE_DATA_URL_TEMPLATE)
            .build(DATASET_ID, "metacard");
    var mockStoreService =
        new StoreServiceImpl(
            storeUrl,
            fileStorageAdaptor,
            irmStorageAdaptor,
            metacardStorageAdaptor,
            indexDatasetClient,
            transformClient,
            taskQueue,
            mockTransformWebClient);
    var metadataInfo = new MetadataInfo();
    metadataInfo.setLocation(new URL("http://location"));
    metadataInfo.setMetadataType("metacard");

    when(mockResource.getInputStream()).thenReturn(mockInputStream);

    mockStoreService.addMetadata(DATASET_ID, List.of(metadataInfo));

    verify(metacardStorageAdaptor)
        .store(
            anyLong(),
            eq(MediaType.APPLICATION_XML_VALUE),
            eq(mockInputStream),
            eq(DATASET_ID),
            eq(Map.of()));
    verify(fileStorageAdaptor).updateStatus(eq(DATASET_ID), eq(STORED));
    verify(metacardStorageAdaptor).delete(eq(DATASET_ID));
    verify(metacardStorageAdaptor).updateStatus(eq(DATASET_ID), eq(STORED));
    verify(indexDatasetClient).indexDataset(DATASET_ID, resourceUri);
  }

  @Test
  void testAddingMetadataBadURL() throws IOException {
    var metadataInfo = mock(MetadataInfo.class);
    var mockTransformWebClient = mock(WebClient.class);
    var uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
    var mockStoreService =
        new StoreServiceImpl(
            storeUrl,
            fileStorageAdaptor,
            irmStorageAdaptor,
            metacardStorageAdaptor,
            indexDatasetClient,
            transformClient,
            taskQueue,
            mockTransformWebClient);

    when(mockTransformWebClient.get()).thenReturn(uriSpecMock);
    when(metadataInfo.getLocation()).thenReturn(new URL("http:// "));

    assertThrows(
        DetailedResponseStatusException.class,
        () -> mockStoreService.addMetadata(DATASET_ID, List.of(metadataInfo)));
  }

  @Test
  void testGettingBadDataType() throws IOException {
    assertThrows(IllegalArgumentException.class, () -> storeService.getData(DATASET_ID, "badType"));
  }

  // Todo: Parameterize these tests
  @Test
  void testGettingIrm(
      @Mock StorageAdaptorRetrieveResponse mockRetrieveResponse, @Mock InputStream mockInputStream)
      throws IOException {
    when(irmStorageAdaptor.getStatus(DATASET_ID)).thenReturn(STORED);
    when(irmStorageAdaptor.retrieve(DATASET_ID)).thenReturn(mockRetrieveResponse);
    when(mockRetrieveResponse.getInputStream()).thenReturn(mockInputStream);

    IonData data = storeService.getData(DATASET_ID, "irm");

    assertThat(data, is(notNullValue()));
    assertThat(data.getFileName(), is("irm-" + DATASET_ID + ".xml"));
  }

  @Test
  void testGettingMetacard(
      @Mock StorageAdaptorRetrieveResponse mockRetrieveResponse, @Mock InputStream mockInputStream)
      throws IOException {
    when(metacardStorageAdaptor.getStatus(DATASET_ID)).thenReturn(STORED);
    when(metacardStorageAdaptor.retrieve(DATASET_ID)).thenReturn(mockRetrieveResponse);
    when(mockRetrieveResponse.getInputStream()).thenReturn(mockInputStream);

    IonData data = storeService.getData(DATASET_ID, "metacard");

    assertThat(data, is(notNullValue()));
    assertThat(data.getFileName(), is("metacard-" + DATASET_ID + ".xml"));
  }

  @ParameterizedTest
  @ValueSource(strings = {STAGED, STORED})
  void testGettingFile(String status) throws IOException {
    StorageAdaptorRetrieveResponse mockRetrieveResponse =
        mock(StorageAdaptorRetrieveResponse.class);
    InputStream mockInputStream = mock(InputStream.class);
    String filename = "name.txt";
    when(fileStorageAdaptor.getStatus(DATASET_ID)).thenReturn(status);
    when(fileStorageAdaptor.retrieve(DATASET_ID)).thenReturn(mockRetrieveResponse);
    when(mockRetrieveResponse.getMetadata()).thenReturn(Map.of("Filename", filename));
    when(mockRetrieveResponse.getMediaType()).thenReturn(MediaType.TEXT_PLAIN);
    when(mockRetrieveResponse.getInputStream()).thenReturn(mockInputStream);

    IonData data = storeService.getData(DATASET_ID, "file");

    assertThat(data, is(notNullValue()));
    assertThat(data.getFileName(), is(filename));
  }

  @Test
  void testGettingDatasetNotStored() {
    when(metacardStorageAdaptor.getStatus(eq(DATASET_ID))).thenReturn(QUARANTINED);

    assertThrows(RetrieveException.class, () -> storeService.getData(DATASET_ID, "metacard"));
    verifyNoInteractions(indexDatasetClient);
  }

  @Test
  void testGettingDatasetNullStatus() {
    when(metacardStorageAdaptor.getStatus(eq(DATASET_ID))).thenReturn(null);

    assertThrows(RetrieveException.class, () -> storeService.getData(DATASET_ID, "metacard"));
    verifyNoInteractions(indexDatasetClient);
  }

  @Test
  void testSuccessfulUnstage() {
    storeService.unstage(DATASET_ID);

    verify(fileStorageAdaptor).updateStatus(DATASET_ID, StoreStatus.STORED);
    verify(metacardStorageAdaptor).delete(DATASET_ID);
  }

  @Test
  void testDatasetNotFoundWhenUnstaging() {
    doThrow(new DatasetNotFoundException(""))
        .when(fileStorageAdaptor)
        .updateStatus(eq(DATASET_ID), eq(STORED));
    assertThrows(DatasetNotFoundException.class, () -> storeService.unstage(DATASET_ID));
  }

  @Test
  void testDeleteExceptionWhenUnstaging() {
    doNothing().when(fileStorageAdaptor).updateStatus(DATASET_ID, STORED);
    doThrow(new QuarantineException("")).when(metacardStorageAdaptor).delete(eq(DATASET_ID));
    assertThrows(QuarantineException.class, () -> storeService.unstage(DATASET_ID));
  }

  @Test
  void testSuccessfulQuarantine() {
    storeService.quarantine(DATASET_ID);
    for (StorageAdaptor adaptor :
        List.of(fileStorageAdaptor, irmStorageAdaptor, metacardStorageAdaptor)) {
      verify(adaptor, times(1)).delete(DATASET_ID);
    }
  }

  @Test
  void testFileQuarantineException() {
    doThrow(new QuarantineException(" ")).when(fileStorageAdaptor).delete(DATASET_ID);
    assertThrows(QuarantineException.class, () -> storeService.quarantine(DATASET_ID));
  }

  @Test
  void testIrmQuarantineException() {
    doNothing().when(fileStorageAdaptor).delete(DATASET_ID);
    doThrow(new QuarantineException(" ")).when(irmStorageAdaptor).delete(DATASET_ID);
    assertThrows(QuarantineException.class, () -> storeService.quarantine(DATASET_ID));
  }

  @Test
  void testMetacardQuarantineException() {
    doNothing().when(fileStorageAdaptor).delete(DATASET_ID);
    doNothing().when(irmStorageAdaptor).delete(DATASET_ID);
    doThrow(new QuarantineException(" ")).when(metacardStorageAdaptor).delete(DATASET_ID);
    assertThrows(QuarantineException.class, () -> storeService.quarantine(DATASET_ID));
  }

  private WebClient mockWebClient(Resource mockResource) {
    final var mock = mock(WebClient.class);
    final var uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
    final var headersSpecMock = mock(WebClient.RequestHeadersSpec.class);
    final var responseSpecMock = mock(WebClient.ResponseSpec.class);
    final var responseEntity = Mono.just(ResponseEntity.ok().body(mockResource));

    when(mock.get()).thenReturn(uriSpecMock);
    when(uriSpecMock.uri(any(URI.class))).thenReturn(headersSpecMock);
    when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
    when(responseSpecMock.toEntity(Resource.class)).thenReturn(responseEntity);
    return mock;
  }
}
