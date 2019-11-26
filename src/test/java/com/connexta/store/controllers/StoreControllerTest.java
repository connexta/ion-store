/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.connexta.ingest.rest.spring.IngestApi;
import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.rest.spring.StoreApi;
import com.connexta.store.service.api.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

// TODO: These test can be changed to test the MultipartFileValidator instead of the controller.
@ExtendWith(MockitoExtension.class)
public class StoreControllerTest {

  private static final String STORE_API_VERSION = "testStoreApiVersion";

  @Mock private StoreService mockStoreService;

  private StoreApi storeApi;

  private IngestApi ingestApi;

  @BeforeEach
  public void beforeEach() {
    storeApi = new StoreController(mockStoreService, STORE_API_VERSION);
    ingestApi = new StoreController(mockStoreService, STORE_API_VERSION);
  }

  @Test
  public void testGetRequest() {
    assertThat(storeApi.getRequest().isEmpty(), is(true));
    assertThat(ingestApi.getRequest().isEmpty(), is(true));
  }

  @Test
  void testCreateDatasetNotImplemented(@Mock MultipartFile mockMultipartFile) {
    assertThat(
        storeApi.createDataset(STORE_API_VERSION, mockMultipartFile).getStatusCode(),
        is(HttpStatus.NOT_IMPLEMENTED));
  }

  @Test
  void testQuarantineSuccess() {
    String datasetId = "x";
    StoreService mockStoreService = mock(StoreService.class);
    StoreController storeController = new StoreController(mockStoreService, "0.x.0");
    assertThat(storeController.quarantine(datasetId), is(ResponseEntity.accepted().build()));
    verify(mockStoreService, times(1)).quarantine(datasetId);
  }

  @Test
  void testQuarantineException() {
    String datasetId = "x";
    StoreService mockStoreService = mock(StoreService.class);
    StoreController storeController = new StoreController(mockStoreService, "0.x.0");
    doThrow(new QuarantineException(" ")).when(mockStoreService).quarantine(datasetId);
    assertThrows(QuarantineException.class, () -> storeController.quarantine(datasetId));
  }
}
