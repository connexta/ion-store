/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storage.persistence;

import com.connexta.multiintstore.storage.persistence.models.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductDao implements Dao<Product> {

  @Override
  public Optional<Product> getById(UUID id) {
    return Optional.empty();
  }

  @Override
  public void save(Product product) {}

  @Override
  public void delete(UUID id) {}
}
