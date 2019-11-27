/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.poller;

import org.springframework.web.reactive.function.client.WebClient;

public class TransformPollerFactory {

  private final WebClient storeWebClient;

  private final WebClient transformWebClient;

  /** Creates a new TransformPollerFactory. */
  public TransformPollerFactory(WebClient transformWebClient, WebClient storeWebClient) {
    this.transformWebClient = transformWebClient;
    this.storeWebClient = storeWebClient;
  }

  public TransformStatusPoller newPoller(TransformStatusTask task) {
    return new TransformStatusPoller(task, transformWebClient, storeWebClient);
  }
}
