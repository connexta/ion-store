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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

  @NotNull private final IndexedMetadataRepository indexedMetadataRepository;

  @NotEmpty private final String retrieveEndpoint;

  public SearchServiceImpl(
      @NotNull final IndexedMetadataRepository indexedMetadataRepository,
      @NotEmpty @Value("${endpointUrl.retrieve}") final String retrieveEndpoint) {
    this.indexedMetadataRepository = indexedMetadataRepository;
    this.retrieveEndpoint = retrieveEndpoint;
  }

  @Override
  public void store(IndexedProductMetadata doc) {
    indexedMetadataRepository.save(doc);
  }

  @Override
  public List<URL> find(String keyword) throws MalformedURLException {
    final List<URL> urls = new ArrayList<>();
    for (final IndexedProductMetadata indexedProductMetadata :
        indexedMetadataRepository.findByContents(keyword)) {
      urls.add(new URL(retrieveEndpoint + indexedProductMetadata.getId()));
    }
    return Collections.unmodifiableList(urls);
  }
}
