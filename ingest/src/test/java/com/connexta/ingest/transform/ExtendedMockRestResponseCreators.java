/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;

public class ExtendedMockRestResponseCreators extends MockRestResponseCreators {

  /**
   * {@code ResponseCreator} for a response with String body.
   *
   * @param status the HTTP status
   * @param body the response body, a "UTF-8" string
   * @param contentType the type of the content (may be {@code null})
   */
  public static ResponseCreator withStatus(HttpStatus status, String body, MediaType contentType) {
    return new ExtendedResponseCreator(status).body(body).contentType(contentType);
  }

  /**
   * {@code ResponseCreator} for a response with JSON body.
   *
   * @param status the HTTP status
   * @param object an object to be serialize as JSON
   */
  public static ResponseCreator withStatus(HttpStatus status, Object object)
      throws JsonProcessingException {
    return withStatus(status, asJson(object), MediaType.APPLICATION_JSON);
  }

  /**
   * {@code ResponseCreator} for a response 200 with JSON body.
   *
   * @param object an object to be serialize as JSON
   */
  public static ResponseCreator withSuccess(Object object) throws JsonProcessingException {
    return withStatus(HttpStatus.ACCEPTED, object);
  }

  /**
   * {@code ResponseCreator} for a response 400 with JSON body.
   *
   * @param object an object to be serialize as JSON
   */
  public static ResponseCreator withBadRequest(Object object) throws JsonProcessingException {
    return withStatus(HttpStatus.BAD_REQUEST, object);
  }

  /**
   * {@code ResponseCreator} for a response 500 with JSON body.
   *
   * @param object an object to be serialize as JSON
   */
  public static ResponseCreator withServerError(Object object) throws JsonProcessingException {
    return withStatus(HttpStatus.INTERNAL_SERVER_ERROR, object);
  }

  private static String asJson(Object object) throws JsonProcessingException {
    return (new ObjectMapper()).writeValueAsString(object);
  }
}
