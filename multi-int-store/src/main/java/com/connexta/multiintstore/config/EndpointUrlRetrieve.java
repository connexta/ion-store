/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import javax.validation.constraints.NotEmpty;
import lombok.Getter;

public class EndpointUrlRetrieve {

  @Getter @NotEmpty private String endpointUrlRetrieve;

  public EndpointUrlRetrieve(@NotEmpty final String endpointUrlRetrieve) {
    this.endpointUrlRetrieve = endpointUrlRetrieve;
  }
}
