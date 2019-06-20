/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.services.api.SearchService;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.solr.UncategorizedSolrException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/search")
@AllArgsConstructor
public class QueryController {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

  private SearchService searchService;

  @RequestMapping(method = RequestMethod.GET, params = "q")
  @ResponseBody
  public ResponseEntity<List<URL>> searchKeyword(@RequestParam(value = "q") String keyword) {
    try {
      return new ResponseEntity<>(searchService.find(keyword), HttpStatus.OK);
    } catch (MalformedURLException e) {
      LOGGER.warn("Unable to construct URLs when querying for {}", keyword, e);
    } catch (UncategorizedSolrException e) {
      LOGGER.warn("Error querying solr for {}", keyword, e);
    }

    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
