/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.libcore.net;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import libcore.net.MimeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests {@link MimeMap} and {@link MimeMap.Builder}.
 */
public class MimeMapTest {

    private MimeMap mimeMap;
    private MimeMap emptyMap;

    @Before public void setUp() {
        mimeMap = MimeMap.getDefault();
        emptyMap = MimeMap.builder().build();
    }

    @After public void tearDown() {
        mimeMap = null;
    }

    @Test public void invalidExtension() {
        assertNull(mimeMap.guessMimeTypeFromExtension(null));
        assertNull(mimeMap.guessMimeTypeFromExtension(""));
        assertFalse(mimeMap.hasExtension(null));
        assertFalse(mimeMap.hasExtension(""));
    }

    @Test public void invalidMimeType() {
        assertNull(mimeMap.guessExtensionFromMimeType(null));
        assertNull(mimeMap.guessExtensionFromMimeType(""));
        assertFalse(mimeMap.hasMimeType(null));
        assertFalse(mimeMap.hasMimeType(""));
    }

    @Test public void caseNormalization() {
        mimeMap = MimeMap.builder()
                .put("application/msWord", Arrays.asList("Doc"))
                .build();
        assertEquals("application/msword", mimeMap.guessMimeTypeFromExtension("dOc"));
        assertEquals("doc", mimeMap.guessExtensionFromMimeType("appliCATion/mSWOrd"));
        assertTrue(mimeMap.hasMimeType("application/msword"));
        assertTrue(mimeMap.hasMimeType("Application/mSWord"));

        assertTrue(mimeMap.hasExtension("doc"));
        assertTrue(mimeMap.hasExtension("DOC"));
        assertTrue(mimeMap.hasExtension("dOc"));
    }

    @Test public void unmapped() {
        mimeMap = MimeMap.builder()
                .put("mime/test", Arrays.asList("test", "tst"))
                .build();
        assertNull(mimeMap.guessExtensionFromMimeType("mime/unknown"));
        assertFalse(mimeMap.hasMimeType("mime/unknown"));

        assertNull(mimeMap.guessMimeTypeFromExtension("absent"));
        assertFalse(mimeMap.hasExtension("absent"));
    }

    @Test public void setDefault() {
        MimeMap defaultMimeMap = MimeMap.getDefault();
        MimeMap otherMimeMap = MimeMap.builder().put("text/plain", "txt").build();
        MimeMap.setDefault(otherMimeMap);
        try {
            assertEquals(otherMimeMap, MimeMap.getDefault());
        } finally {
            MimeMap.setDefault(defaultMimeMap);
        }
    }

