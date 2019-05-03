/*
 * Copyright (c) Connexta
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the
 * GNU Lesser General Public License is distributed along with this
 * program and can be found at http://www.gnu.org/licenses/lgpl.html.
 */
package com.multiintstore;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multiintstore.callbacks.CallbackValidator;
import com.multiintstore.callbacks.FinishedCallback;
import com.multiintstore.callbacks.MetadataCallback;
import java.io.IOException;

import com.multiintstore.callbacks.ProductCallback;
import org.junit.jupiter.api.Test;

public class CallbackValidatorTest {

  private CallbackValidator validator = new CallbackValidator();
  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void MetadataCallbackTest() throws IOException {
    JsonNode validMetadata =
        mapper.readTree(
            "{\n"
                + "\t\"id\": \"42\",\n"
                + "\"status\": \"COMPLETE\",\n"
                + "\"type\": \"ddms2.0\",\n"
                + "\"mimeType\": \"application/xml\",\n"
                + "\"bytes\": \"256\",\n"
                + "\"location\": \"https://localhost:8080\",\n"
                + "\"security\": {\n"
                + "\t\"classification\": \"U\",\n"
                + "\t\"ownerProducer\": \"ownerProducer\"\n"
                + "}\n"
                + "}");
    assertThat(validator.parse(validMetadata), is(instanceOf(MetadataCallback.class)));
  }

  @Test
  public void ProductCallbackTest() throws IOException {
    JsonNode validMetadata =
        mapper.readTree(
            "{\n"
                + "\t\"id\": \"42\",\n"
                + "\"status\": \"COMPLETE\",\n"
                + "\"type\": \"product\",\n"
                + "\"security\": {\n"
                + "\t\"classification\": \"U\",\n"
                + "\t\"ownerProducer\": \"ownerProducer\"\n"
                + "}\n"
                + "}");
    assertThat(validator.parse(validMetadata), is(instanceOf(ProductCallback.class)));
  }

  @Test
  public void FinishedCallbackTest() throws IOException {
    JsonNode validMetadata =
        mapper.readTree(
            "{\n"
                + "\t\"id\": \"42\",\n"
                + "\"status\": \"COMPLETE\"}\n");
    assertThat(validator.parse(validMetadata), is(instanceOf(FinishedCallback.class)));
  }

  @Test
  public void InvalidSecurityMetadataCallbackTest() throws IOException {
    JsonNode validMetadata =
        mapper.readTree(
            "{\n"
                + "\t\"id\": \"42\",\n"
                + "\"status\": \"COMPLETE\",\n"
                + "\"type\": \"ddms2.0\",\n"
                + "\"mimeType\": \"application/xml\",\n"
                + "\"bytes\": \"256\",\n"
                + "\"location\": \"https://localhost:8080\",\n"
                + "\"security\": {\n"
                + "\t\"ownerProducer\": \"ownerProducer\"\n"
                + "}\n"
                + "}");
    assertThat(validator.parse(validMetadata), nullValue());
  }

  @Test
  public void MetadataCallbackMissingBytesTest() throws IOException {
    JsonNode validMetadata =
        mapper.readTree(
            "{\n"
                + "\t\"id\": \"42\",\n"
                + "\"status\": \"COMPLETE\",\n"
                + "\"type\": \"ddms2.0\",\n"
                + "\"mimeType\": \"application/xml\",\n"
                + "\"location\": \"https://localhost:8080\",\n"
                + "\"security\": {\n"
                + "\t\"ownerProducer\": \"ownerProducer\"\n"
                + "}\n"
                + "}");
    assertThat(validator.parse(validMetadata), nullValue());
  }

  @Test
  public void MetadataCallbackStringBytesTest() throws IOException {
    JsonNode validMetadata =
        mapper.readTree(
            "{\n"
                + "\t\"id\": \"42\",\n"
                + "\"status\": \"COMPLETE\",\n"
                + "\"type\": \"ddms2.0\",\n"
                + "\"mimeType\": \"application/xml\",\n"
                + "\"bytes\": \"Heh\",\n"
                + "\"location\": \"https://localhost:8080\",\n"
                + "\"security\": {\n"
                + "\t\"classification\": \"U\",\n"
                + "\t\"ownerProducer\": \"ownerProducer\"\n"
                + "}\n"
                + "}");
    assertThat(validator.parse(validMetadata), nullValue());
  }

  @Test
  public void FinishedCallbackWithMessageTest() throws IOException {
    JsonNode validMetadata =
        mapper.readTree(
            "{\n"
                + "\t\"id\": \"42\",\n"
                + "\"status\": \"Failed\",\n"
                + "\"message\": \"This data sucks\""
                + "}");
    assertThat(validator.parse(validMetadata), is(instanceOf(FinishedCallback.class)));
  }
}
