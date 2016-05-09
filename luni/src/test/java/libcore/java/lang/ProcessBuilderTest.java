/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import libcore.java.util.AbstractResourceLeakageDetectorTestCase;

import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.lang.ProcessBuilder.Redirect.PIPE;
import static tests.support.Support_Exec.execAndCheckOutput;

public class ProcessBuilderTest extends AbstractResourceLeakageDetectorTestCase {

    private static String shell() {
        String deviceSh = System.getenv("ANDROID_ROOT") + "/bin/sh";
        String desktopSh = "/bin/sh";
        return new File(deviceSh).exists() ? deviceSh : desktopSh;
    }

    private static void assertRedirectErrorStream(boolean doRedirect,
            String expectedOut, String expectedErr) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2");
        pb.redirectErrorStream(doRedirect);
        execAndCheckOutput(pb, expectedOut, expectedErr);
    }

    public void test_redirectErrorStream_true() throws Exception {
        assertRedirectErrorStream(true, "out\nerr\n", "");
    }

    public void test_redirectErrorStream_false() throws Exception {
        assertRedirectErrorStream(false, "out\n", "err\n");
    }

    public void testEnvironment() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo $A");
        pb.environment().put("A", "android");
        execAndCheckOutput(pb, "android\n", "");
    }

    public void testDestroyClosesEverything() throws IOException {
        Process process = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2").start();
        InputStream in = process.getInputStream();
        InputStream err = process.getErrorStream();
        OutputStream out = process.getOutputStream();
        process.destroy();

        try {
            in.read();
            fail();
        } catch (IOException expected) {
        }
        try {
            err.read();
            fail();
        } catch (IOException expected) {
        }
        try {
            /*
             * We test write+flush because the RI returns a wrapped stream, but
             * only bothers to close the underlying stream.
             */
            out.write(1);
            out.flush();
            fail();
        } catch (IOException expected) {
        }
    }

    public void testDestroyDoesNotLeak() throws IOException {
        Process process = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2").start();
        process.destroy();
    }

    public void testEnvironmentMapForbidsNulls() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo $A");
        Map<String, String> environment = pb.environment();
        Map<String, String> before = new HashMap<String, String>(environment);
        try {
            environment.put("A", null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            environment.put(null, "android");
            fail();
        } catch (NullPointerException expected) {
        }
        assertEquals(before, environment);
    }

    /**
     * Checks that INHERIT and PIPE tend to have different hashCodes
     * in any particular instance of the runtime.
     * We test this by asserting that they use the identity hashCode,
     * which is a sufficient but not necessary condition for this.
     * If the implementation changes to a different sufficient condition
     * in future, this test should be updated accordingly.
     */
    public void testRedirect_inheritAndPipeTendToHaveDifferentHashCode() {
        assertIdentityHashCode(INHERIT);
        assertIdentityHashCode(PIPE);
    }

    public void testRedirect_hashCodeDependsOnFile() {
        File file = new File("/tmp/file");
        File otherFile = new File("/tmp/some_other_file") {
            @Override public int hashCode() { return 1 + file.hashCode(); }
        };
        Redirect a = Redirect.from(file);
        Redirect b = Redirect.from(otherFile);
        assertFalse("Unexpectedly equal hashCode: " + a + " vs. " + b,
                a.hashCode() == b.hashCode());
    }

    /**
     * Tests that {@link Redirect}'s equals() and hashCode() is sane.
     */
    public void testRedirect_equals() {
        File fileA = new File("/tmp/fileA");
        File fileB = new File("/tmp/fileB");
        File fileB2 = new File("/tmp/fileB");
        // check that test is set up correctly
        assertFalse(fileA.equals(fileB));
        assertEquals(fileB, fileB2);

        assertSymmetricEquals(Redirect.appendTo(fileB), Redirect.appendTo(fileB2));
        assertSymmetricEquals(Redirect.from(fileB), Redirect.from(fileB2));
        assertSymmetricEquals(Redirect.to(fileB), Redirect.to(fileB2));

        Redirect[] redirects = new Redirect[] {
                INHERIT,
                PIPE,
                Redirect.appendTo(fileA),
                Redirect.from(fileA),
                Redirect.to(fileA),
                Redirect.appendTo(fileB),
                Redirect.from(fileB),
                Redirect.to(fileB),
        };
        for (Redirect a : redirects) {
            for (Redirect b : redirects) {
                if (a != b) {
                    assertFalse("Unexpectedly equal: " + a + " vs. " + b, a.equals(b));
                    assertFalse("Unexpected asymmetric equality: " + a + " vs. " + b, b.equals(a));
                }
            }
        }
    }

    /**
     * Tests the {@link Redirect#type() type} of various redirects.
     */
    public void testRedirect_type() {
        File file = new File("/tmp/fake-file-for/java.lang.ProcessBuilderTest");
        assertRedirectType(Redirect.Type.INHERIT, INHERIT);
        assertRedirectType(Redirect.Type.PIPE, PIPE);
        assertRedirectType(Redirect.Type.APPEND, Redirect.appendTo(file));
        assertRedirectType(Redirect.Type.READ, Redirect.from(file));
        assertRedirectType(Redirect.Type.WRITE, Redirect.to(file));
    }

    private static void assertRedirectType(Redirect.Type expectedType, Redirect redirect) {
        assertEquals(redirect.toString(), expectedType, redirect.type());
    }

    public void testRedirect_setAndGet() {
        File file = new File("/tmp/fake-file-for/java.lang.ProcessBuilderTest");
        checkSetAndGet(Redirect.from(file), PIPE, PIPE, new ProcessBuilder().redirectInput(file));
        checkSetAndGet(PIPE, Redirect.to(file), PIPE, new ProcessBuilder().redirectOutput(file));
        checkSetAndGet(PIPE, PIPE, Redirect.to(file), new ProcessBuilder().redirectError(file));
        checkSetAndGet(Redirect.from(file), INHERIT, Redirect.to(file),
                new ProcessBuilder()
                        .redirectInput(PIPE)
                        .redirectOutput(INHERIT)
                        .redirectError(file)
                        .redirectInput(file));
    }

    private static void checkSetAndGet(Redirect in, Redirect out, Redirect err, ProcessBuilder pb) {
        List<Redirect> expected = Arrays.asList(in, out, err);
        List<Redirect> actual = Arrays.asList(
                pb.redirectInput(), pb.redirectOutput(), pb.redirectError());
        assertEquals(expected, actual);
    }

    private static void assertIdentityHashCode(Redirect redirect) {
        assertEquals(System.identityHashCode(redirect), redirect.hashCode());
    }

    private static void assertSymmetricEquals(Redirect a, Redirect b) {
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

}
