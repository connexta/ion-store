/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;

public class CallbackAcceptVersion {

  @Getter @NotEmpty private String callbackAcceptVersion;

  public CallbackAcceptVersion(@NotNull @NotEmpty String callbackAcceptVersion) {
    this.callbackAcceptVersion = callbackAcceptVersion;
  }
}
