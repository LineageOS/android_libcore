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

import java.util.Locale;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import libcore.net.MimeMap;
import libcore.util.NonNull;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class MimeMapTest {

    /** Exposes {@link MimeMap}'s protected methods publicly so that mock calls can be verified. */
    public static abstract class TestMimeMap extends MimeMap {
        @Override
        public abstract String guessMimeTypeFromLowerCaseExtension(@NonNull String extension);

        @Override
        public abstract String guessExtensionFromLowerCaseMimeType(@NonNull String mimeType);
    }

    private TestMimeMap mimeMap;
    private MimeMap defaultMimeMap;

    @Before public void setUp() {
        mimeMap = mock(TestMimeMap.class);
        defaultMimeMap = MimeMap.getDefault();
    }

    @After public void tearDown() {
        mimeMap = null;
    }

    @Test public void invalidExtension() {
        assertNull(mimeMap.guessMimeTypeFromExtension(null));
        assertNull(mimeMap.guessMimeTypeFromExtension(""));
        assertFalse(mimeMap.hasExtension(null));
        assertFalse(mimeMap.hasExtension(""));

        verify(mimeMap, never()).guessExtensionFromLowerCaseMimeType(anyString());
        verify(mimeMap, never()).guessMimeTypeFromLowerCaseExtension(anyString());

    }

    @Test public void invalidMimeType() {
        assertNull(mimeMap.guessExtensionFromMimeType(null));
        assertNull(mimeMap.guessExtensionFromMimeType(""));
        assertFalse(mimeMap.hasMimeType(null));
        assertFalse(mimeMap.hasMimeType(""));

        verify(mimeMap, never()).guessExtensionFromLowerCaseMimeType(anyString());
        verify(mimeMap, never()).guessMimeTypeFromLowerCaseExtension(anyString());
    }

    @Test public void caseNormalization() {
        when(mimeMap.guessExtensionFromLowerCaseMimeType("application/msword")).thenReturn("DoC");
        when(mimeMap.guessMimeTypeFromLowerCaseExtension("doc")).thenReturn("APPLication/msWORD");

        assertEquals("application/msword", mimeMap.guessMimeTypeFromExtension("dOc"));
        assertEquals("doc", mimeMap.guessExtensionFromMimeType("appliCATion/mSWOrd"));
    }

    @Test public void unmapped() {
        assertNull(mimeMap.guessExtensionFromMimeType("test/mime"));
        assertFalse(mimeMap.hasMimeType("test/mime"));

        assertNull(mimeMap.guessMimeTypeFromExtension("test"));
        assertFalse(mimeMap.hasExtension("test"));

        verify(mimeMap, times(2)).guessExtensionFromLowerCaseMimeType("test/mime");
        verify(mimeMap, times(2)).guessMimeTypeFromLowerCaseExtension("test");
    }

    @Test public void compareAndSetDefault() {
        MimeMap otherMimeMap = mock(TestMimeMap.class);
        MimeMap defaultMimeMap = MimeMap.getDefault();
        assertTrue(MimeMap.compareAndSetDefault(defaultMimeMap, mimeMap));
        try {
            assertNotNull(defaultMimeMap);
            assertEquals(mimeMap, MimeMap.getDefault());
            assertFalse(MimeMap.compareAndSetDefault(defaultMimeMap, otherMimeMap));
        } finally {
            assertTrue(MimeMap.compareAndSetDefault(mimeMap, defaultMimeMap));
        }
    }

    @Test public void compareAndSetDefault_null() {
        MimeMap defaultMimeMap = MimeMap.getDefault();
        try {
            MimeMap.compareAndSetDefault(defaultMimeMap, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MimeMap.compareAndSetDefault(null, defaultMimeMap);
            fail();
        } catch (NullPointerException expected) {
        }

        // For comparison, this does not throw (but has no effect):
        MimeMap.compareAndSetDefault(defaultMimeMap, defaultMimeMap);
        assertEquals(defaultMimeMap, MimeMap.getDefault());
    }

    @Test public void defaultMap_15715370() {
        assertEquals("audio/flac", defaultMimeMap.guessMimeTypeFromExtension("flac"));
        assertEquals("flac", defaultMimeMap.guessExtensionFromMimeType("audio/flac"));
        assertEquals("flac", defaultMimeMap.guessExtensionFromMimeType("application/x-flac"));
    }

    // https://code.google.com/p/android/issues/detail?id=78909
    @Test public void defaultMap_78909() {
        assertEquals("mka", defaultMimeMap.guessExtensionFromMimeType("audio/x-matroska"));
        assertEquals("mkv", defaultMimeMap.guessExtensionFromMimeType("video/x-matroska"));
    }

    @Test public void defaultMap_16978217() {
        assertEquals("image/x-ms-bmp", defaultMimeMap.guessMimeTypeFromExtension("bmp"));
        assertEquals("image/x-icon", defaultMimeMap.guessMimeTypeFromExtension("ico"));
        assertEquals("video/mp2ts", defaultMimeMap.guessMimeTypeFromExtension("ts"));
    }

    @Test public void testCommon() {
        assertEquals("audio/mpeg", defaultMimeMap.guessMimeTypeFromExtension("mp3"));
        assertEquals("image/png", defaultMimeMap.guessMimeTypeFromExtension("png"));
        assertEquals("application/zip", defaultMimeMap.guessMimeTypeFromExtension("zip"));

        assertEquals("mp3", defaultMimeMap.guessExtensionFromMimeType("audio/mpeg"));
        assertEquals("png", defaultMimeMap.guessExtensionFromMimeType("image/png"));
        assertEquals("zip", defaultMimeMap.guessExtensionFromMimeType("application/zip"));
    }

    @Test public void defaultMap_18390752() {
        assertEquals("jpg", defaultMimeMap.guessExtensionFromMimeType("image/jpeg"));
    }

    @Test public void defaultMap_30207891() {
        assertTrue(defaultMimeMap.hasMimeType("IMAGE/PNG"));
        assertTrue(defaultMimeMap.hasMimeType("IMAGE/png"));
        assertFalse(defaultMimeMap.hasMimeType(""));
        assertEquals("png", defaultMimeMap.guessExtensionFromMimeType("IMAGE/PNG"));
        assertEquals("png", defaultMimeMap.guessExtensionFromMimeType("IMAGE/png"));
        assertNull(defaultMimeMap.guessMimeTypeFromExtension(""));
        assertNull(defaultMimeMap.guessMimeTypeFromExtension("doesnotexist"));
        assertTrue(defaultMimeMap.hasExtension("PNG"));
        assertTrue(defaultMimeMap.hasExtension("PnG"));
        assertFalse(defaultMimeMap.hasExtension(""));
        assertFalse(defaultMimeMap.hasExtension(".png"));
        assertEquals("image/png", defaultMimeMap.guessMimeTypeFromExtension("PNG"));
        assertEquals("image/png", defaultMimeMap.guessMimeTypeFromExtension("PnG"));
        assertNull(defaultMimeMap.guessMimeTypeFromExtension(".png"));
        assertNull(defaultMimeMap.guessMimeTypeFromExtension(""));
        assertNull(defaultMimeMap.guessExtensionFromMimeType("doesnotexist"));
    }

    @Test public void defaultMap_30793548() {
        assertEquals("video/3gpp", defaultMimeMap.guessMimeTypeFromExtension("3gpp"));
        assertEquals("video/3gpp", defaultMimeMap.guessMimeTypeFromExtension("3gp"));
        assertEquals("video/3gpp2", defaultMimeMap.guessMimeTypeFromExtension("3gpp2"));
        assertEquals("video/3gpp2", defaultMimeMap.guessMimeTypeFromExtension("3g2"));
    }

    @Test public void defaultMap_37167977() {
        // https://tools.ietf.org/html/rfc5334#section-10.1
        assertEquals("audio/ogg", defaultMimeMap.guessMimeTypeFromExtension("ogg"));
        assertEquals("audio/ogg", defaultMimeMap.guessMimeTypeFromExtension("oga"));
        assertEquals("audio/ogg", defaultMimeMap.guessMimeTypeFromExtension("spx"));
        assertEquals("video/ogg", defaultMimeMap.guessMimeTypeFromExtension("ogv"));
    }

    @Test public void defaultMap_70851634_mimeTypeFromExtension() {
        assertEquals("video/vnd.youtube.yt", defaultMimeMap.guessMimeTypeFromExtension("yt"));
    }

    @Test public void defaultMap_70851634_extensionFromMimeType() {
        assertEquals("yt", defaultMimeMap.guessExtensionFromMimeType("video/vnd.youtube.yt"));
        assertEquals("yt", defaultMimeMap.guessExtensionFromMimeType("application/vnd.youtube.yt"));
    }

    @Test public void defaultMap_112162449_audio() {
        // According to https://en.wikipedia.org/wiki/M3U#Internet_media_types
        // this is a giant mess, so we pick "audio/x-mpegurl" because a similar
        // playlist format uses "audio/x-scpls".
        assertMimeTypeFromExtension("audio/x-mpegurl", "m3u");
        assertMimeTypeFromExtension("audio/x-mpegurl", "m3u8");
        assertExtensionFromMimeType("m3u", "audio/x-mpegurl");

        assertExtensionFromMimeType("m4a", "audio/mp4");
        assertMimeTypeFromExtension("audio/mpeg", "m4a");

        assertBidirectional("audio/aac", "aac");
    }

    @Test public void defaultMap_112162449_video() {
        assertBidirectional("video/x-flv", "flv");
        assertBidirectional("video/quicktime", "mov");
        assertBidirectional("video/mpeg", "mpeg");
    }

    @Test public void defaultMap_112162449_image() {
        assertBidirectional("image/heif", "heif");
        assertBidirectional("image/heif-sequence", "heifs");
        assertBidirectional("image/heic", "heic");
        assertBidirectional("image/heic-sequence", "heics");
        assertMimeTypeFromExtension("image/heif", "hif");

        assertBidirectional("image/x-adobe-dng", "dng");
        assertBidirectional("image/x-photoshop", "psd");

        assertBidirectional("image/jp2", "jp2");
        assertMimeTypeFromExtension("image/jp2", "jpg2");
    }

    @Test public void defaultMap_120135571_audio() {
        assertMimeTypeFromExtension("audio/mpeg", "m4r");
    }

    // http://b/122734564
    @Test public void defaultMap_NonLowercaseMimeType() {
        // A mixed-case mimeType that appears in mime.types; we expect guessMimeTypeFromExtension()
        // to return it in lowercase because MimeMap considers lowercase to be the canonical form.
        String mimeType = "application/vnd.ms-word.document.macroEnabled.12".toLowerCase(Locale.US);
        assertBidirectional(mimeType, "docm");
    }

    // Check that the keys given for lookups in either direction are not case sensitive
    @Test public void defaultMap_CaseInsensitiveKeys() {
        String mimeType = defaultMimeMap.guessMimeTypeFromExtension("apk");
        assertNotNull(mimeType);

        assertEquals(mimeType, defaultMimeMap.guessMimeTypeFromExtension("APK"));
        assertEquals(mimeType, defaultMimeMap.guessMimeTypeFromExtension("aPk"));

        assertEquals("apk", defaultMimeMap.guessExtensionFromMimeType(mimeType));
        assertEquals("apk", defaultMimeMap.guessExtensionFromMimeType(
                mimeType.toUpperCase(Locale.US)));
        assertEquals("apk", defaultMimeMap.guessExtensionFromMimeType(
                mimeType.toLowerCase(Locale.US)));
    }

    @Test public void defaultMap_invalid_empty() {
        checkInvalidExtension("");
        checkInvalidMimeType("");
    }

    @Test public void defaultMap_invalid_null() {
        checkInvalidExtension(null);
        checkInvalidMimeType(null);
    }

    @Test public void defaultMap_invalid() {
        checkInvalidMimeType("invalid mime type");
        checkInvalidExtension("invalid extension");
    }

    private void checkInvalidExtension(String s) {
        assertFalse(defaultMimeMap.hasExtension(s));
        assertNull(defaultMimeMap.guessMimeTypeFromExtension(s));
    }

    private void checkInvalidMimeType(String s) {
        assertFalse(defaultMimeMap.hasMimeType(s));
        assertNull(defaultMimeMap.guessExtensionFromMimeType(s));
    }

    private void assertMimeTypeFromExtension(String mimeType, String extension) {
        final String actual = defaultMimeMap.guessMimeTypeFromExtension(extension);
        if (!Objects.equals(mimeType, actual)) {
            fail("Expected " + mimeType + " but was " + actual + " for extension " + extension);
        }
    }

    private void assertExtensionFromMimeType(String extension, String mimeType) {
        final String actual = defaultMimeMap.guessExtensionFromMimeType(mimeType);
        if (!Objects.equals(extension, actual)) {
            fail("Expected " + extension + " but was " + actual + " for type " + mimeType);
        }
    }

    private void assertBidirectional(String mimeType, String extension) {
        assertMimeTypeFromExtension(mimeType, extension);
        assertExtensionFromMimeType(extension, mimeType);
    }
}
