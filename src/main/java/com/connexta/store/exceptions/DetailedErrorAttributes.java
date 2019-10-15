/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.common.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.WebRequest;

/**
 * Enhanced version of Spring's {@link DefaultErrorAttributes} capable of adding details information
 * from known exceptions.
 */
@Component
public class DetailedErrorAttributes extends DefaultErrorAttributes {
  @Override
  public Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {
    final Map<String, Object> errorAttributes =
        super.getErrorAttributes(request, includeStackTrace);
    final Throwable error = getError(request);
    final int code = determineCode(error);
    final List<String> details = determineDetails(error);

    if (code != -1) {
      errorAttributes.put("code", code);
    }
    if (!CollectionUtils.isEmpty(details)) {
      errorAttributes.put("details", details);
    }
    return errorAttributes;
  }

  private int determineCode(Throwable error) {
    if (error instanceof Detailable) {
      return ((Detailable) error).getCode();
    }
    final DetailedResponseStatus responseStatus =
        AnnotatedElementUtils.findMergedAnnotation(error.getClass(), DetailedResponseStatus.class);

    if (responseStatus != null) {
      return responseStatus.code();
    }
    return -1;
  }

  private List<String> determineDetails(Throwable error) {
    if (error instanceof Detailable) {
      return ((Detailable) error).getDetails();
    }
    final DetailedResponseStatus responseStatus =
        AnnotatedElementUtils.findMergedAnnotation(error.getClass(), DetailedResponseStatus.class);

    if (responseStatus != null) {
      return Arrays.asList(responseStatus.details());
    }
    // for now, simply provide the exception message of the exception and its causes
    // re-think what we want to do here
    final List<String> details = new ArrayList<>();

    while (error != null) {
      details.add(error.getMessage());
      error = error.getCause();
    }
    return details;
  }
}
