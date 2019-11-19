/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.clients.IndexDatasetClient;
import com.connexta.store.clients.TransformClient;
import com.connexta.store.controllers.StoreController;
import com.connexta.store.service.api.StoreService;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

  @Test
  @Ignore("TODO")
  void testRetrieveFile() {}

  @Test
  @Ignore("TODO")
  void testRetrieveIrm() {}

  @Test
  @Ignore("TODO")
  void testAddIrm() {}

  @Test
  void testSuccessfulIngest(
      @Mock final StorageAdaptor mockFileStorageAdaptor,
      @Mock final StorageAdaptor mockIrmStorageAdaptor,
      @Mock final StorageAdaptor mockMetacardStorageAdaptor,
      @Mock final IndexDatasetClient mockIndexDatasetClient,
      @Mock final TransformClient mockTransformClient,
      @Mock final InputStream mockFileInputStream,
      @Mock final InputStream mockMetacardInputStream)
      throws Exception {
    // given
    final URI retrieveUri = new URI("http://store:8080");
    final StoreService storeService =
        new StoreServiceImpl(
            retrieveUri,
            mockFileStorageAdaptor,
            mockIrmStorageAdaptor,
            mockMetacardStorageAdaptor,
            mockIndexDatasetClient,
            mockTransformClient);

    final long fileSize = 1L;
    final String mimeType = "mimeType";
    final String filename = "filename";
    final long metacardSize = 1L;

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
    assertThat(datasetId, is(not(emptyString())));
    verify(mockMetacardStorageAdaptor)
        .store(
            eq(metacardSize),
            eq(MediaType.APPLICATION_XML_VALUE),
            eq(mockMetacardInputStream),
            eq(datasetId),
            anyMap());
    verify(mockTransformClient)
        .requestTransform(
            UriComponentsBuilder.fromUri(retrieveUri)
                .path(StoreController.RETRIEVE_FILE_URL_TEMPLATE)
                .build(datasetId),
            mimeType,
            UriComponentsBuilder.fromUri(retrieveUri)
                .path(StoreController.RETRIEVE_METACARD_URL_TEMPLATE)
                .build(datasetId));

    verifyNoMoreInteractions(
        mockFileStorageAdaptor,
        mockIrmStorageAdaptor,
        mockMetacardStorageAdaptor,
        mockIndexDatasetClient,
        mockTransformClient);
  }

  @Test
  @Ignore("TODO")
  void testUnsuccessfulIngest() {}

  @Test
  @Ignore("TODO")
  void testRetrieveMetacard() {}
}
