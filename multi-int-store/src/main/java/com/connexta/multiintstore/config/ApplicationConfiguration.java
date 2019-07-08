/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

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

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.errorHandler(new DefaultResponseErrorHandler()).build();
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    final CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeClientInfo(true);
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setIncludeHeaders(true);
    filter.setAfterMessagePrefix("Inbound Request: ");
    filter.setMaxPayloadLength(5120);
    return filter;
  }
}
