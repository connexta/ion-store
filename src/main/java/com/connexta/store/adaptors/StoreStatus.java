/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

public class StoreStatus {
  private StoreStatus() {}

  public static final String STATUS_KEY = "status";
  public static final String STORED = "stored";
  public static final String STAGED = "staged";
  public static final String QUARANTINED = "quarantined";
}
