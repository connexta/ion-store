/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import javax.validation.constraints.NotNull;

public class UuidValidator implements ResourceIdValidator {

  @Override
  public Boolean isValid(@NotNull String id) {
    return id.matches("[0-9a-fA-F]{32}");
  }
}
