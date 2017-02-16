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
import libcore.tzdata.update2.tools.TimeZoneDistroBuilder;

/**
 * Tests for {@link TimeZoneDistroInstaller}.
 */
public class TimeZoneDistroInstallerTest extends TestCase {

    // OLDER_RULES_VERSION < SYSTEM_RULES_VERSION < NEW_RULES_VERSION < NEWER_RULES_VERSION
    private static final String OLDER_RULES_VERSION = "2030a";
    private static final String SYSTEM_RULES_VERSION = "2030b";
    private static final String NEW_RULES_VERSION = "2030c";
    private static final String NEWER_RULES_VERSION = "2030d";

    private TimeZoneDistroInstaller installer;
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

        installer = new TimeZoneDistroInstaller(
                "TimeZoneDistroInstallerTest", testSystemTzDataFile, testInstallDir);
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
    public void testInstall_badSystemFile() throws Exception {
        File doesNotExist = new File(testSystemTzDataDir, "doesNotExist");
        TimeZoneDistroInstaller brokenSystemInstaller = new TimeZoneDistroInstaller(
                "TimeZoneDistroInstallerTest", doesNotExist, testInstallDir);
        TimeZoneDistro tzData = createValidTimeZoneDistro(NEW_RULES_VERSION, 1);

        try {
            brokenSystemInstaller.installWithErrorCode(tzData.getBytes());
            fail();
        } catch (IOException expected) {}

        assertNoContentInstalled();
    }

