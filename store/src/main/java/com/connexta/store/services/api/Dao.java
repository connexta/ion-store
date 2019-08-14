/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.services.api;

import com.connexta.store.common.exceptions.StorageException;
import com.connexta.store.models.Storable;
import java.util.Optional;

public interface Dao<T extends Storable<U>, U> {

  /** @param id Takes the UUID of the object stored */
  Optional<T> getById(U id) throws StorageException;

  /** @param t The object to be stored. */
  void save(T t) throws StorageException;
}
