/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.services.impl;

import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import com.connexta.multiintstore.services.api.SearchService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

  private final IndexedMetadataRepository indexedMetadataRepository;

  @Override
  public void store(IndexedProductMetadata doc) {
    indexedMetadataRepository.save(doc);
  }

  @Override
  public List<IndexedProductMetadata> find(String keyword) {
    return indexedMetadataRepository.findByContents(keyword);
  }

  @Override
  public void delete(String id) {
    indexedMetadataRepository.deleteById(id);
  }
}
