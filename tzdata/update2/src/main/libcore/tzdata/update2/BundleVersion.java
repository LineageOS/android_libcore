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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Constants and logic associated with the time zone bundle version file.
 */
public class BundleVersion {

    /**
     * The major bundle format version supported by this device.
     * Increment this for non-backwards compatible changes to the bundle format.
     */
    public static final int CURRENT_FORMAT_MAJOR_VERSION = 1;

    /**
     * The minor bundle format version supported by this device. Increment this for
     * backwards-compatible changes to the bundle format.
     */
    public static final int CURRENT_FORMAT_MINOR_VERSION = 1;

    /** The full major + minor bundle format version for this device. */
    private static final String FULL_CURRENT_FORMAT_VERSION_STRING =
            toFormatVersionString(CURRENT_FORMAT_MAJOR_VERSION, CURRENT_FORMAT_MINOR_VERSION);

    private static final int FORMAT_VERSION_STRING_LENGTH =
            FULL_CURRENT_FORMAT_VERSION_STRING.length();
    private static final Pattern FORMAT_VERSION_PATTERN = Pattern.compile("(\\d{3})\\.(\\d{3})");

    /** A pattern that matches the IANA rules value of a rules update. e.g. "2016g" */
    private static final Pattern RULES_VERSION_PATTERN = Pattern.compile("(\\d{4}\\w)");

    private static final int RULES_VERSION_LENGTH = 5;

    /** A pattern that matches the revision of a rules update. e.g. "001" */
    private static final Pattern REVISION_PATTERN = Pattern.compile("(\\d{3})");

    private static final int REVISION_LENGTH = 3;

    /**
     * The length of a well-formed bundle version file:
     * {Bundle version}|{Rule version}|{Revision}
     */
    static final int BUNDLE_VERSION_FILE_LENGTH = FORMAT_VERSION_STRING_LENGTH + 1
            + RULES_VERSION_LENGTH
            + 1 + REVISION_LENGTH;

    private static final Pattern BUNDLE_VERSION_PATTERN = Pattern.compile(
            FORMAT_VERSION_PATTERN.pattern() + "\\|"
                    + RULES_VERSION_PATTERN.pattern() + "\\|"
                    + REVISION_PATTERN.pattern()
                    + ".*" /* ignore trailing */);

    public final int formatMajorVersion;
    public final int formatMinorVersion;
    public final String rulesVersion;
    public final int revision;

    public BundleVersion(int formatMajorVersion, int formatMinorVersion, String rulesVersion,
            int revision) throws BundleException {
        this.formatMajorVersion = validate3DigitVersion(formatMajorVersion);
        this.formatMinorVersion = validate3DigitVersion(formatMinorVersion);
        if (!RULES_VERSION_PATTERN.matcher(rulesVersion).matches()) {
            throw new BundleException("Invalid rulesVersion: " + rulesVersion);
        }
        this.rulesVersion = rulesVersion;
        this.revision = validate3DigitVersion(revision);
    }

    public static BundleVersion fromBytes(byte[] bytes) throws BundleException {
        String bundleVersion = new String(bytes, StandardCharsets.US_ASCII);
        try {
            Matcher matcher = BUNDLE_VERSION_PATTERN.matcher(bundleVersion);
            if (!matcher.matches()) {
                throw new BundleException("Invalid bundle version string: " + bundleVersion);
            }
            String formatMajorVersion = matcher.group(1);
            String formatMinorVersion = matcher.group(2);
            String rulesVersion = matcher.group(3);
            String revision = matcher.group(4);
            return new BundleVersion(
                    from3DigitVersionString(formatMajorVersion),
                    from3DigitVersionString(formatMinorVersion),
                    rulesVersion,
                    from3DigitVersionString(revision));
        } catch (IndexOutOfBoundsException e) {
            // The use of the regexp above should make this impossible.
            throw new BundleException("Bundle version string too short:" + bundleVersion);
        }
    }

    public byte[] toBytes() {
        return toBytes(formatMajorVersion, formatMinorVersion, rulesVersion, revision);
    }

    // @VisibleForTesting - can be used to construct invalid bundle version bytes.
    public static byte[] toBytes(
            int majorFormatVersion, int minorFormatVerison, String rulesVersion, int revision) {
        return (toFormatVersionString(majorFormatVersion, minorFormatVerison)
                + "|" + rulesVersion + "|" + to3DigitVersionString(revision))
                .getBytes(StandardCharsets.US_ASCII);
    }

    public static boolean isCompatibleWithThisDevice(BundleVersion bundleVersion) {
        return (BundleVersion.CURRENT_FORMAT_MAJOR_VERSION
                == bundleVersion.formatMajorVersion)
                && (BundleVersion.CURRENT_FORMAT_MINOR_VERSION
                <= bundleVersion.formatMinorVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BundleVersion that = (BundleVersion) o;

        if (formatMajorVersion != that.formatMajorVersion) {
            return false;
        }
        if (formatMinorVersion != that.formatMinorVersion) {
            return false;
        }
        if (revision != that.revision) {
            return false;
        }
        return rulesVersion.equals(that.rulesVersion);
    }

    @Override
    public String toString() {
        return "BundleVersion{" +
                "formatMajorVersion=" + formatMajorVersion +
                ", formatMinorVersion=" + formatMinorVersion +
                ", rulesVersion='" + rulesVersion + '\'' +
                ", revision=" + revision +
                '}';
    }

    /**
     * Returns a version as a zero-padded three-digit String value.
     */
    private static String to3DigitVersionString(int version) {
        try {
            return String.format(Locale.ROOT, "%03d", validate3DigitVersion(version));
        } catch (BundleException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Validates and parses a zero-padded three-digit String value.
     */
    private static int from3DigitVersionString(String versionString) throws BundleException {
        final String parseErrorMessage = "versionString must be a zero padded, 3 digit, positive"
                + " decimal integer";
        if (versionString.length() != 3) {
            throw new BundleException(parseErrorMessage);
        }
        try {
            int version = Integer.parseInt(versionString);
            return validate3DigitVersion(version);
        } catch (NumberFormatException e) {
            throw new BundleException(parseErrorMessage, e);
        }
    }

    private static int validate3DigitVersion(int value) throws BundleException {
        if (value < 1 || value > 999) {
            throw new BundleException("Expected 1 <= value <= 999, was " + value);
        }
        return value;
    }

    private static String toFormatVersionString(int majorFormatVersion, int minorFormatVersion) {
        return to3DigitVersionString(majorFormatVersion)
                + "." + to3DigitVersionString(minorFormatVersion);
    }
}
