/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storage.persistence.repository;

import com.connexta.multiintstore.storage.persistence.models.CommonSearchTerms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;

public interface CommonSearchTermsRepository extends SolrCrudRepository<CommonSearchTerms, String> {
  @Query(name = "CommonSearchTerms.findByNamedQuery")
  public Page<CommonSearchTerms> findByNamedQuery(String searchTerm, Pageable pageable);
}
