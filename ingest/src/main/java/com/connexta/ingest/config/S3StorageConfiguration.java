/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@Getter
public class S3StorageConfiguration {

  @Value("${aws.s3.endpointUrl}")
  private String s3Endpoint;

  @Value("${aws.s3.region}")
  private String s3Region;

  @Value("${aws.s3.secret.file}")
  private String awsSecretKeyFile;

  @Value("${aws.s3.access.file}")
  private String awsAccessKeyFile;

  private String s3SecretKey;

  private String s3AccessKey;

  @Value("${aws.s3.bucket.quarantine}")
  private String s3BucketQuarantine;

  @PostConstruct
  public void initialize() throws IOException {
    s3AccessKey = FileUtils.readFileToString(new File(awsAccessKeyFile), StandardCharsets.UTF_8);
    s3SecretKey = FileUtils.readFileToString(new File(awsSecretKeyFile), StandardCharsets.UTF_8);
  }
}
