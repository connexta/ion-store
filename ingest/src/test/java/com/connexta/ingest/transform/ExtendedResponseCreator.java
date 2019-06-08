package com.connexta.ingest.transform;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;

public class ExtendedResponseCreator extends DefaultResponseCreator {

  protected ExtendedResponseCreator(HttpStatus statusCode) {
    super(statusCode);
  }

}
