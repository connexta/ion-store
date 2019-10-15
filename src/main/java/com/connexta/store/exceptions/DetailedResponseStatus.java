/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.common.exceptions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/** Marks an exception class to provide additional details about the error. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DetailedResponseStatus {
  /** Alias for {@link #code}. */
  @AliasFor("code")
  int value() default -1;

  /**
   * Specifies a more specific code for the error that should be included in the resulting error
   * message (defaults to <code>-1</code>).
   *
   * @return a more specific code for the error or <code>-1</code> if no specific code is available
   */
  @AliasFor("value")
  int code() default -1;

  /**
   * Specifies additional details information about the error that should be included in the
   * resulting error message (defaults to none).
   *
   * @return additional information about the error
   */
  String[] details() default {};
}
