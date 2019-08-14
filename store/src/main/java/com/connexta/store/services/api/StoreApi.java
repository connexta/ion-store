/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.services.api;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/** TODO Replace this with the real StoreAPI */
@Validated
public interface StoreApi {

  @RequestMapping(
      value = "/product",
      produces = {"application/json"},
      consumes = {"multipart/form-data"},
      method = RequestMethod.POST)
  ResponseEntity<Void> storeProduct(
      @RequestHeader(value = "Accept-Version") @NotBlank final String acceptVersion,
      @Valid @RequestPart("file") @NotNull final MultipartFile file);
}
