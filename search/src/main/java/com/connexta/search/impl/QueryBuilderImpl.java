/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.impl;

import com.connexta.search.api.AttributeBuilder;
import com.connexta.search.api.Combiner;
import com.connexta.search.api.Query;
import com.connexta.search.api.QueryBuilder;

public class QueryBuilderImpl implements QueryBuilder {
  @Override
  public AttributeBuilder create() {
    return null;
  }

  @Override
  public Combiner anyOf(Query... queries) {
    return null;
  }

  @Override
  public Combiner allOf(Query... queries) {
    return null;
  }
}
