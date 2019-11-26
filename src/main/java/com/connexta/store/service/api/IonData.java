/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.api;

import java.io.InputStream;
import javax.annotation.Nullable;
import lombok.Data;

/** Represents a piece of Data within a Dataset. */
@Data
public class IonData {

  private final String mediaType;
  private final InputStream inputStream;
  @Nullable private final String fileName;
}
