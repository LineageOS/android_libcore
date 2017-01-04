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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import libcore.io.IoUtils;
import libcore.io.Streams;
import libcore.tzdata.testing.ZoneInfoTestHelper;
import libcore.tzdata.update2.tools.TimeZoneBundleBuilder;

/**
 * Tests for {@link TimeZoneBundleInstaller}.
 */
public class TimeZoneBundleInstallerTest extends TestCase {

    // OLDER_RULES_VERSION < SYSTEM_RULES_VERSION < NEW_RULES_VERSION < NEWER_RULES_VERSION
    private static final String OLDER_RULES_VERSION = "2030a";
    private static final String SYSTEM_RULES_VERSION = "2030b";
    private static final String NEW_RULES_VERSION = "2030c";
    private static final String NEWER_RULES_VERSION = "2030d";

    private TimeZoneBundleInstaller installer;
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
        byte[] systemTzDataBytes = createTzData(SYSTEM_RULES_VERSION);
        createFile(testSystemTzDataFile, systemTzDataBytes);

        installer = new TimeZoneBundleInstaller(
                "TimeZoneBundleInstallerTest", testSystemTzDataFile, testInstallDir);
    }

    private static File createDirectory(String prefix) throws Exception {
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
        TimeZoneBundleInstaller brokenSystemInstaller = new TimeZoneBundleInstaller(
                "TimeZoneBundleInstallerTest", doesNotExist, testInstallDir);
        TimeZoneBundle tzData = createValidTimeZoneBundle(NEW_RULES_VERSION, "001");

        assertFalse(brokenSystemInstaller.install(tzData.getBytes()));
        assertNoContentInstalled();
    }

    /** Tests the first successful update on a device */
    public void testSuccessfulFirstUpdate() throws Exception {
        TimeZoneBundle bundle = createValidTimeZoneBundle(NEW_RULES_VERSION, "001");

        assertTrue(installer.install(bundle.getBytes()));
        assertBundleInstalled(bundle);
    }

    /**
     * Tests we can install an update the same version as is in /system.
     */
    public void testSuccessfulFirstUpdate_sameVersionAsSystem() throws Exception {
        TimeZoneBundle bundle = createValidTimeZoneBundle(SYSTEM_RULES_VERSION, "001");
        assertTrue(installer.install(bundle.getBytes()));
        assertBundleInstalled(bundle);
    }

    /**
     * Tests we cannot install an update older than the version in /system.
     */
    public void testUnsuccessfulFirstUpdate_olderVersionThanSystem() throws Exception {
        TimeZoneBundle bundle = createValidTimeZoneBundle(OLDER_RULES_VERSION, "001");
        assertFalse(installer.install(bundle.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests an update on a device when there is a prior update already applied.
     */
    public void testSuccessfulFollowOnUpdate_newerVersion() throws Exception {
        TimeZoneBundle bundle1 = createValidTimeZoneBundle(NEW_RULES_VERSION, "001");
        assertTrue(installer.install(bundle1.getBytes()));
        assertBundleInstalled(bundle1);

        TimeZoneBundle bundle2 = createValidTimeZoneBundle(NEW_RULES_VERSION, "002");
        assertTrue(installer.install(bundle2.getBytes()));
        assertBundleInstalled(bundle2);

        TimeZoneBundle bundle3 = createValidTimeZoneBundle(NEWER_RULES_VERSION, "001");
        assertTrue(installer.install(bundle3.getBytes()));
        assertBundleInstalled(bundle3);
    }

    /**
     * Tests an update on a device when there is a prior update already applied, but the follow
     * on update is older than in /system.
     */
    public void testUnsuccessfulFollowOnUpdate_olderVersion() throws Exception {
        TimeZoneBundle bundle1 = createValidTimeZoneBundle(NEW_RULES_VERSION, "002");
        assertTrue(installer.install(bundle1.getBytes()));
        assertBundleInstalled(bundle1);

        TimeZoneBundle bundle2 = createValidTimeZoneBundle(OLDER_RULES_VERSION, "001");
        assertFalse(installer.install(bundle2.getBytes()));
        assertBundleInstalled(bundle1);
    }

    /** Tests that a bundle with a missing file will not update the content. */
    public void testMissingTzDataFile() throws Exception {
        TimeZoneBundle installedBundle = createValidTimeZoneBundle(NEW_RULES_VERSION, "001");
        assertTrue(installer.install(installedBundle.getBytes()));
        assertBundleInstalled(installedBundle);

        TimeZoneBundle incompleteBundle =
                createValidTimeZoneBundleBuilder(NEWER_RULES_VERSION, "001")
                        .clearTzDataForTests()
                        .buildUnvalidated();
        assertFalse(installer.install(incompleteBundle.getBytes()));
        assertBundleInstalled(installedBundle);
    }

    /** Tests that a bundle with a missing file will not update the content. */
    public void testMissingIcuFile() throws Exception {
        TimeZoneBundle installedBundle = createValidTimeZoneBundle(NEW_RULES_VERSION, "001");
        assertTrue(installer.install(installedBundle.getBytes()));
        assertBundleInstalled(installedBundle);

        TimeZoneBundle incompleteBundle =
                createValidTimeZoneBundleBuilder(NEWER_RULES_VERSION, "001")
                        .clearIcuDataForTests()
                        .buildUnvalidated();
        assertFalse(installer.install(incompleteBundle.getBytes()));
        assertBundleInstalled(installedBundle);
    }

    /**
     * Tests that an update will be unpacked even if there is a partial update from a previous run.
     */
    public void testInstallWithWorkingDir() throws Exception {
        File workingDir = installer.getWorkingDir();
        assertTrue(workingDir.mkdir());
        createFile(new File(workingDir, "myFile"), new byte[] { 'a' });

        TimeZoneBundle bundle = createValidTimeZoneBundle(NEW_RULES_VERSION, "001");
        assertTrue(installer.install(bundle.getBytes()));
        assertBundleInstalled(bundle);
    }

    /**
     * Tests that a bundle without a bundle version file will be rejected.
     */
    public void testInstallWithMissingBundleVersionFile() throws Exception {
        // Create a bundle without a version file.
        TimeZoneBundle bundle = createValidTimeZoneBundleBuilder(NEW_RULES_VERSION, "001")
                .clearVersionForTests()
                .buildUnvalidated();
        assertFalse(installer.install(bundle.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a bundle with an newer bundle version will be rejected.
     */
    public void testInstallWithNewerBundleVersion() throws Exception {
        // Create a bundle that will appear to be newer than the one currently supported.
        TimeZoneBundle bundle = createValidTimeZoneBundleBuilder(NEW_RULES_VERSION, "001")
                .setBundleVersionForTests("002.001")
                .buildUnvalidated();
        assertFalse(installer.install(bundle.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a bundle with a badly formed bundle version will be rejected.
     */
    public void testInstallWithBadlyFormedBundleVersion() throws Exception {
        // Create a bundle that has an invalid major bundle version. It should be 3 numeric
        // characters, "." and 3 more numeric characters.
        String invalidBundleVersion = "A01.001";
        byte[] versionBytes =
                BundleVersion.getBytes(invalidBundleVersion, NEW_RULES_VERSION, "001");
        TimeZoneBundle bundle = createTimeZoneBundleWithVersionBytes(versionBytes);
        assertFalse(installer.install(bundle.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a bundle with a badly formed android revision will be rejected.
     */
    public void testInstallWithBadlyFormedAndroidRevision() throws Exception {
        // Create a bundle that has an invalid Android revision. It should be 3 numeric characters.
        String invalidAndroidRevision = "A01";
        byte[] versionBytes = BundleVersion.getBytes(BundleVersion.FULL_BUNDLE_FORMAT_VERSION,
                NEW_RULES_VERSION, invalidAndroidRevision);
        TimeZoneBundle bundle = createTimeZoneBundleWithVersionBytes(versionBytes);
        assertFalse(installer.install(bundle.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a bundle with a badly formed android revision will be rejected.
     */
    public void testInstallWithBadlyFormedRulesVersion() throws Exception {
        // Create a bundle that has an invalid rules version. It should be in the form "2016c".
        final String invalidRulesVersion = "203Bc";
        byte[] versionBytes = BundleVersion.getBytes(BundleVersion.FULL_BUNDLE_FORMAT_VERSION,
                invalidRulesVersion, "001");
        TimeZoneBundle bundle = createTimeZoneBundleWithVersionBytes(versionBytes);
        assertFalse(installer.install(bundle.getBytes()));
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

    private static TimeZoneBundle createValidTimeZoneBundle(
            String rulesVersion, String androidRevision) throws Exception {
        return createValidTimeZoneBundleBuilder(rulesVersion, androidRevision).build();
    }

    private static TimeZoneBundleBuilder createValidTimeZoneBundleBuilder(
            String rulesVersion, String androidRevision) throws Exception {

        byte[] bionicTzData = createTzData(rulesVersion);
        byte[] icuData = new byte[] { 'a' };

        return new TimeZoneBundleBuilder()
                .setRulesVersion(rulesVersion)
                .setAndroidRevision(androidRevision)
                .setTzData(bionicTzData)
                .setIcuData(icuData);
    }

    private void assertBundleInstalled(TimeZoneBundle expectedBundle) throws Exception {
        assertTrue(testInstallDir.exists());

        File currentTzDataDir = installer.getCurrentTzDataDir();
        assertTrue(currentTzDataDir.exists());

        File bundleVersionFile =
                new File(currentTzDataDir, TimeZoneBundle.BUNDLE_VERSION_FILE_NAME);
        assertTrue(bundleVersionFile.exists());

        File bionicFile = new File(currentTzDataDir, TimeZoneBundle.TZDATA_FILE_NAME);
        assertTrue(bionicFile.exists());

        File icuFile = new File(currentTzDataDir, TimeZoneBundle.ICU_DATA_FILE_NAME);
        assertTrue(icuFile.exists());

        try (ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(expectedBundle.getBytes()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                File actualFile;
                if (entryName.endsWith(TimeZoneBundle.BUNDLE_VERSION_FILE_NAME)) {
                   actualFile = bundleVersionFile;
                } else if (entryName.endsWith(TimeZoneBundle.ICU_DATA_FILE_NAME)) {
                    actualFile = icuFile;
                } else if (entryName.endsWith(TimeZoneBundle.TZDATA_FILE_NAME)) {
                    actualFile = bionicFile;
                } else {
                    throw new AssertionFailedError("Unknown file found");
                }
                assertContentsMatches(zis, actualFile);
            }
        }

        // Also check no working directory is left lying around.
        File workingDir = installer.getWorkingDir();
        assertFalse(workingDir.exists());
    }

    private void assertContentsMatches(InputStream expected, File actual)
            throws Exception {
        byte[] actualBytes = IoUtils.readFileAsByteArray(actual.getPath());
        byte[] expectedBytes = Streams.readFullyNoClose(expected);
        assertTrue(Arrays.equals(expectedBytes, actualBytes));
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

    private static byte[] createTzData(String rulesVersion) {
        return new ZoneInfoTestHelper.TzDataBuilder()
                .initializeToValid()
                .setHeaderMagic("tzdata" + rulesVersion)
                .build();
    }

    private static void createFile(File file, byte[] bytes) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Creates a TimeZoneBundle containing arbitrary bytes in the version file. Used for testing
     * bundles with badly formed version info.
     */
    private static TimeZoneBundle createTimeZoneBundleWithVersionBytes(byte[] versionBytes)
            throws Exception {

        // Create a valid bundle, then manipulate the version file.
        TimeZoneBundle bundle = createValidTimeZoneBundle(NEW_RULES_VERSION, "001");
        byte[] bundleBytes = bundle.getBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(bundleBytes.length);
        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new ByteArrayInputStream(bundleBytes));
             ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                zipOutputStream.putNextEntry(entry);
                if (entry.getName().equals(TimeZoneBundle.BUNDLE_VERSION_FILE_NAME)) {
                    // Replace the content.
                    zipOutputStream.write(versionBytes);
                }  else {
                    // Just copy the content.
                    Streams.copy(zipInputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
                zipInputStream.closeEntry();
            }
        }
        return new TimeZoneBundle(baos.toByteArray());
    }
}
