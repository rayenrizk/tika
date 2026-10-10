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

import org.junit.jupiter.api.Test;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

public class FilenameUtilsMutationTest {

    @Test
    void testEmbeddedPathFromEmbeddedResourcePath() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.EMBEDDED_RESOURCE_PATH,
                "resource/file.txt");

        assertEquals(
                "resource/file.txt",
                FilenameUtils.getSanitizedEmbeddedFilePath(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedPathFromInternalPath() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.INTERNAL_PATH,
                "internal/file.txt");

        assertEquals(
                "internal/file.txt",
                FilenameUtils.getSanitizedEmbeddedFilePath(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedPathFromResourceName() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.RESOURCE_NAME_KEY,
                "resourceName/file.txt");

        assertEquals(
                "resourceName/file.txt",
                FilenameUtils.getSanitizedEmbeddedFilePath(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedPathFromRelationshipId() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.EMBEDDED_RELATIONSHIP_ID,
                "relationship/file.txt");

        assertEquals(
                "relationship/file.txt",
                FilenameUtils.getSanitizedEmbeddedFilePath(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedPathFromOriginalResourceName() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.ORIGINAL_RESOURCE_NAME,
                "original/file.txt");

        assertEquals(
                "original/file.txt",
                FilenameUtils.getSanitizedEmbeddedFilePath(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedNameFromResourceName() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.RESOURCE_NAME_KEY,
                "resource.txt");

        assertEquals(
                "resource.txt",
                FilenameUtils.getSanitizedEmbeddedFileName(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedNameFromInternalPath() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.INTERNAL_PATH,
                "internal.txt");

        assertEquals(
                "internal.txt",
                FilenameUtils.getSanitizedEmbeddedFileName(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedNameFromRelationshipId() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.EMBEDDED_RELATIONSHIP_ID,
                "relationship.txt");

        assertEquals(
                "relationship.txt",
                FilenameUtils.getSanitizedEmbeddedFileName(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedNameFromEmbeddedResourcePath() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.EMBEDDED_RESOURCE_PATH,
                "embedded.txt");

        assertEquals(
                "embedded.txt",
                FilenameUtils.getSanitizedEmbeddedFileName(
                        metadata, ".bin", 255));
    }

    @Test
    void testEmbeddedNameFromOriginalResourceName() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.ORIGINAL_RESOURCE_NAME,
                "original.txt");

        assertEquals(
                "original.txt",
                FilenameUtils.getSanitizedEmbeddedFileName(
                        metadata, ".bin", 255));
    }
    





        @Test
    void testSuffixExactlyFiveCharactersIsRejected() {
        assertEquals(
                "",
                FilenameUtils.getSuffixFromPath("file.abcde"));
    }

    @Test
    void testEmbeddedFileNameExactlyAtMaxLength() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.RESOURCE_NAME_KEY,
                "abcdefghij.txt");

        assertEquals(
                "abcdefghij.txt",
                FilenameUtils.getSanitizedEmbeddedFileName(
                        metadata, ".bin", 10));
    }

    @Test
    void testEmbeddedPathExactlyAtMaxLength() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.EMBEDDED_RESOURCE_PATH,
                "a/b.txt");

        assertEquals(
                "a/b.txt",
                FilenameUtils.getSanitizedEmbeddedFilePath(
                        metadata, ".bin", 7));
    }

    @Test
    void testEmbeddedPathNamePartExactlyAtMaxLength() {
        Metadata metadata = new Metadata();
        metadata.set(
                TikaCoreProperties.EMBEDDED_RESOURCE_PATH,
                "dir/abcdefghij.txt");

        assertEquals(
                "abcdefghij.txt",
                FilenameUtils.getSanitizedEmbeddedFilePath(
                        metadata, ".bin", 10));
    }
}
