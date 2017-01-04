/*
 * Copyright (C) 2015 The Android Open Source Project
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
package libcore.tzdata.update2.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import libcore.tzdata.update2.BundleException;
import libcore.tzdata.update2.BundleVersion;
import libcore.tzdata.update2.TimeZoneBundle;

/**
 * A class for creating a {@link TimeZoneBundle} containing timezone update data. Used in real
 * bundle creation code and tests.
 */
//TODO(nfuller): Rename to TimeZoneBundleBuilder.
public final class TzDataBundleBuilder {

    private String bundleFormatVersion = BundleVersion.FULL_BUNDLE_FORMAT_VERSION;
    private String rulesVersion;
    private String androidRevision;
    private byte[] tzData;
    private byte[] icuData;

    // For use in tests.
    public TzDataBundleBuilder setBundleVersionForTests(String bundleVersion) {
        this.bundleFormatVersion = bundleVersion;
        return this;
    }

    public TzDataBundleBuilder setRulesVersion(String rulesVersion) {
        this.rulesVersion = rulesVersion;
        return this;
    }

    public TzDataBundleBuilder setAndroidRevision(String androidRevision) {
        this.androidRevision = androidRevision;
        return this;
    }

    public TzDataBundleBuilder clearVersionForTests() {
        // This has the effect of omitting the version file in buildUnvalidated().
        this.bundleFormatVersion = null;
        return this;
    }

    public TzDataBundleBuilder setTzData(File tzDataFile) throws IOException {
        return setTzData(readFileAsByteArray(tzDataFile));
    }

    public TzDataBundleBuilder setTzData(byte[] tzData) {
        this.tzData = tzData;
        return this;
    }

    // For use in tests.
    public TzDataBundleBuilder clearTzDataForTests() {
        this.tzData = null;
        return this;
    }

    public TzDataBundleBuilder setIcuData(File icuDataFile) throws IOException {
        return setIcuData(readFileAsByteArray(icuDataFile));
    }

    public TzDataBundleBuilder setIcuData(byte[] icuData) {
        this.icuData = icuData;
        return this;
    }

    // For use in tests.
    public TzDataBundleBuilder clearIcuDataForTests() {
        this.icuData = null;
        return this;
    }

    /**
     * For use in tests. Use {@link #build()}.
     */
    public TimeZoneBundle buildUnvalidated() throws BundleException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            if (bundleFormatVersion != null && rulesVersion != null && androidRevision != null) {
                BundleVersion bundleVersion =
                        new BundleVersion(bundleFormatVersion, rulesVersion, androidRevision);
                addZipEntry(zos, TimeZoneBundle.BUNDLE_VERSION_FILE_NAME, bundleVersion.getBytes());
            }

            if (tzData != null) {
                addZipEntry(zos, TimeZoneBundle.TZDATA_FILE_NAME, tzData);
            }
            if (icuData != null) {
                addZipEntry(zos, TimeZoneBundle.ICU_DATA_FILE_NAME, icuData);
            }
        } catch (IOException e) {
            throw new BundleException("Unable to create zip file", e);
        }
        return new TimeZoneBundle(baos.toByteArray());
    }

    /**
     * Builds a {@link TimeZoneBundle}.
     */
    public TimeZoneBundle build() throws BundleException {
        if (bundleFormatVersion == null) {
            throw new IllegalStateException("Missing bundleVersion");
        }
        if (!BundleVersion.BUNDLE_FORMAT_VERSION_PATTERN.matcher(bundleFormatVersion).matches()) {
            throw new IllegalStateException("bundleVersion invalid: " + bundleFormatVersion);
        }

        if (rulesVersion == null) {
            throw new IllegalStateException("Missing rulesVersion");
        }
        if (!BundleVersion.RULES_VERSION_PATTERN.matcher(rulesVersion).matches()) {
            throw new IllegalStateException("rulesVersion invalid: " + rulesVersion);
        }

        if (androidRevision == null) {
            throw new IllegalStateException("Missing androidRevision");
        }
        if (!BundleVersion.ANDROID_REVISION_PATTERN.matcher(androidRevision).matches()) {
            throw new IllegalStateException("androidRevision invalid: " + androidRevision);
        }
        if (icuData == null) {
            throw new IllegalStateException("Missing icuData");
        }
        if (tzData == null) {
            throw new IllegalStateException("Missing tzData");
        }
        return buildUnvalidated();
    }

    private static void addZipEntry(ZipOutputStream zos, String name, byte[] content)
            throws BundleException {
        try {
            ZipEntry zipEntry = new ZipEntry(name);
            zipEntry.setSize(content.length);
            zos.putNextEntry(zipEntry);
            zos.write(content);
            zos.closeEntry();
        } catch (IOException e) {
            throw new BundleException("Unable to add zip entry", e);
        }
    }

    /**
     * Returns the contents of 'path' as a byte array.
     */
    public static byte[] readFileAsByteArray(File file) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream  fis = new FileInputStream(file)) {
            int count;
            while ((count = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
            }
        }
        return baos.toByteArray();
    }
}

