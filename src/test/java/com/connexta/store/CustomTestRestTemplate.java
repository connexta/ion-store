/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import java.io.IOException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;

/** Extend the functionality of the TestRestTemplate */
public class CustomTestRestTemplate extends TestRestTemplate {

  public CustomTestRestTemplate(ApplicationContext applicationContext) {
    // The LocalHostUriTemplateHandler will expand a path like "/dataset" in to a URL like
    // "https://localhost:8080/dataset
    getRestTemplate()
        .setUriTemplateHandler(
            new LocalHostUriTemplateHandler(applicationContext.getEnvironment()));
  }

  /**
   * Include additional headers to be sent with every request.
   *
   * @param additionalHeaders
   * @return the object
   */
  public TestRestTemplate addRequestHeaders(
      @NotNull MultiValueMap<String, String> additionalHeaders) {
    getRestTemplate().getInterceptors().add(new AddHeadersInterceptor(additionalHeaders));
    return this;
  }

  /**
   * Add one aditional header to be sent with every request.
   *
   * @param header
   * @param value
   * @return the object
   */
  public TestRestTemplate addRequestHeader(@NotBlank String header, @NotNull String value) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(header, value);
    addRequestHeaders(httpHeaders);
    return this;
  }

  class AddHeadersInterceptor implements ClientHttpRequestInterceptor {
    MultiValueMap<String, String> additionalHeaders;

    AddHeadersInterceptor(MultiValueMap<String, String> additionalHeaders) {
      this.additionalHeaders = additionalHeaders;
    }

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
      request.getHeaders().addAll(additionalHeaders);
      return execution.execute(request, body);
    }
  }
}
