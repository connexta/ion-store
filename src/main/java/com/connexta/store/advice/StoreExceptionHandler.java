/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.advice;

import com.connexta.store.exceptions.StoreException;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class StoreExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(StoreException.class)
  protected ResponseEntity<Object> handleStoreException(
      @NotNull final StoreException e, @NotNull final WebRequest request) {
    final String message = e.getMessage();
    final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    log.warn("Request is invalid: {}. Returning {}.", message, httpStatus, e);
    return handleExceptionInternal(e, message, new HttpHeaders(), httpStatus, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(
      @NotNull final ConstraintViolationException e, @NotNull final WebRequest request) {
    final String message = e.getMessage();
    final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    log.warn("Request is invalid: {}. Returning {}.", message, httpStatus, e);
    return handleExceptionInternal(e, message, new HttpHeaders(), httpStatus, request);
  }
}
