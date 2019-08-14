/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(collection = IndexedProductMetadata.SOLR_COLLECTION)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IndexedProductMetadata implements Storable<String> {

  public static final String SOLR_COLLECTION = "searchTerms";

  @Id
  @Indexed(name = "id", type = "string")
  private String id;

  @Indexed(name = "contents", type = "string")
  private String contents;
}
