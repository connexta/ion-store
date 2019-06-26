/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storage.persistence;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.repositories.IndexedMetadataRepository;
import com.connexta.multiintstore.services.api.Dao;
import com.connexta.multiintstore.services.api.DuplicateIdException;
import com.connexta.multiintstore.services.api.StorageException;
import com.connexta.multiintstore.services.impl.IndexedMetadataDao;
import org.junit.Before;
import org.junit.Test;

public class IndexedMetadataDaoTest {

  // Mocked Database and methods
  private Dao<IndexedProductMetadata, String> dao;

  private IndexedMetadataRepository repository;

  @Before
  public void beforeEach() {
    repository = mock(IndexedMetadataRepository.class);
    doAnswer(MockDB::store).when(repository).save(any());
    doAnswer(MockDB::load).when(repository).findById(any());
    doAnswer(MockDB::delete).when(repository).deleteById(any());

    dao = new IndexedMetadataDao(repository);

    MockDB.clear();
  }

  @Test
  public void testStorage() {
    IndexedProductMetadata ipm = new IndexedProductMetadata("nice.id", "and some cool content");
    dao.save(ipm);
    assertThat(dao.getById(ipm.getId()), isPresentAnd(is(ipm)));
  }

  @Test(expected = DuplicateIdException.class)
  public void testDuplicateIds() {
    IndexedProductMetadata ipm = new IndexedProductMetadata("nice.id", "and some cool content");
    dao.save(ipm);
    assertThat(dao.getById(ipm.getId()), isPresentAnd(is(ipm)));
    dao.save(ipm);
  }

  @Test(expected = StorageException.class)
  public void testExpectedStorageFailure() {
    when(repository.save(any())).thenThrow(new RuntimeException("Internal Server Error"));
    IndexedProductMetadata ipm = new IndexedProductMetadata("nice.id", "and some cool content");
    dao.save(ipm);
  }
}
