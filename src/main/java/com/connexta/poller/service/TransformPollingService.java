/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.service;

import com.connexta.poller.api.PollingService;
import com.connexta.poller.api.PollingTask;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * This is class is meant to be instantiated as a Bean, Component, or Service. The class's
 * responsibilities are to manage the settings for polling and kick off new polling tasks. Other
 * responsibilities include settings the wait and retry behavior. It also creates and configures the
 * executor, and ensures that it is shutdown properly.
 */
@Service
@Slf4j
public class TransformPollingService implements PollingService {

  @Override
  public <T> Future<T> poll(PollingTask<T> pollingTask) {
    return null;
  }
}
