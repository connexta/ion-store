/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storage.persistence;

import com.connexta.multiintstore.storage.persistence.models.Metadata;
import com.connexta.multiintstore.storage.persistence.repository.MetadataRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class MetadataDao implements Dao<Metadata> {

  private MetadataRepository repository;

  @Autowired
  public MetadataDao(MetadataRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Metadata> getById(UUID id) {
    return repository.findById(id);
  }

  @Override
  public void save(Metadata metadata) {
    Metadata savedMetadata = repository.findById(metadata.getId()).orElse(null);
    Metadata merged = mergeMetadata(savedMetadata, metadata);
    repository.save(merged);
  }

  @Override
  public void delete(UUID id) {
    repository.deleteById(id);
  }

  private Metadata mergeMetadata(@Nullable Metadata old, Metadata updated) {
    if (updated == null) {
      return null;
    }
    //  Register the BeanUtils.copyProperties to not copy null Values
    Metadata newMetadata = new Metadata();
    if (old != null) {
      copyMetadata(old, newMetadata);
    }
    copyMetadata(updated, newMetadata);

    return newMetadata;
  }

  private void copyMetadata(Metadata prev, Metadata dest) {
    ifNotNullDo(prev.getId(), () -> dest.setId(prev.getId()));
    ifNotNullDo(prev.getDdms2(), () -> dest.setDdms2(prev.getDdms2()));
    ifNotNullDo(prev.getDdms5(), () -> dest.setDdms5(prev.getDdms5()));
  }

  private void ifNotNullDo(Object a, Runnable r) {
    if (a == null) {
      return;
    }
    r.run();
  }
}
