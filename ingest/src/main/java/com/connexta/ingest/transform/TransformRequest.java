/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransformRequest implements Serializable {

  private String id;
  private String stagedLocation;
  private String productLocation;
  private String mimeType;
  private String bytes;
  private String callbackUrl;
}
