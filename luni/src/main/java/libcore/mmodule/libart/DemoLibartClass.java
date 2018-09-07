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

package libcore.mmodule.libart;

import libcore.mmodule.CoreApi;
import libcore.mmodule.IntraCoreMModuleApi;
import libcore.mmodule.simple.DemoSimpleClass;

/**
 * A class that provides:
 * <ul>
 * <li>A method within core-libart for the core-simple mmodule to depend on,
 * and a method that depends on the core-simple mmodule thereby demonstrating a
 * bi-directional, intra-module dependency.</li>
 * <li>A "core module API" method for use by higher-level code.</li>
 * </ul>
 *
 * @hide
 */
@CoreApi
@IntraCoreMModuleApi
public class DemoLibartClass {

    private DemoLibartClass() {}

    /**
     * A method that depends on the simple mmodule to work.
     *
     * @hide
     */
    @IntraCoreMModuleApi
    public static String intraCoreDependencyMethod() {
        // Delegate to core-simple code to implement the method.
        return DemoSimpleClass.simpleMethod();
    }

    /**
     * A simple method that has no native or data file dependencies but is part of the intra-core
     * mmodule API contract.
     *
     * @hide
     */
    @IntraCoreMModuleApi
    public static String simpleMethod() {
        return "Hello World";
    }

    /**
     * A core API method provided to higher-level code in the Android software stack.
     */
    @CoreApi
    public static String coreApiMethod() {
        return "Hello World";
    }

    /**
     * A method that is public but not part of the intra-core mmodule API contract, i.e. it cannot
     * be used from an mmodule.
     *
     * @hide
     */
    public static String hiddenMethod() {
        return "Hello World";
    }
}
