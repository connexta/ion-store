/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.adaptors;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

@Getter
public class RetrieveResponse {

  @NotNull private final MediaType mediaType;
  @NotNull private final Resource resource;

  public RetrieveResponse(@NotNull final MediaType mediaType, @NotNull final Resource resource) {
    this.mediaType = mediaType;
    this.resource = resource;
  }
}
