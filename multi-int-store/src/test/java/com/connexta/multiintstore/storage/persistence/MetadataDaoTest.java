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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.connexta.multiintstore.storage.persistence.models.Metadata;
import com.connexta.multiintstore.storage.persistence.repository.MetadataRepository;
import java.util.UUID;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetadataDaoTest {

  // Mocked Database and methods
  private static Dao<Metadata> dao;

  @BeforeClass
  public static void init() {
    MetadataRepository repository = mock(MetadataRepository.class);
    doAnswer(MockDB::store).when(repository).save(any());
    doAnswer(MockDB::load).when(repository).findById(any());
    doAnswer(MockDB::delete).when(repository).deleteById(any());

    dao = new MetadataDao(repository);
  }

  @Before
  public void beforeEach() {
    MockDB.clear();
  }

  @Test
  public void testStoreAndLoad() {
    String phrase1 = "Put this";
    String phrase2 = "Somewhere safe";
    UUID id = UUID.randomUUID();
    Metadata toStore = new Metadata(id, phrase1, phrase2);

    dao.save(toStore);

    assertThat(dao.getById(id).isPresent(), is(true));
    assertThat(dao.getById(id).get(), equalTo(toStore));
  }

  @Test
  public void testEmptyLoad() {
    assertThat(dao.getById(UUID.randomUUID()).isEmpty(), is(true));
  }

  @Test
  public void testOverridingMetadata() {
    UUID id = UUID.randomUUID();
    String phrase1 = "This test should pass";
    String phrase2 = "with amazing grace.";
    Metadata meta1 = new Metadata(id, phrase2, phrase1);
    Metadata meta2 = new Metadata(id, phrase1, phrase2);

    dao.save(meta1);
    dao.save(meta2);

    assertThat(dao.getById(id).isPresent(), is(true));
    assertThat(dao.getById(id).get(), equalTo(meta2));
  }

  @Test
  public void testMutltipleSavesWithNulls() {
    UUID id = UUID.randomUUID();
    String phrase1 = "This test should pass";
    String phrase2 = "with amazing grace.";
    Metadata meta1 = new Metadata(id, phrase1, null);
    Metadata meta2 = new Metadata(id, null, phrase2);

    System.err.println(dao);

    dao.save(meta1);
    dao.save(meta2);

    assertThat(dao.getById(id).isPresent(), is(true));
    assertThat(dao.getById(id).get().getDdms2(), is(phrase1));
    assertThat(dao.getById(id).get().getDdms5(), is(phrase2));
  }

  @Test
  public void testDelete() {
    UUID id = UUID.randomUUID();
    String phrase1 = "This was a mistake";
    Metadata meta1 = new Metadata(id, phrase1, null);

    dao.save(meta1);

    assertThat(dao.getById(id).isPresent(), is(true));
    assertThat(dao.getById(id).get(), equalTo(meta1));

    dao.delete(id);

    assertThat(dao.getById(id).isPresent(), is(false));
  }
}
