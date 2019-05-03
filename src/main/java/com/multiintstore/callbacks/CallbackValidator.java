/*
 * Copyright (c) Connexta
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the
 * GNU Lesser General Public License is distributed along with this
 * program and can be found at http://www.gnu.org/licenses/lgpl.html.
 */
package com.multiintstore.callbacks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class CallbackValidator {

  private final ObjectMapper mapper = new ObjectMapper();
  private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  private final Validator validator = factory.getValidator();

  public CallbackValidator() {}

  public Object parse(JsonNode body) {
    //  Put these in the order of largest to smallest?
    Class<?>[] list = {MetadataCallback.class, ProductCallback.class, FinishedCallback.class};

    for (Class<?> clas : list) {
      Object temp;
      if ((temp = parseCallback(body, clas)) != null) {
        return temp;
      }
    }

    System.out.println(body + " is not valid callback");

    return null;
  }

  private Object parseCallback(JsonNode body, Class<?> clas) {
    try {
      Object callback = mapper.convertValue(body, clas);
      return validator.validate(callback).isEmpty() ? callback : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
