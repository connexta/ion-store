/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.impl;

import com.connexta.search.api.Query;
import com.connexta.search.api.ValueBuilder;

public class ValueBuilderImpl implements ValueBuilder {
  @Override
  public Query text(String text) {
    return null;
  }

  @Override
  public Query number(int num) {
    return null;
  }

  @Override
  public Query number(double num) {
    return null;
  }

  @Override
  public Query number(float num) {
    return null;
  }
}
