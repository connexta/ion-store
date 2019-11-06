/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.api;

import com.dyngr.core.AttemptMaker;
import com.dyngr.core.AttemptResult;

/** This interface is the boundary between the polling library and client code. */
public interface PollingTask<T> extends AttemptMaker<T> {

  // AttemptResult is a class from the open source polling library
  AttemptResult<T> process();
}
