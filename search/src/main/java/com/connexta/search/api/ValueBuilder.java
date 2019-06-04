/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.api;

public interface ValueBuilder {
  Query text(String text);

  Query number(int num);

  Query number(double num);

  Query number(float num);
}
