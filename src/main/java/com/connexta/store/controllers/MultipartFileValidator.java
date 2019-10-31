/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import javax.validation.ValidationException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Encapsulate common validations of an incoming MultipartFile. Throw a ValidationException if there
 * is a violation.
 */
public class MultipartFileValidator {
  private final MultipartFile file;
  private static final long GIGABYTE = 1 << 30;
  private static final long MAX_FILE_BYTES = 10 * GIGABYTE;

  public MultipartFileValidator(MultipartFile file) {
    this.file = file;
  }

  public void validate() {
    validateSize();
    validateMediaType(file.getContentType(), "Media type is missing");
    validateMediaType(file.getOriginalFilename(), "Filename is missing");
    validateInputStream();
  }

  private void validateInputStream() {
    try {
      file.getInputStream();
    } catch (IOException e) {
      throw new ValidationException("Unable to read file", e);
    }
  }

  private String validateMediaType(String contentType, String s) {
    final String mediaType = contentType;
    if (isBlank(mediaType)) {
      throw new ValidationException(s);
    }
    return mediaType;
  }

  private void validateSize() {
    final Long fileSize = file.getSize();
    if (fileSize > MAX_FILE_BYTES) {
      throw new ValidationException(
          String.format(
              "File size is %d bytes. File size cannot be greater than %d bytes",
              fileSize, MAX_FILE_BYTES));
    }
  }
}
