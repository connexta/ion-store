/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storage.persistence;

import com.connexta.multiintstore.models.Storable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.mockito.invocation.InvocationOnMock;

/**
 * Helper class for mocking out the Metadata Repository. This class will be usable by anyone
 * extending the {@link org.springframework.data.repository.CrudRepository} and storing an object
 * that implements {@link Storable}
 */
public class MockDB {
  private static final Map<String, Storable> db = new HashMap<>();

  static Object store(InvocationOnMock invocationOnMock) {
    Object arg = invocationOnMock.getArgument(0);
    if (arg instanceof Storable) {
      db.put(((Storable) arg).getId().toString(), (Storable) arg);
    }
    return arg;
  }

  static Object load(InvocationOnMock invocationOnMock) {
    Object arg = invocationOnMock.getArgument(0);
    if (arg instanceof String) {
      return (db.containsKey(arg)) ? Optional.of(db.get(arg)) : Optional.empty();
    }
    return Optional.empty();
  }

  static Object delete(InvocationOnMock invocationOnMock) {
    Object arg = invocationOnMock.getArgument(0);
    if (arg instanceof String) {
      db.remove(arg);
    }
    return null;
  }

  static void clear() {
    db.clear();
  }
}
