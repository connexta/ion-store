/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public abstract class MultiIntStoreIntegrationTest {

  @Autowired private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setup() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testContextLoads() {}

  @Test
  public void testRetrieve() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/retrieve/1"))
        .andDo(print())
        .andExpect(status().isOk());
  }
}
