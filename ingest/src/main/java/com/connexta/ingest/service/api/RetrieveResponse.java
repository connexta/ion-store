/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.service.api;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

@Getter
public class RetrieveResponse {

  @NotNull private final MediaType mediaType;
  @NotNull private final Resource resource;

  @Autowired
  public RetrieveResponse(final MediaType mediaType, final Resource resource) {
    this.mediaType = mediaType;
    this.resource = resource;
  }
}
