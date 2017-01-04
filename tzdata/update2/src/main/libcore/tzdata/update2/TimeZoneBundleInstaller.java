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
package libcore.tzdata.update2;

import android.util.Slog;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import libcore.util.ZoneInfoDB;

/**
 * A bundle-validation / extraction class. Separate from the services code that uses it for easier
 * testing.
 */
public final class TimeZoneBundleInstaller {

    private static final String CURRENT_TZ_DATA_DIR_NAME = "current";
    private static final String WORKING_DIR_NAME = "working";
    private static final String OLD_TZ_DATA_DIR_NAME = "old";

    private final String logTag;
    private final File systemTzDataFile;
    private final File oldTzDataDir;
    private final File currentTzDataDir;
    private final File workingDir;

    public TimeZoneBundleInstaller(String logTag, File systemTzDataFile, File installDir) {
        this.logTag = logTag;
        this.systemTzDataFile = systemTzDataFile;
        oldTzDataDir = new File(installDir, OLD_TZ_DATA_DIR_NAME);
        currentTzDataDir = new File(installDir, CURRENT_TZ_DATA_DIR_NAME);
        workingDir = new File(installDir, WORKING_DIR_NAME);
    }

    // VisibleForTesting
    File getOldTzDataDir() {
        return oldTzDataDir;
    }

    // VisibleForTesting
    File getCurrentTzDataDir() {
        return currentTzDataDir;
    }

    // VisibleForTesting
    File getWorkingDir() {
        return workingDir;
    }

    /**
     * Install the supplied content.
     *
     * <p>Errors during unpacking or installation will throw an {@link IOException}.
     * If the content is invalid this method returns {@code false}.
     * If the installation completed successfully this method returns {@code true}.
     */
    public boolean install(byte[] content) throws IOException {
        if (oldTzDataDir.exists()) {
            FileUtils.deleteRecursive(oldTzDataDir);
        }
        if (workingDir.exists()) {
            FileUtils.deleteRecursive(workingDir);
        }

        Slog.i(logTag, "Unpacking / verifying time zone update");
        unpackBundle(content, workingDir);
        try {
            if (!checkBundleFilesExist(workingDir)) {
                Slog.i(logTag, "Update not applied: Bundle is missing files");
                return false;
            }

            BundleVersion bundleVersion = readBundleVersion(workingDir);
            if (bundleVersion == null) {
                Slog.i(logTag, "Update not applied: Bundle version could not be loaded");
                return false;
            }
            if (!checkBundleFormatVersion(bundleVersion)) {
                Slog.i(logTag, "Update not applied: Bundle format version check failed: "
                        + bundleVersion);
                return false;
            }

            if (!checkBundleRulesNewerThanSystem(systemTzDataFile, bundleVersion)) {
                Slog.i(logTag, "Update not applied: Bundle rules version check failed");
                return false;
            }

            File zoneInfoFile = new File(workingDir, TimeZoneBundle.TZDATA_FILE_NAME);
            ZoneInfoDB.TzData tzData = ZoneInfoDB.TzData.loadTzData(zoneInfoFile.getPath());
            if (tzData == null) {
                Slog.i(logTag, "Update not applied: " + zoneInfoFile + " could not be loaded");
                return false;
            }
            try {
                tzData.validate();
            } catch (IOException e) {
                Slog.i(logTag, "Update not applied: " + zoneInfoFile + " failed validation", e);
                return false;
            } finally {
                tzData.close();
            }
            // TODO(nfuller): Add deeper validity checks / canarying before applying.
            // http://b/31008728

            Slog.i(logTag, "Applying time zone update");
            FileUtils.makeDirectoryWorldAccessible(workingDir);

            if (currentTzDataDir.exists()) {
                Slog.i(logTag, "Moving " + currentTzDataDir + " to " + oldTzDataDir);
                FileUtils.rename(currentTzDataDir, oldTzDataDir);
            }
            Slog.i(logTag, "Moving " + workingDir + " to " + currentTzDataDir);
            FileUtils.rename(workingDir, currentTzDataDir);
            Slog.i(logTag, "Update applied: " + currentTzDataDir + " successfully created");
            return true;
        } finally {
            deleteBestEffort(oldTzDataDir);
            deleteBestEffort(workingDir);
        }
    }

