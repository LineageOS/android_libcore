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


    @Before public void setUp() {
        mimeMap = mock(TestMimeMap.class);
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

}
