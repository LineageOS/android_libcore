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

package libcore.mmodule.simple;

import libcore.mmodule.IntraCoreMModuleApi;
import libcore.mmodule.libart.DemoLibartClass;

/**
 * A class that nothing in libcore or the Android framework depends on to provide public SDK
 * behavior. It is intended for use in a fake installable mmodule. Its presence can be tested for,
 * the classloader identified and its behavior modified over time to simulate real mmodule code,
 * without touching any "real" platform logic.
 *
 * @hide
 */
@IntraCoreMModuleApi
public class DemoSimpleClass {

    private DemoSimpleClass() {}

    /**
     * A simple method that has no native or data file dependencies but is part of the simple
     * mmodule's API contract.
     *
     * @hide
     */
    @IntraCoreMModuleApi
    public static String simpleMethod() {
        return "Hello World";
    }

    /**
     * A method that depends on another part of the core libraries to work.
     *
     * @hide
     */
    @IntraCoreMModuleApi // Exposed for tests
    public static String intraCoreDependencyMethod() {
        // Delegate to core-libart code to implement the method.
        return DemoLibartClass.simpleMethod();
    }

    /**
     * A method that is public but not part of the simple mmodule's API contract.
     *
     * @hide
     */
    public static String hiddenMethod() {
        return "Hello World";
    }
}
