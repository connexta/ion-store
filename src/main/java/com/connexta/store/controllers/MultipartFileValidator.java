/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.validation.ValidationException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Encapsulate common validations of an incoming {@link MultipartFile}. Throw a {@link
 * ValidationException} if there is a violation.
 */
public class MultipartFileValidator {

  private static final long GIGABYTE = 1 << 30;
  public static final long MAX_FILE_BYTES = 10 * GIGABYTE;

  private MultipartFileValidator() {}

  public static MultipartFile validate(final MultipartFile file) {
    validateNotBlank(file.getContentType(), "Media type is missing");
    validateNotBlank(file.getOriginalFilename(), "Filename is missing");
    validateSize(file);
    return file;
  }

  private static void validateNotBlank(String s, String message) {
    if (isBlank(s)) {
      throw new ValidationException(message);
    }
  }

  private static void validateSize(final MultipartFile file) {
    final Long fileSize = file.getSize();
    if (fileSize > MAX_FILE_BYTES) {
      throw new ValidationException(
          String.format(
              "File size is %d bytes. File size cannot be greater than %d bytes",
              fileSize, MAX_FILE_BYTES));
    }
  }
}
