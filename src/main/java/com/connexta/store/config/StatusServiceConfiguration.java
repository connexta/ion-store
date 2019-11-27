/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.poller.StatusService;
import com.connexta.store.poller.TransformPollerFactory;
import com.connexta.store.poller.TransformStatusTask;
import java.util.concurrent.BlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatusServiceConfiguration {

  @Bean(initMethod = "init", destroyMethod = "destroy")
  public StatusService statusService(
      BlockingQueue<TransformStatusTask> transformStatusQueue,
      TransformPollerFactory transformPollerFactory) {
    return new StatusService(transformStatusQueue, transformPollerFactory);
  }
}
