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
import com.android.tradefed.result.InputStreamSource;
import com.android.tradefed.result.LogDataType;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner.TestLogData;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner.TestMetrics;
import com.android.tradefed.testtype.IDeviceTest;
import com.android.tradefed.util.FileUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Tests that gather metrics about zygote+image heap and about the impact of core library calls on
 * app heap.
 */
@RunWith(DeviceJUnit4ClassRunner.class)
public class LibcoreHeapMetricsTest implements IDeviceTest {

    private static final String TIMESTAMPED_LABEL =
            "LibcoreHeapMetricsTest-" + getCurrentTimeIso8601();
    private static final String APK_INSTRUMENTATION_NAME
            = "libcore.heapdumper/.HeapDumpInstrumentation";
    private static final int WAIT_FOR_DUMP_MAX_RETRIES = 10;
    private static final long WAIT_FOR_DUMP_RETRY_INTERVAL_MILLIS = 1000;

    @Rule public TestMetrics metrics = new TestMetrics();
    @Rule public TestLogData logs = new TestLogData();

    private ITestDevice mTestDevice;
    private String mDeviceParentDirectory;

    @Override
    public void setDevice(ITestDevice device) {
        mTestDevice = device;
    }

    @Override
    public ITestDevice getDevice() {
        return mTestDevice;
    }

    @Before
    public void findDeviceParentDirectory() throws DeviceNotAvailableException {
        mDeviceParentDirectory = mTestDevice.executeShellCommand("echo -n ${EXTERNAL_STORAGE}");
    }

    @Test
    public void measureNoop() throws Exception {
        AhatSnapshot afterDump = runApp("NOOP");
        AhatSnapshot beforeDump = afterDump.getBaseline();
        // TODO(peteg): Common up as much of the metric reporting code as makes sense, once we've
        // got multiple test methods.
        metrics.addTestMetric(
                "zygoteSize", Long.toString(beforeDump.getHeap("zygote").getSize().getSize()));
        metrics.addTestMetric(
                "imageSize", Long.toString(beforeDump.getHeap("image").getSize().getSize()));
        metrics.addTestMetric(
                "beforeAppSize", Long.toString(beforeDump.getHeap("app").getSize().getSize()));
        metrics.addTestMetric(
                "afterAppSize", Long.toString(afterDump.getHeap("app").getSize().getSize()));
        // TODO(peteg): Analyse the zygote + image heaps and add more metrics.
    }

    // TODO(peteg): Add more tests which do some library calls in their action.

    private static String getCurrentTimeIso8601() {
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date now = new Date();
        return iso8601Format.format(now);
    }

    /**
     * Runs the app and fetches the heap dumps.
     *
     * @param action The name of the action to run, sent in the intent to the application
     * @return The heap dump after the action, with the heap dump before the action as its baseline
     */
    private AhatSnapshot runApp(String action)
            throws DeviceNotAvailableException, IOException, HprofFormatException {
        String relativeDirectoryName = String.format("%s-%s", TIMESTAMPED_LABEL, action);
        String deviceDirectoryName =
                String.format("%s/%s", mDeviceParentDirectory, relativeDirectoryName);
        mTestDevice.executeShellCommand(String.format("mkdir %s", deviceDirectoryName));
        try {
            mTestDevice.executeShellCommand(
                    String.format(
                            "am instrument -w -e dumpdir %s -e action %s  %s",
                            relativeDirectoryName, action, APK_INSTRUMENTATION_NAME));
            checkForErrorFile(deviceDirectoryName);
            AhatSnapshot beforeDump = fetchHeapDump(deviceDirectoryName, "before.hprof", action);
            AhatSnapshot afterDump = fetchHeapDump(deviceDirectoryName, "after.hprof", action);
            Diff.snapshots(afterDump, beforeDump);
            return afterDump;
        } finally {
            mTestDevice.executeShellCommand(String.format("rm -r %s", deviceDirectoryName));
        }
    }

    /**
     * Looks for a file called {@code error} in the named device directory, and throws an
     * {@link ApplicationException} using the first line of that file as the message if found.
     */
    private boolean checkForErrorFile(String deviceDirectoryName)
            throws DeviceNotAvailableException, IOException {
        String[] deviceDirectoryContents =
                mTestDevice.executeShellCommand("ls " + deviceDirectoryName).split("\\s");
        for (String deviceFileName : deviceDirectoryContents) {
            if (deviceFileName.equals("error")) {
                throw new ApplicationException(readErrorFile(deviceDirectoryName));
            }
        }
        return false;
    }

    /**
     * Returns the first line read from a file called {@code error} on the device in the named
     * directory.
     *
     * <p>The file is pulled into a temporary location on the host, and deleted after reading.
     */
    private String readErrorFile(String deviceDirectoryName)
            throws IOException, DeviceNotAvailableException {
        File file = mTestDevice.pullFile(String.format("%s/error", deviceDirectoryName));
        if (file == null) {
            throw new RuntimeException("Failed to pull error log: " + file.getAbsolutePath());
        }
        try {
            return FileUtil.readStringFromFile(file);
        } finally {
            file.delete();
        }
    }

    /**
     * Returns an {@link AhatSnapshot} parsed from an {@code hprof} file on the device at the given
     * directory and relative filename.
     *
     * <p>The file is pulled into a temporary location on the host, and deleted after reading. It is
     * also logged via {@link TestLogData} under a name formed from the action and the relative
     * filename (e.g. {@code noop-before.hprof}).
     */
    private AhatSnapshot fetchHeapDump(
            String deviceDirectoryName, String relativeDumpFilename, String action)
            throws DeviceNotAvailableException, IOException, HprofFormatException {
        String deviceFileName = String.format("%s/%s", deviceDirectoryName, relativeDumpFilename);
        File file = mTestDevice.pullFile(deviceFileName);
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
     * Logs the heap dump from the given file via {@link TestLogData} with the given log filename.
     */
    private void logHeapDump(File file, String logFilename) {
        try (FileInputStreamSource dataStream = new FileInputStreamSource(file)) {
            logs.addTestLog(logFilename, LogDataType.HPROF, dataStream);
        }
    }

    /**
     * An exception indicating that the activity on the device encountered an error which it passed
     * back to the host.
     */
    private static class ApplicationException extends RuntimeException {

        private static final long serialVersionUID = 0;

        ApplicationException(String applicationError) {
            super("Error encountered running application on device: " + applicationError);
        }
    }
}
