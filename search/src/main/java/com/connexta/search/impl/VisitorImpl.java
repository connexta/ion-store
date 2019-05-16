/*
 * Copyright (c) Connexta
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the
 * GNU Lesser General Public License is distributed along with this
 * program and can be found at http://www.gnu.org/licenses/lgpl.html.
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
