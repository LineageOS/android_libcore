/*
 * Copyright (C) 2018 The Android Open Source Project
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

package test;

/**
 * Class used as part of the class loading tests.
 */
public class TestJni {
    static {
        System.loadLibrary("test_jni");
    }

    /**
     * Simple sameness assertion checker.
     */
    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new RuntimeException(
                "EXPECTED: " + expected + "; ACTUAL: " + actual);
        }
    }

    /**
     * Invoke simple native method.
     */
    public static void test_nativeMethod() throws Exception {
        TestJni obj = new TestJni();
        assertSame(obj, obj.returnThis());
    }

    private native Object returnThis();
}
