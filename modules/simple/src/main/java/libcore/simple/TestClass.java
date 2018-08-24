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

package libcore.simple;

/**
 * A class that nothing in libcore or the Android framework depends on. It is intended for use in a
 * fake installable module. Its presence can be tested for, the classloader identified and its
 * behavior modified over time to simulate a real module, without touching any "real" platform
 * logic.
 */
public class TestClass {

    private TestClass() {}

    /**
     * A simple method that has no native or data file dependencies.
     */
    public static String simpleMethod() {
        return "Hello World";
    }
}
