/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class ApplicationConfiguration {

  /** Used to check the Accept-Version in the callback request */
  @NotEmpty
  @Value("${callback-accept-version}")
  private String callbackAcceptVersion;

  @Bean
  public CallbackAcceptVersion callbackAcceptVersion() {
    return new CallbackAcceptVersion(callbackAcceptVersion);
  }

  @NotEmpty
  @Value("${endpointUrl.retrieve}")
  private String endpointUrlRetrieve;

  @Bean
  public EndpointUrlRetrieve endpointUrlRetrieve() {
    return new EndpointUrlRetrieve(endpointUrlRetrieve);
  }
}
