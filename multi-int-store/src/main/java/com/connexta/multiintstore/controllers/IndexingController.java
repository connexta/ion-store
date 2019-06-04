/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.storage.persistence.repository.CommonSearchTermsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class IndexingController {

  private CommonSearchTermsRepository commonSearchTermsRepository;

  @Autowired
  public void setCommonSearchTermsRepository(
      CommonSearchTermsRepository commonSearchTermsRepository) {
    this.commonSearchTermsRepository = commonSearchTermsRepository;
  }
}