    /**
     * Uninstall the current timezone update in /data, returning the device to using data from
     * /system. Returns {@code true} if uninstallation was successful, {@code false} if there was
     * nothing installed in /data to uninstall.
     *
     * <p>Errors encountered during uninstallation will throw an {@link IOException}.
     */
    public boolean uninstall() throws IOException {
        Slog.i(logTag, "Uninstalling time zone update");

        // Make sure we don't have a dir where we're going to move the currently installed data to.
        if (oldTzDataDir.exists()) {
            // If we can't remove this, an exception is thrown and we don't continue.
            FileUtils.deleteRecursive(oldTzDataDir);
        }

        if (!currentTzDataDir.exists()) {
            Slog.i(logTag, "Nothing to uninstall at " + currentTzDataDir);
            return false;
        }

        try {
            Slog.i(logTag, "Moving " + currentTzDataDir + " to " + oldTzDataDir);
            // Move currentTzDataDir out of the way in one operation so we can't partially delete
            // the contents, which would leave a partial install.
            FileUtils.rename(currentTzDataDir, oldTzDataDir);
            return true;
        } finally {
            // Do our best to delete the now uninstalled timezone data.
            deleteBestEffort(oldTzDataDir);
        }
    }

    private void deleteBestEffort(File dir) {
        if (dir.exists()) {
            try {
                FileUtils.deleteRecursive(dir);
            } catch (IOException e) {
                // Logged but otherwise ignored.
                Slog.w(logTag, "Unable to delete " + dir, e);
            }
        }
    }

    private void unpackBundle(byte[] content, File targetDir) throws IOException {
        Slog.i(logTag, "Unpacking update content to: " + targetDir);
        TimeZoneBundle bundle = new TimeZoneBundle(content);
        bundle.extractTo(targetDir);
    }

    private boolean checkBundleFilesExist(File unpackedContentDir) throws IOException {
        Slog.i(logTag, "Verifying bundle contents");
        return FileUtils.filesExist(unpackedContentDir,
                TimeZoneBundle.BUNDLE_VERSION_FILE_NAME,
                TimeZoneBundle.TZDATA_FILE_NAME,
                TimeZoneBundle.ICU_DATA_FILE_NAME);
    }

    private BundleVersion readBundleVersion(File unpackedContentDir) throws IOException {
        Slog.i(logTag, "Reading bundle format version");
        File bundleVersionFile =
                new File(unpackedContentDir, TimeZoneBundle.BUNDLE_VERSION_FILE_NAME);
        byte[] versionBytes =
                FileUtils.readBytes(bundleVersionFile, BundleVersion.BUNDLE_VERSION_FILE_LENGTH);
        try {
            return BundleVersion.extractFromBytes(versionBytes);
        } catch (BundleException e) {
            Slog.i(logTag, "Invalid bundle version bytes: " + Arrays.toString(versionBytes)
                    + ": " + e.getMessage());
            return null;
        }
    }

    private boolean checkBundleFormatVersion(BundleVersion bundleVersion) {
        return bundleVersion.getBundleFormatMajorVersion()
                .equals(BundleVersion.BUNDLE_FORMAT_MAJOR_VERSION);
    }

    /**
     * Returns true if the the bundle IANA rules version is >= system IANA rules version.
     */
    private boolean checkBundleRulesNewerThanSystem(
            File systemTzDataFile, BundleVersion bundleVersion) throws IOException {

        // We only check the /system tzdata file and assume that other data like ICU is in sync.
        // There is a CTS test that checks ICU and bionic/libcore are in sync.
        Slog.i(logTag, "Reading /system rules version");
        if (!systemTzDataFile.exists()) {
            Slog.i(logTag, "tzdata file cannot be found in /system");
            return false;
        }
        String systemRulesVersion = ZoneInfoDB.TzData.getRulesVersion(systemTzDataFile);

        String bundleRulesVersion = bundleVersion.rulesVersion;
        // canApply = bundleRulesVersion >= systemRulesVersion
        boolean canApply = bundleRulesVersion.compareTo(systemRulesVersion) >= 0;
        if (!canApply) {
            Slog.i(logTag, "Failed rules version check: bundleRulesVersion="
                    + bundleRulesVersion + ", systemRulesVersion=" + systemRulesVersion);
        }
        return canApply;
    }
}
