/*
 * Copyright (c) Connexta
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the
 * GNU Lesser General Public License is distributed along with this
 * program and can be found at http://www.gnu.org/licenses/lgpl.html.
 */
package com.connexta.multiintstore.common;

import com.connexta.multiintstore.callbacks.FinishedCallback;
import com.connexta.multiintstore.callbacks.MetadataCallback;
import com.connexta.multiintstore.callbacks.ProductCallback;

public class StorageManager {

  private void sortCallbacks(Object callback) {
    if (callback instanceof ProductCallback) {
      handleCallback((ProductCallback) callback);
    }
    if (callback instanceof FinishedCallback) {
      handleCallback((FinishedCallback) callback);
    }
    if (callback instanceof MetadataCallback) {
      handleCallback((MetadataCallback) callback);
    }
  }

  public void handleGeneralCallback(Object callback) {
    if (callback == null) {
      return;
    }
    sortCallbacks(callback);
    return;
  }

  private void handleCallback(ProductCallback callback) {
    System.out.println("Product");
    //  TODO :: Check Markings
    //  TODO :: Store Product
  }

  private void handleCallback(MetadataCallback callback) {
    System.out.println("Metadata");
    //  TODO :: Check Markings
    //  TODO :: Store Metadata
  }

  private void handleCallback(FinishedCallback callback) {
    System.out.println("Finished");
    //  TODO :: Remove from Temp-Store
  }
}
