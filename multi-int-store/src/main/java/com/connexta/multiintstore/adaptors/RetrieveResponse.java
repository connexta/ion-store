/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.adaptors;

import java.io.InputStream;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
public class RetrieveResponse {

  @NotNull private final MediaType mediaType;
  @NotNull private final InputStream inputStream;
  @NotEmpty private final String fileName;

  public RetrieveResponse(
      @NotNull final MediaType mediaType,
      @NotNull final InputStream inputStream,
      @NotEmpty final String fileName) {
    this.mediaType = mediaType;
    this.inputStream = inputStream;
    this.fileName = fileName;
  }
}
