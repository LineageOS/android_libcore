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

import libcore.net.MimeMap;
import libcore.util.NonNull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test public void setDefault() {
        MimeMap defaultMimeMap = MimeMap.getDefault();
        MimeMap otherMimeMap = mock(TestMimeMap.class);
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

}
