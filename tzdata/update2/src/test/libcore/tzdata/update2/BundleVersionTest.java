/*
 * Copyright (C) 2017 The Android Open Source Project
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

package libcore.tzdata.update2;

import junit.framework.TestCase;

public class BundleVersionTest extends TestCase {

    private static final int INVALID_VERSION_LOW = 0;
    private static final int VALID_VERSION = 23;
    private static final int INVALID_VERSION_HIGH = 1000;
    private static final String VALID_RULES_VERSION = "2016a";
    private static final String INVALID_RULES_VERSION = "A016a";

    public void testConstructorValidation() throws Exception {
        checkConstructorThrows(
                INVALID_VERSION_LOW, VALID_VERSION, VALID_RULES_VERSION, VALID_VERSION);
        checkConstructorThrows(
                INVALID_VERSION_HIGH, VALID_VERSION, VALID_RULES_VERSION, VALID_VERSION);
        checkConstructorThrows(
                VALID_VERSION, INVALID_VERSION_LOW, VALID_RULES_VERSION, VALID_VERSION);
        checkConstructorThrows(
                VALID_VERSION, INVALID_VERSION_HIGH, VALID_RULES_VERSION, VALID_VERSION);
        checkConstructorThrows(VALID_VERSION, VALID_VERSION, INVALID_RULES_VERSION, VALID_VERSION);
        checkConstructorThrows(VALID_VERSION, VALID_VERSION, VALID_RULES_VERSION,
                INVALID_VERSION_LOW);
        checkConstructorThrows(VALID_VERSION, VALID_VERSION, VALID_RULES_VERSION,
                INVALID_VERSION_HIGH);
    }

    private static void checkConstructorThrows(
            int majorVersion, int minorVersion, String rulesVersion, int revision) {
        try {
            new BundleVersion(majorVersion, minorVersion, rulesVersion, revision);
            fail();
        } catch (BundleException expected) {}
    }

    public void testConstructor() throws Exception {
        BundleVersion bundleVersion = new BundleVersion(1, 2, VALID_RULES_VERSION, 3);
        assertEquals(1, bundleVersion.formatMajorVersion);
        assertEquals(2, bundleVersion.formatMinorVersion);
        assertEquals(VALID_RULES_VERSION, bundleVersion.rulesVersion);
        assertEquals(3, bundleVersion.revision);
    }

    public void testToFromBytesRoundTrip() throws Exception {
        BundleVersion bundleVersion = new BundleVersion(1, 2, VALID_RULES_VERSION, 3);
        assertEquals(bundleVersion, BundleVersion.fromBytes(bundleVersion.toBytes()));
    }

    public void testIsCompatibleWithThisDevice() throws Exception {
        BundleVersion exactMatch = createBundleVersion(
                BundleVersion.CURRENT_FORMAT_MAJOR_VERSION,
                BundleVersion.CURRENT_FORMAT_MINOR_VERSION);
        assertTrue(BundleVersion.isCompatibleWithThisDevice(exactMatch));

        BundleVersion newerMajor = createBundleVersion(
                BundleVersion.CURRENT_FORMAT_MAJOR_VERSION + 1,
                BundleVersion.CURRENT_FORMAT_MINOR_VERSION);
        assertFalse(BundleVersion.isCompatibleWithThisDevice(newerMajor));

        BundleVersion newerMinor = createBundleVersion(
                BundleVersion.CURRENT_FORMAT_MAJOR_VERSION,
                BundleVersion.CURRENT_FORMAT_MINOR_VERSION + 1);
        assertTrue(BundleVersion.isCompatibleWithThisDevice(newerMinor));

        if (BundleVersion.CURRENT_FORMAT_MAJOR_VERSION > 1) {
            BundleVersion olderMajor = createBundleVersion(
                    BundleVersion.CURRENT_FORMAT_MAJOR_VERSION - 1,
                    BundleVersion.CURRENT_FORMAT_MINOR_VERSION);
            assertFalse(BundleVersion.isCompatibleWithThisDevice(olderMajor));
        }

        if (BundleVersion.CURRENT_FORMAT_MINOR_VERSION > 1) {
            BundleVersion olderMinor = createBundleVersion(
                    BundleVersion.CURRENT_FORMAT_MAJOR_VERSION,
                    BundleVersion.CURRENT_FORMAT_MINOR_VERSION - 1);
            assertFalse(BundleVersion.isCompatibleWithThisDevice(olderMinor));
        }
    }

    private BundleVersion createBundleVersion(int majorFormatVersion, int minorFormatVersion)
            throws BundleException {
        return new BundleVersion(majorFormatVersion, minorFormatVersion, VALID_RULES_VERSION, 3);
    }
}
