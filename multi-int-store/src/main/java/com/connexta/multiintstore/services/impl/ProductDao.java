/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.services.impl;

import com.connexta.multiintstore.models.Product;
import com.connexta.multiintstore.services.api.Dao;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductDao implements Dao<Product, String> {

  @Override
  public Optional<Product> getById(String id) {
    return Optional.empty();
  }

  @Override
  public void save(Product product) {}

  @Override
  public void delete(String id) {}
}
