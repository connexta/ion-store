/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.poller.service;

// TODO: Expectation is that this class would be provided by the Transform API
public class StatusResponse {

  private String status;

  // TODO: Lombok and Jackson are not playing well together. Had to remove all Lombok annotations.
  // Not sure what I was doing wrong. Solved by creating getter/setter methods.
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
