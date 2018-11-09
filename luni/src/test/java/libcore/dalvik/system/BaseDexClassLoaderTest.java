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

package libcore.dalvik.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

import libcore.io.Streams;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class BaseDexClassLoaderTest {
    private static class Reporter implements BaseDexClassLoader.Reporter {
        public final List<ClassLoader> classLoaders = new ArrayList<>();
        public final List<String> loadedDexPaths = new ArrayList<>();

        @Override
        public void report(List<ClassLoader> loaders, List<String> dexPaths) {
            classLoaders.addAll(loaders);
            loadedDexPaths.addAll(dexPaths);
        }

        void reset() {
            classLoaders.clear();
            loadedDexPaths.clear();
        }
    }

    private ClassLoader pcl;
    private File jar;
    private Reporter reporter;

    @Before
    public void extractTestJar() throws Exception {
        // Extract loading-test.jar from the resource.
        pcl = BaseDexClassLoaderTest.class.getClassLoader();
        jar = File.createTempFile("loading-test", ".jar");
        try (InputStream in = pcl.getResourceAsStream("dalvik/system/loading-test.jar");
             FileOutputStream out = new FileOutputStream(jar)) {
          Streams.copy(in, out);
        }
    }

    @Before
    public void registerReporter() {
        reporter = new Reporter();
        BaseDexClassLoader.setReporter(reporter);
    }

    @After
    public void unregisterReporter() {
        BaseDexClassLoader.setReporter(null);
    }

    @After
    public void deleteTestJar() throws Exception {
        assertTrue(jar.delete());
    }

    @Test
    public void testReporting() throws Exception {
        // Load the jar file using a PathClassLoader.
        BaseDexClassLoader cl1 = new PathClassLoader(jar.getPath(),
            ClassLoader.getSystemClassLoader());

        // Verify the reported data.
        assertEquals(2, reporter.loadedDexPaths.size());
        assertEquals(2, reporter.classLoaders.size());

        // First class loader should be the one loading the files
        assertEquals(jar.getPath(), reporter.loadedDexPaths.get(0));
        assertEquals(cl1, reporter.classLoaders.get(0));

        // Second class loader should be the system class loader.
        // Don't check the actual classpath as that might vary based on system properties.
        assertEquals(ClassLoader.getSystemClassLoader(), reporter.classLoaders.get(1));
    }

    @Test
    public void testReportingUnknownLoader() throws Exception {
        // Add an unknown classloader between cl1 and the system
        ClassLoader unknownLoader = new ClassLoader(ClassLoader.getSystemClassLoader()) {};
        BaseDexClassLoader cl1 = new PathClassLoader(jar.getPath(), unknownLoader);

        assertEquals(3, reporter.loadedDexPaths.size());
        assertEquals(3, reporter.classLoaders.size());

        assertEquals(jar.getPath(), reporter.loadedDexPaths.get(0));
        assertEquals(cl1, reporter.classLoaders.get(0));

        assertNull(reporter.loadedDexPaths.get(1));
        assertEquals(unknownLoader, reporter.classLoaders.get(1));

        assertEquals(ClassLoader.getSystemClassLoader(), reporter.classLoaders.get(2));
    }

    @Test
    public void testNoReportingAfterResetting() throws Exception {
        BaseDexClassLoader cl1 = new PathClassLoader(jar.getPath(),
            ClassLoader.getSystemClassLoader());

        assertEquals(2, reporter.loadedDexPaths.size());
        assertEquals(2, reporter.classLoaders.size());

        // Check we don't report after the reporter is unregistered.
        unregisterReporter();
        reporter.reset();

        // Load the jar file using another PathClassLoader.
        BaseDexClassLoader cl2 = new PathClassLoader(jar.getPath(), pcl);

        // Verify nothing reported
        assertEquals(0, reporter.loadedDexPaths.size());
        assertEquals(0, reporter.classLoaders.size());
    }
}
