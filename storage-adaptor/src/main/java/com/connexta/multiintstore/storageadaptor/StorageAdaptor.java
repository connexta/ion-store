/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.storageadaptor;

import java.io.IOException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public interface StorageAdaptor {

  void store(
      @NotEmpty final String mimeType,
      @NotNull final MultipartFile file,
      @NotEmpty final String fileName,
      @NotEmpty final String key)
      throws IOException;

  @NotNull
  RetrieveResponse retrieve(final String key) throws IOException;
}