    /** Tests the first successful update on a device */
    public void testInstall_successfulFirstUpdate() throws Exception {
        TimeZoneDistro distro = createValidTimeZoneDistro(NEW_RULES_VERSION, 1);

        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(distro.getBytes()));
        assertDistroInstalled(distro);
    }

    /**
     * Tests we can install an update the same version as is in /system.
     */
    public void testInstall_successfulFirstUpdate_sameVersionAsSystem() throws Exception {
        TimeZoneDistro distro = createValidTimeZoneDistro(SYSTEM_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(distro.getBytes()));
        assertDistroInstalled(distro);
    }

    /**
     * Tests we cannot install an update older than the version in /system.
     */
    public void testInstall_unsuccessfulFirstUpdate_olderVersionThanSystem() throws Exception {
        TimeZoneDistro distro = createValidTimeZoneDistro(OLDER_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_RULES_TOO_OLD,
                installer.installWithErrorCode(distro.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests an update on a device when there is a prior update already applied.
     */
    public void testInstall_successfulFollowOnUpdate_newerVersion() throws Exception {
        TimeZoneDistro distro1 = createValidTimeZoneDistro(NEW_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(distro1.getBytes()));
        assertDistroInstalled(distro1);

        TimeZoneDistro distro2 = createValidTimeZoneDistro(NEW_RULES_VERSION, 2);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(distro2.getBytes()));
        assertDistroInstalled(distro2);

        TimeZoneDistro distro3 = createValidTimeZoneDistro(NEWER_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(distro3.getBytes()));
        assertDistroInstalled(distro3);
    }

    /**
     * Tests an update on a device when there is a prior update already applied, but the follow
     * on update is older than in /system.
     */
    public void testInstall_unsuccessfulFollowOnUpdate_olderVersion() throws Exception {
        TimeZoneDistro distro1 = createValidTimeZoneDistro(NEW_RULES_VERSION, 2);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(distro1.getBytes()));
        assertDistroInstalled(distro1);

        TimeZoneDistro distro2 = createValidTimeZoneDistro(OLDER_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_RULES_TOO_OLD,
                installer.installWithErrorCode(distro2.getBytes()));
        assertDistroInstalled(distro1);
    }

    /** Tests that a distro with a missing file will not update the content. */
    public void testInstall_missingTzDataFile() throws Exception {
        TimeZoneDistro installedDistro = createValidTimeZoneDistro(NEW_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(installedDistro.getBytes()));
        assertDistroInstalled(installedDistro);

        TimeZoneDistro incompleteDistro =
                createValidTimeZoneDistroBuilder(NEWER_RULES_VERSION, 1)
                        .clearTzDataForTests()
                        .buildUnvalidated();
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_BAD_DISTRO_STRUCTURE,
                installer.installWithErrorCode(incompleteDistro.getBytes()));
        assertDistroInstalled(installedDistro);
    }

    /** Tests that a distro with a missing file will not update the content. */
    public void testInstall_missingIcuFile() throws Exception {
        TimeZoneDistro installedDistro = createValidTimeZoneDistro(NEW_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(installedDistro.getBytes()));
        assertDistroInstalled(installedDistro);

        TimeZoneDistro incompleteDistro =
                createValidTimeZoneDistroBuilder(NEWER_RULES_VERSION, 1)
                        .clearIcuDataForTests()
                        .buildUnvalidated();
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_BAD_DISTRO_STRUCTURE,
                installer.installWithErrorCode(incompleteDistro.getBytes()));
        assertDistroInstalled(installedDistro);
    }

    /**
     * Tests that an update will be unpacked even if there is a partial update from a previous run.
     */
    public void testInstall_withWorkingDir() throws Exception {
        File workingDir = installer.getWorkingDir();
        assertTrue(workingDir.mkdir());
        createFile(new File(workingDir, "myFile"), new byte[] { 'a' });

        TimeZoneDistro distro = createValidTimeZoneDistro(NEW_RULES_VERSION, 1);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_SUCCESS,
                installer.installWithErrorCode(distro.getBytes()));
        assertDistroInstalled(distro);
    }

    /**
     * Tests that a distro without a distro version file will be rejected.
     */
    public void testInstall_withMissingDistroVersionFile() throws Exception {
        // Create a distro without a version file.
        TimeZoneDistro distro = createValidTimeZoneDistroBuilder(NEW_RULES_VERSION, 1)
                .clearVersionForTests()
                .buildUnvalidated();
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_BAD_DISTRO_STRUCTURE,
                installer.installWithErrorCode(distro.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a distro with an newer distro version will be rejected.
     */
    public void testInstall_withNewerDistroVersion() throws Exception {
        // Create a distro that will appear to be newer than the one currently supported.
        TimeZoneDistro distro = createValidTimeZoneDistroBuilder(NEW_RULES_VERSION, 1)
                .replaceFormatVersionForTests(2, 1)
                .buildUnvalidated();
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_BAD_DISTRO_FORMAT_VERSION,
                installer.installWithErrorCode(distro.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a distro with a badly formed distro version will be rejected.
     */
    public void testInstall_withBadlyFormedDistroVersion() throws Exception {
        // Create a distro that has an invalid major distro version. It should be 3 numeric
        // characters, "." and 3 more numeric characters.
        DistroVersion validDistroVersion = new DistroVersion(1, 1, NEW_RULES_VERSION, 1);
        byte[] invalidFormatVersionBytes = validDistroVersion.toBytes();
        invalidFormatVersionBytes[0] = 'A';

        TimeZoneDistro distro = createTimeZoneDistroWithVersionBytes(invalidFormatVersionBytes);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_BAD_DISTRO_STRUCTURE,
                installer.installWithErrorCode(distro.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a distro with a badly formed revision will be rejected.
     */
    public void testInstall_withBadlyFormedRevision() throws Exception {
        // Create a distro that has an invalid revision. It should be 3 numeric characters.
        DistroVersion validDistroVersion = new DistroVersion(1, 1, NEW_RULES_VERSION, 1);
        byte[] invalidRevisionBytes = validDistroVersion.toBytes();
        invalidRevisionBytes[invalidRevisionBytes.length - 3] = 'A';

        TimeZoneDistro distro = createTimeZoneDistroWithVersionBytes(invalidRevisionBytes);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_BAD_DISTRO_STRUCTURE,
                installer.installWithErrorCode(distro.getBytes()));
        assertNoContentInstalled();
    }

    /**
     * Tests that a distro with a badly formed rules version will be rejected.
     */
    public void testInstall_withBadlyFormedRulesVersion() throws Exception {
        // Create a distro that has an invalid rules version. It should be in the form "2016c".
        DistroVersion validDistroVersion = new DistroVersion(1, 1, NEW_RULES_VERSION, 1);
        byte[] invalidRulesVersionBytes = validDistroVersion.toBytes();
        invalidRulesVersionBytes[invalidRulesVersionBytes.length - 6] = 'B';

        TimeZoneDistro distro = createTimeZoneDistroWithVersionBytes(invalidRulesVersionBytes);
        assertEquals(
                TimeZoneDistroInstaller.INSTALL_FAIL_BAD_DISTRO_STRUCTURE,
                installer.installWithErrorCode(distro.getBytes()));
        assertNoContentInstalled();
    }

    public void testUninstall_noExistingDataDistro() throws Exception {
        assertFalse(installer.uninstall());
        assertNoContentInstalled();
    }

    public void testUninstall_existingDataDistro() throws Exception {
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

    public void testGetSystemRulesVersion() throws Exception {
        assertEquals(SYSTEM_RULES_VERSION, installer.getSystemRulesVersion());
    }

    private static TimeZoneDistro createValidTimeZoneDistro(
            String rulesVersion, int revision) throws Exception {
        return createValidTimeZoneDistroBuilder(rulesVersion, revision).build();
    }

    private static TimeZoneDistroBuilder createValidTimeZoneDistroBuilder(
            String rulesVersion, int revision) throws Exception {

        byte[] bionicTzData = createTzData(rulesVersion);
        byte[] icuData = new byte[] { 'a' };
        DistroVersion distroVersion = new DistroVersion(
                DistroVersion.CURRENT_FORMAT_MAJOR_VERSION,
                DistroVersion.CURRENT_FORMAT_MINOR_VERSION,
                rulesVersion,
                revision);
        return new TimeZoneDistroBuilder()
                .setDistroVersion(distroVersion)
                .setTzData(bionicTzData)
                .setIcuData(icuData);
    }

    private void assertDistroInstalled(TimeZoneDistro expectedDistro) throws Exception {
        assertTrue(testInstallDir.exists());

        File currentTzDataDir = installer.getCurrentTzDataDir();
        assertTrue(currentTzDataDir.exists());

        File distroVersionFile =
                new File(currentTzDataDir, TimeZoneDistro.DISTRO_VERSION_FILE_NAME);
        assertTrue(distroVersionFile.exists());

        File bionicFile = new File(currentTzDataDir, TimeZoneDistro.TZDATA_FILE_NAME);
        assertTrue(bionicFile.exists());

        File icuFile = new File(currentTzDataDir, TimeZoneDistro.ICU_DATA_FILE_NAME);
        assertTrue(icuFile.exists());

        // Assert getInstalledDistroVersion() is reporting correctly.
        assertEquals(expectedDistro.getDistroVersion(), installer.getInstalledDistroVersion());

        try (ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(expectedDistro.getBytes()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                File actualFile;
                if (entryName.endsWith(TimeZoneDistro.DISTRO_VERSION_FILE_NAME)) {
                   actualFile = distroVersionFile;
                } else if (entryName.endsWith(TimeZoneDistro.ICU_DATA_FILE_NAME)) {
                    actualFile = icuFile;
                } else if (entryName.endsWith(TimeZoneDistro.TZDATA_FILE_NAME)) {
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

    private void assertNoContentInstalled() throws Exception {
        assertNull(installer.getInstalledDistroVersion());

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
     * Creates a TimeZoneDistro containing arbitrary bytes in the version file. Used for testing
     * distros with badly formed version info.
     */
    private static TimeZoneDistro createTimeZoneDistroWithVersionBytes(byte[] versionBytes)
            throws Exception {

        // Create a valid distro, then manipulate the version file.
        TimeZoneDistro distro = createValidTimeZoneDistro(NEW_RULES_VERSION, 1);
        byte[] distroBytes = distro.getBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(distroBytes.length);
        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new ByteArrayInputStream(distroBytes));
             ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                zipOutputStream.putNextEntry(entry);
                if (entry.getName().equals(TimeZoneDistro.DISTRO_VERSION_FILE_NAME)) {
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
        return new TimeZoneDistro(baos.toByteArray());
    }
}
