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

import com.connexta.store.exceptions.QuarantineException;
import com.connexta.store.service.api.StoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

// TODO: These test can be changed to test the MultipartFileValidator instead of the controller.
@ExtendWith(MockitoExtension.class)
public class StoreControllerTest {

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
