/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.services.api;

import com.connexta.multiintstore.models.IndexedProductMetadata;
import java.util.List;

public interface SearchService {

  void store(IndexedProductMetadata doc);

  List<IndexedProductMetadata> find(String keyword);

  void delete(String id);
}
