/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.retrieve.RetrieveService;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetrieveController
    extends com.connexta.multiintstore.retrieve.impl.spring.RetrieveController {

  public RetrieveController(@NotNull RetrieveService retrieveService) {
    super(retrieveService);
  }
}
