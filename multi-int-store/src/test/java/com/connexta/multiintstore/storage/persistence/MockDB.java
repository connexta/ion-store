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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.mockito.invocation.InvocationOnMock;

/**
 * Helper class for mocking out the Metadata Repository. This class will be usable by anyone
 * extending the {@link org.springframework.data.repository.CrudRepository} and storing an object
 * that implements {@link Storable}
 */
public class MockDB {
  private static Map<UUID, Storable> db = new HashMap<>();

  static Object store(InvocationOnMock invocationOnMock) {
    Object arg = invocationOnMock.getArgument(0);
    if (arg instanceof Storable) {
      db.put(((Storable) arg).getId(), (Storable) arg);
    }
    return arg;
  }

  static Object load(InvocationOnMock invocationOnMock) {
    Object arg = invocationOnMock.getArgument(0);
    if (arg instanceof UUID) {
      return (db.containsKey(arg)) ? Optional.of(db.get(arg)) : Optional.empty();
    }
    return Optional.empty();
  }

  static Object delete(InvocationOnMock invocationOnMock) {
    Object arg = invocationOnMock.getArgument(0);
    if (arg instanceof UUID) {
      db.remove(arg);
    }
    return null;
  }

  static void clear() {
    db.clear();
  }
}
