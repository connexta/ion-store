/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AmazonS3Configuration {

  @NotBlank private final String endpoint;
  @NotBlank private final String region;
  @NotBlank private final String accessKey;
  @NotBlank private final String secretKey;
}
