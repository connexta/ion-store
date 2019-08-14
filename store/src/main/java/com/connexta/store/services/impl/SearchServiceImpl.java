/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.services.impl;

import com.connexta.store.common.exceptions.SearchException;
import com.connexta.store.models.IndexedProductMetadata;
import com.connexta.store.repositories.IndexedMetadataRepository;
import com.connexta.store.services.api.SearchService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;

@Slf4j
public class SearchServiceImpl implements SearchService {

  @NotNull private final IndexedMetadataRepository indexedMetadataRepository;

  @NotBlank private final String endpointUrlRetrieve;

  public SearchServiceImpl(
      @NotNull final IndexedMetadataRepository indexedMetadataRepository,
      @NotBlank final String retrieveEndpoint) {
    this.indexedMetadataRepository = indexedMetadataRepository;
    this.endpointUrlRetrieve = retrieveEndpoint;
  }

  @Override
  public void store(IndexedProductMetadata doc) {
    indexedMetadataRepository.save(doc);
  }

  @Override
  public List<URI> find(String keyword) throws SearchException {
    final List<IndexedProductMetadata> matchingIndexedProductMetadatas;
    try {
      matchingIndexedProductMetadatas = indexedMetadataRepository.findByContents(keyword);
    } catch (RuntimeException e) {
      // TODO remove this check once solr is deployed independently
      if (e instanceof DataAccessResourceFailureException
          && indexedMetadataRepository.count() == 0) {
        log.warn("Solr is empty. Returning empty search results.");
        return Collections.emptyList();
      }

      throw new SearchException("Unable to search for " + keyword, e);
    }

    final List<URI> uris = new ArrayList<>();
    for (final IndexedProductMetadata indexedProductMetadata : matchingIndexedProductMetadatas) {
      final String id = indexedProductMetadata.getId();
      final URI uri;
      try {
        uri = new URI(endpointUrlRetrieve + id);
      } catch (URISyntaxException e) {
        throw new SearchException(
            "Unable to construct retrieve URI from endpointUrlRetrieve="
                + endpointUrlRetrieve
                + " and id="
                + id,
            e);
      }
      uris.add(uri);
    }

    return Collections.unmodifiableList(uris);
  }
}
