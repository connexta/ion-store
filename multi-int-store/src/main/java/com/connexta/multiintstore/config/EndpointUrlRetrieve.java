/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import lombok.Getter;

public class EndpointUrlRetrieve {

  @Getter private String endpointUrlRetrieve;

  public EndpointUrlRetrieve(String endpointUrlRetrieve) {
    this.endpointUrlRetrieve = endpointUrlRetrieve;
  }
}
