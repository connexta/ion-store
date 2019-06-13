/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import org.json.JSONObject;

public class TestUtil {

  public static String createMetadataCallbackJson(
      String id,
      String status,
      String type,
      String mimeType,
      int bytes,
      String location,
      String classification,
      String ownerProducer)
      throws Exception {
    return new JSONObject()
        .put("id", id)
        .put("status", status)
        .put("type", type)
        .put("mimeType", mimeType)
        .put("bytes", bytes)
        .put("location", location)
        .put(
            "security",
            new JSONObject()
                .put("classification", classification)
                .put("ownerProducer", ownerProducer))
        .toString();
  }
}
