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

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import libcore.io.Streams;
import libcore.tzdata.testing.ZoneInfoTestHelper;
import libcore.tzdata.update2.tools.TzDataBundleBuilder;

/**
 * Tests for {@link TzDataBundleInstaller}.
 */
public class TzDataBundleInstallerTest extends TestCase {

    // OLDER_RULES_VERSION < SYSTEM_RULES_VERSION < NEW_RULES_VERSION < NEWER_RULES_VERSION
    private static final String OLDER_RULES_VERSION = "2030a";
    private static final String SYSTEM_RULES_VERSION = "2030b";
    private static final String NEW_RULES_VERSION = "2030c";
    private static final String NEWER_RULES_VERSION = "2030d";

    private TzDataBundleInstaller installer;
    private File tempDir;
    private File testInstallDir;
    private File testSystemTzDataDir;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        tempDir = createDirectory("tempDir");
        testInstallDir =  createDirectory("testInstall");
        testSystemTzDataDir =  createDirectory("testSystemTzData");

        // Create a file to represent the tzdata file in the /system partition of the device.
        File testSystemTzDataFile = new File(testSystemTzDataDir, "tzdata");
        createTzDataFile(testSystemTzDataFile, SYSTEM_RULES_VERSION);

        installer = new TzDataBundleInstaller(
                "TzDataBundleInstallerTest", testSystemTzDataFile, testInstallDir);
    }

    private static File createDirectory(String prefix) throws IOException {
        File dir = File.createTempFile(prefix, "");
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

    @Override
    public void tearDown() throws Exception {
        if (testSystemTzDataDir.exists()) {
            FileUtils.deleteRecursive(testInstallDir);
        }
        if (testInstallDir.exists()) {
            FileUtils.deleteRecursive(testInstallDir);
        }
        if (tempDir.exists()) {
            FileUtils.deleteRecursive(tempDir);
        }
        super.tearDown();
    }

    /** Tests the an update on a device will fail if the /system tzdata file cannot be found. */
    public void testBadSystemFile() throws Exception {
        File doesNotExist = new File(testSystemTzDataDir, "doesNotExist");
        TzDataBundleInstaller brokenSystemInstaller = new TzDataBundleInstaller(
                "TzDataBundleInstallerTest", doesNotExist, testInstallDir);
        ConfigBundle tzData = createValidTzDataBundle(NEW_RULES_VERSION);

        assertFalse(brokenSystemInstaller.install(tzData.getBundleBytes()));
        assertNoContentInstalled();
    }

    /** Tests the first successful update on a device */
    public void testSuccessfulFirstUpdate() throws Exception {
        ConfigBundle tzData = createValidTzDataBundle(NEW_RULES_VERSION);

        assertTrue(installer.install(tzData.getBundleBytes()));
        assertTzDataInstalled(tzData);
    }

    /**
     * Tests we can install an update the same version as is in /system.
     */
    public void testSuccessfulFirstUpdate_sameVersionAsSystem() throws Exception {
        ConfigBundle tzData1 = createValidTzDataBundle(SYSTEM_RULES_VERSION);
        assertTrue(installer.install(tzData1.getBundleBytes()));
        assertTzDataInstalled(tzData1);
    }

    /**
     * Tests we cannot install an update older than the version in /system.
     */
    public void testUnsuccessfulFirstUpdate_olderVersionThanSystem() throws Exception {
        ConfigBundle tzData1 = createValidTzDataBundle(OLDER_RULES_VERSION);
        assertFalse(installer.install(tzData1.getBundleBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests an update on a device when there is a prior update already applied.
     */
    public void testSuccessfulFollowOnUpdate_newerVersion() throws Exception {
        ConfigBundle tzData1 = createValidTzDataBundle(NEW_RULES_VERSION);
        assertTrue(installer.install(tzData1.getBundleBytes()));
        assertTzDataInstalled(tzData1);

        ConfigBundle tzData2 = createValidTzDataBundle(NEWER_RULES_VERSION);
        assertTrue(installer.install(tzData2.getBundleBytes()));
        assertTzDataInstalled(tzData2);
    }

    /**
     * Tests an update on a device when there is a prior update already applied, but the follow
     * on update is older than in /system.
     */
    public void testUnsuccessfulFollowOnUpdate_olderVersion() throws Exception {
        ConfigBundle tzData1 = createValidTzDataBundle(NEW_RULES_VERSION);
        assertTrue(installer.install(tzData1.getBundleBytes()));
        assertTzDataInstalled(tzData1);

        ConfigBundle tzData2 = createValidTzDataBundle(OLDER_RULES_VERSION);
        assertFalse(installer.install(tzData2.getBundleBytes()));
        assertTzDataInstalled(tzData1);
    }

    /** Tests that a bundle with a missing file will not update the content. */
    public void testMissingRequiredBundleFile() throws Exception {
        ConfigBundle installedConfigBundle = createValidTzDataBundle(NEW_RULES_VERSION);
        assertTrue(installer.install(installedConfigBundle.getBundleBytes()));
        assertTzDataInstalled(installedConfigBundle);

        ConfigBundle incompleteUpdate =
                createValidTzDataBundleBuilder(NEWER_RULES_VERSION)
                        .clearBionicTzData()
                        .buildUnvalidated();
        assertFalse(installer.install(incompleteUpdate.getBundleBytes()));
        assertTzDataInstalled(installedConfigBundle);
    }

    /**
     * Tests that an update will be unpacked even if there is a partial update from a previous run.
     */
    public void testInstallWithWorkingDir() throws Exception {
        File workingDir = installer.getWorkingDir();
        assertTrue(workingDir.mkdir());
        createFile(new File(workingDir, "myFile"));

        ConfigBundle tzData = createValidTzDataBundle(NEW_RULES_VERSION);
        assertTrue(installer.install(tzData.getBundleBytes()));
        assertTzDataInstalled(tzData);
    }

    /**
     * Tests that a bundle without a bundle version file will be rejected.
     */
    public void testInstallWithMissingBundleVersionFile() throws Exception {
        File workingDir = installer.getWorkingDir();
        assertTrue(workingDir.mkdir());

        ConfigBundle tzData = createTzDataBundleWithoutFormatVersionFile(NEW_RULES_VERSION);
        assertFalse(installer.install(tzData.getBundleBytes()));
        assertNoContentInstalled();
    }

    private ConfigBundle createTzDataBundleWithoutFormatVersionFile(String tzDataVersion)
            throws IOException {

        // Create a valid bundle.
        ConfigBundle bundle = createValidTzDataBundle(tzDataVersion);
        byte[] bundleBytes = bundle.getBundleBytes();

        // Remove the version file to make the bundle invalid.
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bundleBytes.length);
        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new ByteArrayInputStream(bundleBytes));
             ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.getName().equals(ConfigBundle.BUNDLE_VERSION_FILE_NAME)) {
                    zipOutputStream.putNextEntry(entry);
                    Streams.copy(zipInputStream, zipOutputStream);
                    zipOutputStream.closeEntry();
                }
                zipInputStream.closeEntry();
            }
        }
        return new ConfigBundle(baos.toByteArray());
    }

    /**
     * Tests that a bundle with an incorrect bundle version will be rejected.
     */
    public void testInstallWithInvalidBundleVersionFile() throws Exception {
        ConfigBundle tzData = createTzDataBundleWithInvalidBundleVersion(NEW_RULES_VERSION);
        assertFalse(installer.install(tzData.getBundleBytes()));
        assertNoContentInstalled();
    }

    public void testUninstall_noExistingDataBundle() throws Exception {
        assertFalse(installer.uninstall());
        assertNoContentInstalled();
    }

    public void testUninstall_existingDataBundle() throws Exception {
        File currentDataDir = installer.getCurrentTzDataDir();
        assertTrue(currentDataDir.mkdir());

        assertTrue(installer.uninstall());
        assertNoContentInstalled();
    }

    public void testUninstall_oldDirsAlreadyExists() throws Exception {
        File oldTzDataDir = installer.getOldTzDataDir();
        assertTrue(oldTzDataDir.mkdir());

        File currentDataDir = installer.getCurrentTzDataDir();
        assertTrue(currentDataDir.mkdir());

        assertTrue(installer.uninstall());
        assertNoContentInstalled();
    }

    private ConfigBundle createTzDataBundleWithInvalidBundleVersion(String tzDataVersion)
            throws IOException {

        // Create a valid bundle.
        ConfigBundle bundle = createValidTzDataBundle(tzDataVersion);
        byte[] bundleBytes = bundle.getBundleBytes();

        // Modify the bundle version file to be invalid.
        byte[] badVersionBytes = new byte[ConfigBundle.BUNDLE_VERSION_BYTES.length];
        System.arraycopy(ConfigBundle.BUNDLE_VERSION_BYTES, 0, badVersionBytes, 0,
                badVersionBytes.length);
        badVersionBytes[0]++;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(bundleBytes.length);
        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new ByteArrayInputStream(bundleBytes));
             ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                zipOutputStream.putNextEntry(entry);
                if (entry.getName().equals(ConfigBundle.BUNDLE_VERSION_FILE_NAME)) {
                    zipOutputStream.write(badVersionBytes);
                } else {
                    Streams.copy(zipInputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
                zipInputStream.closeEntry();
            }
        }
        return new ConfigBundle(baos.toByteArray());
    }

    private ConfigBundle createValidTzDataBundle(String tzDataVersion)
            throws IOException {
        return createValidTzDataBundleBuilder(tzDataVersion).build();
    }

    private TzDataBundleBuilder createValidTzDataBundleBuilder(String tzDataVersion)
            throws IOException {

        File bionicTzData = new File(tempDir, "zoneinfo");
        createTzDataFile(bionicTzData, tzDataVersion);

        File icuData = new File(tempDir, "icudata");
        createFile(icuData);

        return new TzDataBundleBuilder()
                .setTzDataVersion(tzDataVersion)
                .addBionicTzData(bionicTzData)
                .addIcuTzData(icuData);
    }

    private void assertTzDataInstalled(ConfigBundle expectedTzData) throws Exception {
        assertTrue(testInstallDir.exists());

        File currentTzDataDir = installer.getCurrentTzDataDir();
        assertTrue(currentTzDataDir.exists());

        File bundleVersionFile = new File(currentTzDataDir,
                ConfigBundle.BUNDLE_VERSION_FILE_NAME);
        assertTrue(bundleVersionFile.exists());

        File versionFile = new File(currentTzDataDir,
                ConfigBundle.TZ_DATA_VERSION_FILE_NAME);
        assertTrue(versionFile.exists());

        File bionicFile = new File(currentTzDataDir, ConfigBundle.ZONEINFO_FILE_NAME);
        assertTrue(bionicFile.exists());

        File icuFile = new File(currentTzDataDir, ConfigBundle.ICU_DATA_FILE_NAME);
        assertTrue(icuFile.exists());

        // Also check no working directory is left lying around.
        File workingDir = installer.getWorkingDir();
        assertFalse(workingDir.exists());
    }

    private void assertNoContentInstalled() {
        File currentTzDataDir = installer.getCurrentTzDataDir();
        assertFalse(currentTzDataDir.exists());

        // Also check no working directories are left lying around.
        File workingDir = installer.getWorkingDir();
        assertFalse(workingDir.exists());

        File oldDataDir = installer.getOldTzDataDir();
        assertFalse(oldDataDir.exists());
    }

    private void createTzDataFile(File file, String rulesVersion) {
        byte[] bytes = new ZoneInfoTestHelper.TzDataBuilder()
                .initializeToValid()
                .setHeaderMagic("tzdata" + rulesVersion)
                .build();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    private static void createFile(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write('a');
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
