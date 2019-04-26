package com.connexta.multiintstore.storage.persistence;

import com.connexta.multiintstore.storage.persistence.Dao;
import com.connexta.multiintstore.storage.persistence.models.Metadata;

import java.util.Optional;

public class MetadataDao implements Dao<Metadata> {
    @Override
    public Optional<Metadata> get(long id) {
        return Optional.empty();
    }

    @Override
    public void save(Metadata metadata) {

    }

    @Override
    public void delete(Metadata metadata) {

    }
}
