/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.exception.SearchException;
import com.connexta.multiintstore.services.api.SearchService;
import java.net.URL;
import java.util.List;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestController
@RequestMapping(value = "/search")
@AllArgsConstructor
@Validated
public class QueryController {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

  private SearchService searchService;

  @GetMapping
  @ResponseBody
  public ResponseEntity<List<URL>> searchKeyword(
      @RequestParam(value = "q") @NotEmpty final String keyword) {
    try {
      return new ResponseEntity<>(searchService.find(keyword), HttpStatus.OK);
    } catch (SearchException e) {
      LOGGER.warn("Unable to search for {}", keyword, e);
    }

    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ControllerAdvice
  private class ConstraintViolationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolation(
        @NotNull final ConstraintViolationException e, @NotNull final WebRequest request) {
      final String message = e.getMessage();
      final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
      LOGGER.warn("Request is invalid: {}. Returning {}.", message, httpStatus, e);
      return handleExceptionInternal(e, message, new HttpHeaders(), httpStatus, request);
    }
  }
}
