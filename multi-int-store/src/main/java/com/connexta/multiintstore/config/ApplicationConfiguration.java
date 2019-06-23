/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.config;

import com.connexta.multiintstore.storageadaptor.impl.s3.spring.S3StorageAdaptorConfiguration;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
@Import({S3StorageAdaptorConfiguration.class})
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

  @NotEmpty
  @Value("${aws.s3.bucket}")
  private String s3Bucket;

  @NotNull
  @Bean
  public String s3Bucket() {
    return s3Bucket;
  }
}
