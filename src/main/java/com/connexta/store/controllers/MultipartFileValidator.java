/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import javax.validation.ValidationException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Encapsulate common validations of an incoming {@link MultipartFile}. Throw a {@link
 * ValidationException} if there is a violation.
 */
public class MultipartFileValidator {

  private static final long GIGABYTE = 1 << 30;
  public static final long MAX_FILE_BYTES = 10 * GIGABYTE;
  private static final long MIN_FILE_BYTES = 1;

  private MultipartFileValidator() {}

  public static MultipartFile validate(final MultipartFile file) {
    validateNotBlank(file.getContentType(), "Media type is missing");
    validateNotBlank(file.getOriginalFilename(), "Filename is missing");
    validateSize(file);
    return file;
  }

  private static void validateNotBlank(String s, String message) {
    if (s == null || s.isBlank()) {
      throw new ValidationException(message);
    }
  }

  private static void validateSize(final MultipartFile file) {
    final long fileSize = file.getSize();
    if (fileSize < MIN_FILE_BYTES) {
      throw new ValidationException(
          String.format("File size must be larger than %d MB", MIN_FILE_BYTES));
    }

    if (fileSize > MAX_FILE_BYTES) {
      throw new ValidationException(String.format("File exceeded maximum size of %d GB", GIGABYTE));
    }
  }
}
