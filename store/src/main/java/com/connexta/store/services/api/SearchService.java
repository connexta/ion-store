/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.services.api;

import com.connexta.store.common.exceptions.SearchException;
import com.connexta.store.models.IndexedProductMetadata;
import java.net.URI;
import java.util.List;

public interface SearchService {

  void store(IndexedProductMetadata doc);

  List<URI> find(String keyword) throws SearchException;
}
