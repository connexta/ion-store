/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.callbacks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(CallbackValidator.class);

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

    LOGGER.warn("{} is not a valid callback", body);

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
