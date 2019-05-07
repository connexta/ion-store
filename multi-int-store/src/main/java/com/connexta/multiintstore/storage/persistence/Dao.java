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
package com.connexta.multiintstore.storage.persistence;

import com.connexta.multiintstore.storage.persistence.models.Storable;
import java.util.Optional;
import java.util.UUID;

public interface Dao<T extends Storable> {
  /** @param id Takes the UUID of the object stored */
  Optional<T> getById(UUID id);
  /** @param t The object to be stored. */
  void save(T t);
  // We need this guy? Maybe for the MIS ID
  void delete(UUID id);
}
