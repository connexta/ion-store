package com.multiintstore.storage.persistence;

import com.multiintstore.storage.persistence.models.Metadata;

import java.util.Optional;

public class MetadataDao implements Dao<Metadata> {
  @Override
  public Optional<Metadata> get(long id) {
    return Optional.empty();
  }

  @Override
  public void save(Metadata metadata) {}

  @Override
  public void delete(Metadata metadata) {}
}
