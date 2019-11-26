/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.service.api;

import com.connexta.store.rest.models.MetadataInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** Provides operations to take on Datasets. */
public interface StoreService {

  /**
   * Ingests a file and metacard.
   *
   * @param fileSize the size of the file
   * @param mimeType the type of the file
   * @param inputStream the contents of the file
   * @param fileName the file's name
   * @param metacardFileSize the metacard's size
   * @param metacardInputStream the xml content of the metacard
   */
  void ingest(
      final long fileSize,
      final String mimeType,
      final InputStream inputStream,
      final String fileName,
      final long metacardFileSize,
      final InputStream metacardInputStream);

  /**
   * Retrieves a Data from a Dataset.
   *
   * @param datasetId id of the Dataset to fetch the Data from
   * @param dataType the type of the Data to fetch
   * @return the {@link IonData}
   * @throws IOException if there was an error reading the Data
   */
  IonData getData(String datasetId, String dataType) throws IOException;

  /**
   * Adds a set of Data to a Dataset.
   *
   * @param datasetId id of the Dataset to add Data to
   * @param metadataInfos list of Metadata info to add
   * @throws IOException if there was an error reading one of the Metadatas
   */
  void addMetadata(String datasetId, List<MetadataInfo> metadataInfos) throws IOException;

  void quarantine(String datasetId);
}
