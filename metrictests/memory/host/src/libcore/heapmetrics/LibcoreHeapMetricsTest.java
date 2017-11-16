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
import com.android.ahat.heapdump.Size;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner.TestLogData;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner.TestMetrics;
import com.android.tradefed.testtype.IDeviceTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that gather metrics about zygote+image heap and about the impact of core library calls on
 * app heap.
 */
@RunWith(DeviceJUnit4ClassRunner.class)
public class LibcoreHeapMetricsTest implements IDeviceTest {

    @Rule public TestMetrics metrics = new TestMetrics();
    @Rule public TestLogData logs = new TestLogData();

    private ITestDevice testDevice;
    private HeapDumpRunner heapDumpRunner;

    @Override
    public void setDevice(ITestDevice device) {
        testDevice = device;
    }

    @Override
    public ITestDevice getDevice() {
        return testDevice;
    }

    @Before
    public void initializeHeapDumperRunner() throws DeviceNotAvailableException {
        heapDumpRunner = HeapDumpRunner.create(testDevice, logs);
    }

    @Test
    public void measureNoop() throws Exception {
        AhatSnapshot afterDump = heapDumpRunner.runInstrumentation("NOOP");
        AhatSnapshot beforeDump = afterDump.getBaseline();
        // TODO(peteg): Common up as much of the metric reporting code as makes sense, once we've
        // got multiple test methods.
        recordSizeMetric("zygoteSize", beforeDump.getHeap("zygote").getSize());
        recordSizeMetric("imageSize", beforeDump.getHeap("image").getSize());
        recordSizeMetric("beforeAppSize", beforeDump.getHeap("app").getSize());
        recordSizeMetric("afterAppSize", afterDump.getHeap("app").getSize());
        // TODO(peteg): Analyse the zygote + image heaps and add more metrics.
    }

    // TODO(peteg): Add more tests which do some library calls in their action.

    private void recordSizeMetric(String name, Size size) {
        metrics.addTestMetric(name, Long.toString(size.getSize()));
    }
}
