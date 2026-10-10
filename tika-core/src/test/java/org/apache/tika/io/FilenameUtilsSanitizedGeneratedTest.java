/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

/**
 * Six tests retained from ChatUniTest output and manually corrected.
 * The original generation is preserved separately.
 */
public class FilenameUtilsSanitizedGeneratedTest {

    private Metadata metadataWithName(String name) {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, name);
        return metadata;
    }

    @Test
    public void testEmptyResourceName() {
        Metadata metadata = metadataWithName("");
        assertNull(FilenameUtils.getSanitizedEmbeddedFileName(metadata, null, 100));
    }

    @Test
    public void testBlankResourceName() {
        Metadata metadata = metadataWithName("   ");
        assertNull(FilenameUtils.getSanitizedEmbeddedFileName(metadata, null, 100));
    }

    @Test
    public void testLongNameWithShortDefaultExtension() {
        Metadata metadata = metadataWithName("a" + "b".repeat(100) + ".txt");
        String result = FilenameUtils.getSanitizedEmbeddedFileName(metadata, "default", 100);
        assertEquals("a" + "b".repeat(92) + "...txt".replace("txt", ".txt"), result);
    }

    @Test
    public void testLongNameWithLongDefaultExtension() {
        Metadata metadata = metadataWithName("a" + "b".repeat(100) + ".txt");
        String result = FilenameUtils.getSanitizedEmbeddedFileName(metadata, "default.txt", 100);
        assertEquals("a" + "b".repeat(92) + "..." + ".txt", result);
    }

    @Test
    public void testLongNameWithAnotherDefaultExtension() {
        Metadata metadata = metadataWithName("a" + "b".repeat(100) + ".txt");
        String result = FilenameUtils.getSanitizedEmbeddedFileName(metadata, "longextension.txt", 100);
        assertEquals("a" + "b".repeat(92) + "..." + ".txt", result);
    }

    @Test
    public void testLongNameWithExistingExtensionAndNullDefault() {
        Metadata metadata = metadataWithName("a" + "b".repeat(100) + ".txt");
        String result = FilenameUtils.getSanitizedEmbeddedFileName(metadata, null, 100);
        assertEquals("a" + "b".repeat(92) + "..." + ".txt", result);
    }
}
