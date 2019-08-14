/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolrClientConfiguration {

  @NotBlank private final String host;
  private final int port;
}
