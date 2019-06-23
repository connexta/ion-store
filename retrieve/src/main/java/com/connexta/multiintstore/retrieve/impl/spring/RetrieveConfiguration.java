/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.retrieve.impl.spring;

import com.connexta.multiintstore.retrieve.RetrieveService;
import com.connexta.multiintstore.retrieve.impl.RetrieveServiceImpl;
import com.connexta.multiintstore.storageadaptor.StorageAdaptor;
import javax.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class RetrieveConfiguration {

  @Bean
  public RetrieveService retrieveService(@NotNull final StorageAdaptor storageAdaptor) {
    return new RetrieveServiceImpl(storageAdaptor);
  }
}
