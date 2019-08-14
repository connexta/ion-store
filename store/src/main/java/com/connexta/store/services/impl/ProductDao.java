/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.services.impl;

import com.connexta.store.models.Product;
import com.connexta.store.services.api.Dao;
import java.util.Optional;

public class ProductDao implements Dao<Product, String> {

  @Override
  public Optional<Product> getById(String id) {
    return Optional.empty();
  }

  @Override
  public void save(Product product) {}
}
