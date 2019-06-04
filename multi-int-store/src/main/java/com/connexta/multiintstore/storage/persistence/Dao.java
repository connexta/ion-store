/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
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
