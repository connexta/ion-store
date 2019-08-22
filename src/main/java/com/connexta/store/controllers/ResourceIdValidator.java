/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import javax.validation.ValidationException;

public interface ResourceIdValidator {

  Boolean isValid(String id);

  default void validate(String id) {
    if (!isValid(id)) {
      throw new ValidationException(String.format("The value \"%s\" is not a valid ID", id));
    }
  }
}