    @Test public void setDefault_null() {
        MimeMap defaultMimeMap = MimeMap.getDefault();
        try {
            MimeMap.setDefault(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(defaultMimeMap, MimeMap.getDefault());
        }
    }

    @Test public void buildUpon() {
        mimeMap = MimeMap.builder()
                .build();
        assertMap(
                makeMap(),
                makeMap(),
                mimeMap);

        mimeMap = mimeMap.buildUpon()
                .build();
        assertMap(
                makeMap(),
                makeMap(),
                mimeMap);

        mimeMap = mimeMap.buildUpon()
                .put("text/plain", "txt")
                .build();
        assertMap(
                makeMap("text/plain", "txt"),
                makeMap("txt", "text/plain"),
                mimeMap);

        mimeMap = mimeMap.buildUpon()
                .put("audio/mpeg", Arrays.asList("mp2", "mp3"))
                .build();
        assertMap(
                makeMap("audio/mpeg", "mp2",
                        "text/plain", "txt"),
                makeMap("mp2", "audio/mpeg",
                        "mp3", "audio/mpeg",
                        "txt", "text/plain"),
                mimeMap);

        mimeMap = mimeMap.buildUpon()
                .put("text/plain", "text")
                .build();
        assertMap(
                makeMap("audio/mpeg", "mp2",
                        "text/plain", "text"),
                makeMap("mp2", "audio/mpeg",
                        "mp3", "audio/mpeg",
                        "text", "text/plain",
                        "txt", "text/plain"),
                mimeMap);
    }

    @Test public void put() {
        MimeMap a = MimeMap.builder()
                .put("text/plain", Arrays.asList("txt", "text"))
                .put("application/msword", "doc")
                .build();
        MimeMap b = MimeMap.builder()
                .put("text/plain", Arrays.asList("txt", "text"))
                .put("application/msword", "doc")
                .build();
        assertEqualsButNotSame(a, b);
        assertEqualsButNotSame(a, a.buildUpon().build());
        assertMap(
                makeMap(
                        "text/plain", "txt",
                        "application/msword", "doc"),
                makeMap("txt", "text/plain",
                        "text", "text/plain",
                        "doc", "application/msword"),
                a);
    }

    @Test public void put_noExtensions() {
        checkPut_noExtensions(emptyMap);
        checkPut_noExtensions(MimeMap.builder().put("text/plain", "txt").build());
        checkPut_noExtensions(mimeMap);
    }

    /**
     * Checks that put(String, emptyList()) doesn't change or add any mappings.
     */
    private static void checkPut_noExtensions(MimeMap baseMap) {
        MimeMap mimeMap = baseMap.buildUpon()
                .put("mime/type", Collections.emptyList())
                .build();
        assertEquals(baseMap, mimeMap);
    }

    @Test public void put_String_List_nullOrEmpty() {
        // We still check mimeType for validity even if no extensions are specified
        assertPutThrowsNpe(null);
        assertPutThrowsIae("");

        // null or "" are not allowed for either MIME type or extension
        assertPutThrowsNpe(null, "ext");
        assertPutThrowsIae("", "ext");
        assertPutThrowsNpe("mime", null);
        assertPutThrowsIae("mime", "");

        assertPutThrowsNpe("mime", "ext", null);
        assertPutThrowsIae("mime", "ext", "");
    }

    @Test public void put_String_String_nullOrEmpty() {
        assertThrowsNpe(() -> MimeMap.builder().put(null, "ext"));
        assertThrowsIae(() -> MimeMap.builder().put("", "ext"));

        assertThrowsNpe(() -> MimeMap.builder().put("mime", (String) null));
        assertThrowsIae(() -> MimeMap.builder().put("mime", ""));
    }

    /**
     * Tests put() arguments that have a prefix {@code "?"} which leads to putIfAbsent semantics.
     */
    @Test public void putIfAbsent() {
        // Starting from an empty mapping, add a bunch more, some with and some without '?'.
        mimeMap = MimeMap.builder()
                .put("?text/plain", "?txt")
                .put("audio/mpeg", Arrays.asList("mpga", "mpega", "?mp2", "mp3"))
                .build();
        assertEquals("txt", mimeMap.guessExtensionFromMimeType("text/plain"));
        assertEquals("text/plain", mimeMap.guessMimeTypeFromExtension("txt"));
        assertEquals("mpga", mimeMap.guessExtensionFromMimeType("audio/mpeg"));
        assertEquals("audio/mpeg", mimeMap.guessMimeTypeFromExtension("mp2"));
        assertEquals("audio/mpeg", mimeMap.guessMimeTypeFromExtension("mp3"));

        // Override a ext -> MIME mapping without overriding the MIME -> ext mapping.
        mimeMap = mimeMap.buildUpon()
                .put("?audio/mpeg", "m4a")
                .build();
        assertEquals("mpga", mimeMap.guessExtensionFromMimeType("audio/mpeg"));
        assertEquals("audio/mpeg", mimeMap.guessMimeTypeFromExtension("m4a"));

        // Override a MIME -> ext mapping without overriding the ext -> MIME mapping.
        mimeMap = mimeMap.buildUpon()
                .put("audio/mpeg", "?txt")
                .build();
        assertEquals("txt", mimeMap.guessExtensionFromMimeType("audio/mpeg"));
        assertEquals("text/plain", mimeMap.guessMimeTypeFromExtension("txt"));


        // Check final state
        assertMap(
                makeMap(
                        "text/plain", "txt",
                        "audio/mpeg", "txt"
                        ),
                makeMap(
                        "txt", "text/plain",
                        "m4a", "audio/mpeg",
                        "mp2", "audio/mpeg",
                        "mp3", "audio/mpeg",
                        "mpega", "audio/mpeg",
                        "mpga", "audio/mpeg"
                ),
                mimeMap
        );
    }

    /**
     * Tests invalid put() invocations that have '?' in additional/invalid places.
     */
    @Test public void put_additionalQuestionMarks() {
        // Potentially we could tolerate additional ? as a prefix in future, but right now we don't.
        assertPutThrowsIae("??text/plain", "txt");
        assertPutThrowsIae("text/p?lain", "txt");
        assertPutThrowsIae("text/plain", "txt", "t?ext");
        assertPutThrowsIae("text/plain", "??txt");
        assertPutThrowsIae("text/plain", "t?xt");
    }

    private static void assertPutThrowsNpe(String mime, String... exts) {
        assertThrowsNpe(() -> MimeMap.builder().put(mime, Arrays.asList(exts)));    }

    private static void assertPutThrowsIae(final String mime, final String... exts) {
        assertThrowsIae(() -> MimeMap.builder().put(mime, Arrays.asList(exts)));
    }

    private static void assertThrowsNpe(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private static void assertThrowsIae(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test public void hashCodeValue() {
        assertEquals(0, emptyMap.hashCode());
        MimeMap a = MimeMap.builder().put("mime/test", "test").build();
        MimeMap b = a.buildUpon().put("foo/bar", "baz").build();
        assertTrue(0 != a.hashCode());
        assertTrue((a.hashCode() != b.hashCode()));
    }

    @Test public void empty_copies() {
        assertEqualsButNotSame(emptyMap, MimeMap.builder().build());
        assertEqualsButNotSame(emptyMap, emptyMap.buildUpon().build());
    }

    /** Creates a map from alternating key/value arguments, useful for test assertions. */
    private static Map<String, String> makeMap(String... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Invalid length " + keysAndValues.length + ": " + keysAndValues);
        }
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            String key = keysAndValues[i];
            String value = keysAndValues[i + 1];
            result.put(key, value);
        }
        return result;

    }

    /**
     * Asserts that the given {@code MimeMap} has exactly the given mime -> ext and ext -> mime
     * mappings, but no others.
     */
    private static<T> void assertMap(
            Map<String, String> expectedMimeToExt,
            Map<String, String> expectedExtToMime,
            MimeMap mimeMap)
    {
        MimeMap.Builder expectedBuilder = MimeMap.builder();
        for (Map.Entry<String, String> entry : expectedExtToMime.entrySet()) {
            String ext = entry.getKey();
            String mime = entry.getValue();
            assertEquals(ext + ": " + mimeMap, mime, mimeMap.guessMimeTypeFromExtension(ext));
            expectedBuilder.put("?" + mime, ext);
        }
        for (Map.Entry<String, String> entry : expectedMimeToExt.entrySet()) {
            String mime = entry.getKey();
            String ext = entry.getValue();
            assertEquals(mime + ": "  + mimeMap, ext, mimeMap.guessExtensionFromMimeType(mime));
            expectedBuilder.put(mime, "?" + ext);
        }
        // Check that there are no unexpected additional mappings.
        assertEqualsButNotSame(expectedBuilder.build(), mimeMap);
    }

    private static<T> void assertEqualsButNotSame(T a, T b) {
        assertEquals(a, b);
        assertEquals(b, a);
        assertNotSame(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

}
