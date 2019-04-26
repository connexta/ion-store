package com.connexta.multiintstore.storage.persistence;

import java.util.Optional;

public interface Dao<T> {
    Optional<T> get(long id);
    void save(T t);
    // We need this guy? Maybe for the MIS ID
    void delete(T t);
}
