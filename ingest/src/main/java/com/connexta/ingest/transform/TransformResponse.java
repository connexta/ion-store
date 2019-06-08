package com.connexta.ingest.transform;

public class TransformResponse {

  private String id;
  private String message;
  //  private List<String> details;

  //  @JsonIgnore private HttpStatus status;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  //  public List<String> getDetails() {
  //    return details;
  //  }
  //
  //  public void setDetails(List<String> details) {
  //    this.details = details;
  //  }

  //  public HttpStatus getStatus() {
  //    return status;
  //  }

  //  public void setStatus(HttpStatus status) {
  //    this.status = status;
  //  }

  public boolean isError() {
    return false;
    //    return getStatus() != null || getStatus().isError();
  }
}
