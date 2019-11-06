/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.api;

import java.util.concurrent.Future;

/**
 * This service accepts a PollingTask and returns a Future. The Future provides the results of the
 * polling task.
 */
public interface PollingService {

  <T> Future<T> poll(PollingTask<T> pollingTask);
}
