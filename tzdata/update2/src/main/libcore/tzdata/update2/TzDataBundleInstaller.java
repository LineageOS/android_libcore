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

/**
 * A bundle-validation / extraction class. Separate from the services code that uses it for easier
 * testing.
 */
public final class TzDataBundleInstaller {

    static final String CURRENT_TZ_DATA_DIR_NAME = "current";
    static final String WORKING_DIR_NAME = "working";
    static final String OLD_TZ_DATA_DIR_NAME = "old";

    private final String logTag;
    private final File installDir;

    public TzDataBundleInstaller(String logTag, File installDir) {
        this.logTag = logTag;
        this.installDir = installDir;
    }

    /**
     * Install the supplied content.
     *
     * <p>Errors during unpacking or installation will throw an {@link IOException}.
     * If the content is invalid this method returns {@code false}.
     * If the installation completed successfully this method returns {@code true}.
     */
    public boolean install(byte[] content) throws IOException {
        File oldTzDataDir = new File(installDir, OLD_TZ_DATA_DIR_NAME);
        if (oldTzDataDir.exists()) {
            FileUtils.deleteRecursive(oldTzDataDir);
        }

        File currentTzDataDir = new File(installDir, CURRENT_TZ_DATA_DIR_NAME);
        File workingDir = new File(installDir, WORKING_DIR_NAME);

        Slog.i(logTag, "Unpacking / verifying time zone update");
        File unpackedContentDir = unpackBundle(content, workingDir);
        try {
            if (!checkBundleVersion(unpackedContentDir)) {
                Slog.i(logTag, "Update not applied: Bundle format version is incorrect.");
                return false;
            }
            // This check should not fail if the bundle version check passes, but we're being
            // intentionally paranoid.
            if (!checkBundleFilesExist(unpackedContentDir)) {
                Slog.i(logTag, "Update not applied: Bundle is missing files");
                return false;
            }

            // TODO(nfuller): Add IANA version check. http://b/31008728

            // TODO(nfuller): Add deeper validity checks / canarying before applying.
            // http://b/31008728

            Slog.i(logTag, "Applying time zone update");
            FileUtils.makeDirectoryWorldAccessible(unpackedContentDir);

            if (currentTzDataDir.exists()) {
                Slog.i(logTag, "Moving " + currentTzDataDir + " to " + oldTzDataDir);
                FileUtils.rename(currentTzDataDir, oldTzDataDir);
            }
            Slog.i(logTag, "Moving " + unpackedContentDir + " to " + currentTzDataDir);
            FileUtils.rename(unpackedContentDir, currentTzDataDir);
            Slog.i(logTag, "Update applied: " + currentTzDataDir + " successfully created");
            return true;
        } finally {
            deleteBestEffort(oldTzDataDir);
            deleteBestEffort(unpackedContentDir);
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

    private File unpackBundle(byte[] content, File targetDir) throws IOException {
        Slog.i(logTag, "Unpacking update content to: " + targetDir);
        ConfigBundle bundle = new ConfigBundle(content);
        bundle.extractTo(targetDir);
        return targetDir;
    }

    private boolean checkBundleFilesExist(File unpackedContentDir) throws IOException {
        Slog.i(logTag, "Verifying bundle contents");
        return FileUtils.filesExist(unpackedContentDir,
                ConfigBundle.TZ_DATA_VERSION_FILE_NAME,
                ConfigBundle.ZONEINFO_FILE_NAME,
                ConfigBundle.ICU_DATA_FILE_NAME);
    }

    private boolean checkBundleVersion(File unpackedContentDir) throws IOException {
        Slog.i(logTag, "Verifying bundle format version");
        if (!FileUtils.filesExist(unpackedContentDir, ConfigBundle.BUNDLE_VERSION_FILE_NAME)) {
            Slog.i(logTag, "Bundle format version file does not exist.");
            return false;
        }

        File bundleVersionFile =
                new File(unpackedContentDir, ConfigBundle.BUNDLE_VERSION_FILE_NAME);
        byte[] versionBytes = FileUtils.readBytes(bundleVersionFile, 10);
        if (!Arrays.equals(versionBytes, ConfigBundle.BUNDLE_VERSION_BYTES)) {
            Slog.i(logTag, "Incompatible bundle format version: " + Arrays.toString(versionBytes));
            return false;
        }
        return true;
    }
}
