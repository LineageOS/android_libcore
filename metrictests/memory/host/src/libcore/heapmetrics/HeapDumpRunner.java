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

package libcore.heapmetrics;

import com.android.ahat.heapdump.AhatSnapshot;
import com.android.ahat.heapdump.Diff;
import com.android.ahat.heapdump.HprofFormatException;
import com.android.ahat.heapdump.Parser;
import com.android.ahat.proguard.ProguardMap;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.result.FileInputStreamSource;
import com.android.tradefed.result.LogDataType;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner.TestLogData;
import com.android.tradefed.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class that runs the {@link libcore.heapdumper.HeapDumpInstrumentation} instrumentation
 * on a test device.
 */
class HeapDumpRunner {

    private static final String APK_INSTRUMENTATION_NAME
            = "libcore.heapdumper/.HeapDumpInstrumentation";

    private final ITestDevice testDevice;
    private final String deviceParentDirectory;
    private final TestLogData logs;
    private final String timestampedLabel;

    /**
     * Creates a helper using the given {@link ITestDevice}, uploading heap dumps to the given
     * {@link TestLogData}.
     */
    static HeapDumpRunner create(ITestDevice testDevice, TestLogData logs)
            throws DeviceNotAvailableException {
        String deviceParentDirectory =
                testDevice.executeShellCommand("echo -n ${EXTERNAL_STORAGE}");
        return new HeapDumpRunner(testDevice, deviceParentDirectory, logs);
    }

    private HeapDumpRunner(
            ITestDevice testDevice, String deviceParentDirectory, TestLogData logs) {
        this.testDevice = testDevice;
        this.deviceParentDirectory = deviceParentDirectory;
        this.logs = logs;
        this.timestampedLabel = "LibcoreHeapMetricsTest-" + getCurrentTimeIso8601();
    }

    /**
     * Runs the instrumentation and fetches the heap dumps.
     *
     * @param action The name of the action to run, to be sent as an argument to the instrumentation
     * @return The heap dump after the action, with the heap dump before the action as its baseline
     */
    AhatSnapshot runInstrumentation(String action)
            throws DeviceNotAvailableException, IOException, HprofFormatException {
        String relativeDirectoryName = String.format("%s-%s", timestampedLabel, action);
        String deviceDirectoryName =
                String.format("%s/%s", deviceParentDirectory, relativeDirectoryName);
        testDevice.executeShellCommand(String.format("mkdir %s", deviceDirectoryName));
        try {
            testDevice.executeShellCommand(
                    String.format(
                            "am instrument -w -e dumpdir %s -e action %s  %s",
                            relativeDirectoryName, action, APK_INSTRUMENTATION_NAME));
            checkForErrorFile(deviceDirectoryName);
            AhatSnapshot beforeDump = fetchHeapDump(deviceDirectoryName, "before.hprof",
                    action);
            AhatSnapshot afterDump = fetchHeapDump(deviceDirectoryName, "after.hprof", action);
            Diff.snapshots(afterDump, beforeDump);
            return afterDump;
        } finally {
            testDevice.executeShellCommand(String.format("rm -r %s", deviceDirectoryName));
        }
    }

    /**
     * Looks for a file called {@code error} in the named device directory, and throws an
     * {@link ApplicationException} using the first line of that file as the message if found.
     */
    private void checkForErrorFile(String deviceDirectoryName)
            throws DeviceNotAvailableException, IOException {
        String[] deviceDirectoryContents =
                testDevice.executeShellCommand("ls " + deviceDirectoryName).split("\\s");
        for (String deviceFileName : deviceDirectoryContents) {
            if (deviceFileName.equals("error")) {
                throw new ApplicationException(readErrorFile(deviceDirectoryName));
            }
        }
    }

    /**
     * Returns the first line read from a file called {@code error} on the device in the named
     * directory.
     *
     * <p>The file is pulled into a temporary location on the host, and deleted after reading.
     */
    private String readErrorFile(String deviceDirectoryName)
            throws IOException, DeviceNotAvailableException {
        File file = testDevice.pullFile(String.format("%s/error", deviceDirectoryName));
        if (file == null) {
            throw new RuntimeException(
                    "Failed to pull error log from directory " + deviceDirectoryName);
        }
        try {
            return FileUtil.readStringFromFile(file);
        } finally {
            file.delete();
        }
    }

    /**
     * Returns an {@link AhatSnapshot} parsed from an {@code hprof} file on the device at the
     * given directory and relative filename.
     *
     * <p>The file is pulled into a temporary location on the host, and deleted after reading.
     * It is also logged via {@link TestLogData} under a name formed from the action and the
     * relative filename (e.g. {@code noop-before.hprof}).
     */
    private AhatSnapshot fetchHeapDump(
            String deviceDirectoryName, String relativeDumpFilename, String action)
            throws DeviceNotAvailableException, IOException, HprofFormatException {
        String deviceFileName = String
                .format("%s/%s", deviceDirectoryName, relativeDumpFilename);
        File file = testDevice.pullFile(deviceFileName);
        if (file == null) {
            throw new RuntimeException("Failed to pull dump: " + deviceFileName);
        }
        try {
            logHeapDump(file, String.format("%s-%s", action, relativeDumpFilename));
            return Parser.parseHeapDump(file, new ProguardMap());
        } finally {
            file.delete();
        }
    }

    /**
     * Logs the heap dump from the given file via {@link TestLogData} with the given log
     * filename.
     */
    private void logHeapDump(File file, String logFilename) {
        try (FileInputStreamSource dataStream = new FileInputStreamSource(file)) {
            logs.addTestLog(logFilename, LogDataType.HPROF, dataStream);
        }
    }

    private static String getCurrentTimeIso8601() {
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date now = new Date();
        return iso8601Format.format(now);
    }

    /**
     * An exception indicating that the activity on the device encountered an error which it
     * passed
     * back to the host.
     */
    private static class ApplicationException extends RuntimeException {

        private static final long serialVersionUID = 0;

        ApplicationException(String applicationError) {
            super("Error encountered running application on device: " + applicationError);
        }
    }
}
