/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.impl;

import com.connexta.search.api.Combiner;
import com.connexta.search.api.Visitor;

public class VisitorImpl implements Visitor {

  @Override
  public void addAttribute(String attribute) {}

  @Override
  public void addOperator(String operator) {}

  @Override
  public void addValue(String value) {}

  @Override
  public void addCombiner(Combiner combiner) {}
}
