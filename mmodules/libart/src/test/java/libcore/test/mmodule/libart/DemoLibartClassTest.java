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

package libcore.test.mmodule.libart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import libcore.mmodule.libart.DemoLibartClass;

import org.junit.Test;

/**
 * A test for the presence and behavior of {@link DemoLibartClass}.
 */
public class DemoLibartClassTest {

    @Test
    public void classLoader() {
        Class<?> clazz = DemoLibartClass.class;
        ClassLoader bootClassLoader = ClassLoader.getSystemClassLoader().getParent();

        // The DemoLibartClass must be loaded by the boot classloader.
        assertSame(bootClassLoader, clazz.getClassLoader());
    }

    @Test
    public void simpleMethod() {
        assertEquals("Hello World", DemoLibartClass.simpleMethod());
    }

    @Test
    public void intraCoreDependencyMethod() {
        assertEquals("Hello World", DemoLibartClass.intraCoreDependencyMethod());
    }
}
