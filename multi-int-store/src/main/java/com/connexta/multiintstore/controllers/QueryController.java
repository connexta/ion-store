/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.models.IndexedProductMetadata;
import com.connexta.multiintstore.services.api.SearchService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/search")
@AllArgsConstructor
public class QueryController {

  private SearchService searchService;

  @RequestMapping(method = RequestMethod.GET, params = "q")
  @ResponseBody
  public List<IndexedProductMetadata> searchKeyword(@RequestParam(value = "q") String keyword) {
    return searchService.find(keyword);
  }
}
