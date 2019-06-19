/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.services.impl;

import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import com.connexta.multiintstore.services.api.Dao;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class IndexedMetadataDao implements Dao<IndexedProductMetadata, String> {

  private final IndexedMetadataRepository repository;

  public IndexedMetadataDao(IndexedMetadataRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<IndexedProductMetadata> getById(String id) {
    return repository.findById(id);
  }

  @Override
  public void save(IndexedProductMetadata indexedProductMetadata) {
    repository.save(indexedProductMetadata);
  }

  @Override
  public void delete(String id) {
    repository.deleteById(id);
  }
}
