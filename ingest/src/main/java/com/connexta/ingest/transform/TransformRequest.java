/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import java.io.Serializable;

public class TransformRequest implements Serializable {

  private String id;
  private String stagedLocation;
  private String productLocation;
  private String mimeType;
  private String bytes;
  private String callbackUrl;

  public TransformRequest() {}

  public String getId() {
    return id;
  }

  public String getStagedLocation() {
    return stagedLocation;
  }

  public String getProductLocation() {
    return productLocation;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getBytes() {
    return bytes;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setStagedLocation(String stagedLocation) {
    this.stagedLocation = stagedLocation;
  }

  public void setProductLocation(String productLocation) {
    this.productLocation = productLocation;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setBytes(String bytes) {
    this.bytes = bytes;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }
}
