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
import com.connexta.multiintstore.storage.persistence.Dao;
import com.connexta.multiintstore.storage.persistence.models.Metadata;
import com.connexta.multiintstore.storage.persistence.models.Product;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageManager {

  private Dao<Metadata> metadataDao;
  private Dao<Product> productDao;

  @Autowired
  public StorageManager(Dao<Metadata> metadataDao, Dao<Product> productDao) {
    this.metadataDao = metadataDao;
    this.productDao = productDao;
  }

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

    Metadata metadata = getter(callback.getLocation(), callback.getId());
    if (metadata != null) {
      metadataDao.save(metadata);
    }
  }

  private void handleCallback(FinishedCallback callback) {
    System.out.println("Finished");
    //  TODO :: Remove from Temp-Store
  }

  /* Temporary code until DT figures out how we're retrieving the
    metadata.
  */
  private Map<UUID, Integer> tempState = new HashMap<>();
  private String[] metadatas = new String[] {"ddms2", "ddms5"};
  private Map<String, UUID> tempMap = new HashMap<>();

  private Metadata getter(URI location, String id) {
    Metadata temp = new Metadata();

    UUID tempID = tempMap.get(id);
    if (tempID == null) {
      tempID = UUID.randomUUID();
      tempMap.put(id, tempID);
    }

    tempState.putIfAbsent(tempID, 0);

    int index = tempState.get(tempID);
    tempState.put(tempID, index + 1);

    if (index >= metadatas.length) {
      return null;
    }

    switch (metadatas[index]) {
      case "ddms2":
        temp.setDdms2("Much DDMS2 Stuffs from " + location);
        System.out.println(metadatas[index]);
        break;
      case "ddms5":
        temp.setDdms5("Such DDMS5 Stuffs from " + location);
        System.out.println(metadatas[index]);
        break;
    }
    temp.setId(tempID);

    return temp;
  }
}
