/*
 * Copyright (C) 2016 The Android Open Source Project
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

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Constants and logic associated with the timezone bundle version file.
 */
public class BundleVersion {

    /**
     * The current bundle format version in the form XXX.YYY. Increment the first number (XXX)
     * when making incompatible changes to the bundle structure, or the files contained within.
     * The second number (YYY) is currently ignored.
     */
    public static final String BUNDLE_FORMAT_MAJOR_VERSION = "001";

    public static final String FULL_BUNDLE_FORMAT_VERSION = BUNDLE_FORMAT_MAJOR_VERSION + ".001";

    private static final int BUNDLE_FORMAT_LENGTH = 7;

    public static final Pattern BUNDLE_FORMAT_VERSION_PATTERN = Pattern.compile("\\d{3}\\.\\d{3}");

    /** A pattern that matches the IANA rules value of a rules update. e.g. "2016g" */
    public static final Pattern RULES_VERSION_PATTERN = Pattern.compile("\\d{4}\\w");

    private static final int RULES_VERSION_LENGTH = 5;

    /** A pattern that matches the Android revision of a rules update. e.g. "001" */
    public static final Pattern ANDROID_REVISION_PATTERN = Pattern.compile("\\d{3}");

    private static final int ANDROID_REVISION_LENGTH = 3;

    /**
     * The length of a well-formed bundle version file:
     * {Bundle version}|{Rule version}|{Android revision}
     */
    public static final int BUNDLE_VERSION_FILE_LENGTH = BUNDLE_FORMAT_LENGTH + 1
            + RULES_VERSION_LENGTH
            + 1 + ANDROID_REVISION_LENGTH;

    private static final Pattern BUNDLE_VERSION_PATTERN = Pattern.compile(
            BUNDLE_FORMAT_VERSION_PATTERN.pattern() + "\\|"
                    + RULES_VERSION_PATTERN.pattern() + "\\|"
                    + ANDROID_REVISION_PATTERN.pattern()
                    + ".*" /* ignore trailing */);

    private final String bundleFormatVersion;

    public final String rulesVersion;

    public final String androidRevision;

    public BundleVersion(String bundleFormatVersion, String rulesVersion,
            String androidRevision) throws BundleException {
        if (!BUNDLE_FORMAT_VERSION_PATTERN.matcher(bundleFormatVersion).matches()) {
            throw new BundleException("Invalid bundleFormatVersion: " + bundleFormatVersion);
        }
        if (!RULES_VERSION_PATTERN.matcher(rulesVersion).matches()) {
            throw new BundleException("Invalid rulesVersion: " + rulesVersion);
        }
        if (!ANDROID_REVISION_PATTERN.matcher(androidRevision).matches()) {
            throw new BundleException("Invalid androidRevision: " + androidRevision);
        }
        this.bundleFormatVersion = bundleFormatVersion;
        this.rulesVersion = rulesVersion;
        this.androidRevision = androidRevision;
    }

    public static BundleVersion extractFromBytes(byte[] bytes) throws BundleException {
        try {
            String bundleVersion = new String(bytes, StandardCharsets.US_ASCII);
            if (!BUNDLE_VERSION_PATTERN.matcher(bundleVersion).matches()) {
                throw new BundleException("Invalid bundle version string: " + bundleVersion);
            }
            String bundleFormatVersion = bundleVersion.substring(0, 7);
            String rulesVersion = bundleVersion.substring(8, 13);
            String androidRevision = bundleVersion.substring(14);
            return new BundleVersion(bundleFormatVersion, rulesVersion, androidRevision);
        } catch (IndexOutOfBoundsException e) {
            throw new BundleException("Data too short");
        }
    }

    public String getBundleFormatMajorVersion() {
        return bundleFormatVersion.substring(0, 3);
    }

    @Override
    public String toString() {
        return "BundleVersion{" +
                "bundleFormatVersion='" + bundleFormatVersion + '\'' +
                ", rulesVersion='" + rulesVersion + '\'' +
                ", androidRevision='" + androidRevision + '\'' +
                '}';
    }

    public byte[] getBytes() {
        return getBytes(bundleFormatVersion, rulesVersion, androidRevision);
    }

    // @VisibleForTesting - can be used to construct invalid bundle version bytes.
    public static byte[] getBytes(
            String bundleFormatVersion, String rulesVersion, String androidRevision) {
        return (bundleFormatVersion + "|" + rulesVersion + "|" + androidRevision)
                .getBytes(StandardCharsets.US_ASCII);
    }
}
