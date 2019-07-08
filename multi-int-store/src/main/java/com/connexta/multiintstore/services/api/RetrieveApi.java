/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.services.api;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** TODO Replace this with the real StoreAPI */
@Validated
public interface RetrieveApi {

  @GetMapping(value = "/product/{productId}")
  ResponseEntity<Resource> retrieveProduct(@PathVariable(value = "productId") String productId);
}
