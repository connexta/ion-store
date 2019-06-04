/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.api;

public interface QueryBuilder {
  AttributeBuilder create();

  Combiner anyOf(Query... queries);

  Combiner allOf(Query... queries);
}
