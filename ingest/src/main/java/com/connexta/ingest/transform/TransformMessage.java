package com.connexta.ingest.transform;

import java.io.Serializable;

public class TransformMessage implements Serializable {

  private final String id;
  private final String stagedLocation;
  private final String productLocation;
  private final String mimeType;
  private final String bytes;
  private final String callbackUrl;

  public TransformMessage() {
    id = "30f14c6c1fc85cba12bfd093aa8f90e3";
    stagedLocation = "https://<host>:<port>/my/temp/location/e1bd38ddb07444958cf3a18dd6291518";
    productLocation = "https://<host>:<port>/my/final/location/30f14c6c1fc85cba12bfd093aa8f90e3";
    mimeType = "img/nitf";
    bytes = "2147483648";
    callbackUrl = "https://0.0.0.0:8090/a/callback";
  }

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
}
