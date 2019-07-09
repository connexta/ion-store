/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.services.impl;

import com.connexta.multiintstore.common.exceptions.SearchException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

  @NotNull private final IndexedMetadataRepository indexedMetadataRepository;

  @NotEmpty private final String endpointUrlRetrieve;

  public SearchServiceImpl(
      @NotNull final IndexedMetadataRepository indexedMetadataRepository,
      @NotEmpty @Value("${endpointUrl.retrieve}") final String retrieveEndpoint) {
    this.indexedMetadataRepository = indexedMetadataRepository;
    this.endpointUrlRetrieve = retrieveEndpoint;
  }

  @Override
  public void store(IndexedProductMetadata doc) {
    indexedMetadataRepository.save(doc);
  }

  @Override
  public List<URL> find(String keyword) throws SearchException {
    final List<IndexedProductMetadata> matchingIndexedProductMetadatas;
    try {
      matchingIndexedProductMetadatas = indexedMetadataRepository.findByContents(keyword);
    } catch (RuntimeException e) {
      throw new SearchException("Unable to search for " + keyword, e);
    }

    final List<URL> urls = new ArrayList<>();
    for (final IndexedProductMetadata indexedProductMetadata : matchingIndexedProductMetadatas) {
      final String id = indexedProductMetadata.getId();
      final URL url;
      try {
        url = new URL(endpointUrlRetrieve + id);
      } catch (MalformedURLException e) {
        throw new SearchException(
            "Unable to construct retrieve URL from endpointUrlRetrieve="
                + endpointUrlRetrieve
                + " and id="
                + id,
            e);
      }
      urls.add(url);
    }

    return Collections.unmodifiableList(urls);
  }
}
