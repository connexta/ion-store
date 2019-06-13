/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storage.persistence;

import com.connexta.multiintstore.storage.persistence.models.CommonSearchTerms;
import com.connexta.multiintstore.storage.persistence.repository.CommonSearchTermsRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonSearchDao implements Dao<CommonSearchTerms, String> {

  private final CommonSearchTermsRepository repository;

  @Autowired
  public CommonSearchDao(CommonSearchTermsRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<CommonSearchTerms> getById(String id) {
    return repository.findById(id);
  }

  @Override
  public void save(CommonSearchTerms commonSearchTerms) {
    repository.save(commonSearchTerms);
  }

  @Override
  public void delete(String id) {
    repository.deleteById(id);
  }
}
