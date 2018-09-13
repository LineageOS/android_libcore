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

package com.android.coreplatformapiclient;

import libcore.mmodule.libart.DemoLibartClass;

/**
 * A class that uses "core API" and can be used to test framework build rule changes.
 */
public class DemoCorePlatformApiClientClass {

    public static void callingPublicApi() {
        new Object();
    }

    public static void callingCorePlatformApi() {
        DemoLibartClass.corePlatformApiMethod();

        // This is an example of a method that is not in the public SDK or core API so will fail
        // compilation if the build is doing things correctly.
        // Byte.toHexString((byte) 8, true);
    }
}
