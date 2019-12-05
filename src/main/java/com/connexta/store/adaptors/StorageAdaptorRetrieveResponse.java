/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import java.io.InputStream;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Generated;
import org.springframework.http.MediaType;

@Data
@Generated
public class StorageAdaptorRetrieveResponse {

  @NotNull private final MediaType mediaType;
  @NotNull private final InputStream inputStream;
  @NotNull private final Map<String, String> metadata;
}
