/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.api;

public interface Visitor {
  void addAttribute(String attribute);

  void addOperator(String operator);

  void addValue(String value);

  void addCombiner(Combiner combiner);
}
