/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.callbacks;

import java.net.URL;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
public class MetadataCallback {
  @NotNull private String id; // TODO this should be removed from the request API

  @NotNull private String status;

  @NotNull private String type;

  @NotNull private String mimeType;

  @NotNull private Long bytes;

  @NotNull private URL location;

  @Valid @NotNull private Markings security;
}
