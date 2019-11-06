/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class TransformPollingServiceUnitTests {

  private final TransformPollingService pollingService = new TransformPollingService();

  @Test
  void testPoll(final @Mock TransformPollingTask task)
      throws ExecutionException, InterruptedException {
    Future future = pollingService.poll(task);
    assertThat(future.isDone(), is(true));
    // TODO test a non-null result instead of null
    assertThat(future.get(), is(nullValue(null)));
  }
}
