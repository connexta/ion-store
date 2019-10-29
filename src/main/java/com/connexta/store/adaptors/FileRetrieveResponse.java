/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import java.io.InputStream;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.MediaType;

@Data
public class FileRetrieveResponse {

  @NotNull private final MediaType mediaType;
  @NotNull private final InputStream inputStream;
  @NotBlank private final String fileName;
}
