/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class TransformResponse {

  private String id;
  private String message;
  private List<String> details;
  private HttpStatus status;

  @JsonIgnore
  public boolean isError() {
    return getStatus() == null || getStatus().isError();
  }
}
