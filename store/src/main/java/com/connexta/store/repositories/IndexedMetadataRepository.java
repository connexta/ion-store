/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.repositories;

import com.connexta.store.models.IndexedProductMetadata;
import java.util.List;
import org.springframework.data.solr.UncategorizedSolrException;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexedMetadataRepository
    extends SolrCrudRepository<IndexedProductMetadata, String> {

  List<IndexedProductMetadata> findByContents(String keyword) throws UncategorizedSolrException;
}
