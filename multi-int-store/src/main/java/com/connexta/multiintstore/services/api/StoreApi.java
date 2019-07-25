/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.services.api;

import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
  ResponseEntity<URI> storeProduct(
      @RequestHeader(value = "Accept-Version") @NotBlank String acceptVersion,
      @RequestParam(value = "fileSize") @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @RequestParam(value = "mimeType") @NotBlank String mimeType,
      @Valid @RequestPart("file") @NotNull MultipartFile file,
      @RequestParam(value = "fileName") @NotBlank String fileName);

  @RequestMapping(
      value = "/product/{productId}/{metadataType}",
      produces = {"application/json"},
      consumes = {"multipart/form-data"},
      method = RequestMethod.PUT)
  ResponseEntity<Void> storeMetadata(
      @RequestHeader(value = "Accept-Version") @NotBlank String acceptVersion,
      @PathVariable(value = "productId") @NotBlank String productId,
      @PathVariable(value = "metadataType") @NotBlank String metadataType,
      @RequestParam(value = "fileSize") @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @RequestParam(value = "mimeType") @NotBlank String mimeType,
      @Valid @RequestPart("file") @NotNull MultipartFile file,
      @RequestParam(value = "fileName") @NotBlank String fileName);
}
