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
 * limitations under the License
 */

package libcore.java.lang.invoke;

import junit.framework.TestCase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Vector;

import static java.lang.invoke.MethodHandles.Lookup.*;

public class MethodHandlesTest extends TestCase {
    private static final int ALL_LOOKUP_MODES = (PUBLIC | PRIVATE | PACKAGE | PROTECTED);

    public void test_publicLookupClassAndModes() {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        assertSame(Object.class, publicLookup.lookupClass());
        assertEquals(PUBLIC, publicLookup.lookupModes());
    }

    public void test_defaultLookupClassAndModes() {
        MethodHandles.Lookup defaultLookup = MethodHandles.lookup();
        assertSame(MethodHandlesTest.class, defaultLookup.lookupClass());
        assertEquals(ALL_LOOKUP_MODES, defaultLookup.lookupModes());
    }

    public void test_LookupIn() {
        MethodHandles.Lookup defaultLookup = MethodHandles.lookup();

        // A class in the same package loses the privilege to lookup protected and private
        // members.
        MethodHandles.Lookup siblingLookup = defaultLookup.in(PackageSibling.class);
        assertEquals(ALL_LOOKUP_MODES & ~(PROTECTED | PRIVATE),  siblingLookup.lookupModes());

        // The new lookup isn't in the same package, so it loses all its privileges except
        // for public.
        MethodHandles.Lookup nonSibling = defaultLookup.in(Vector.class);
        assertEquals(PUBLIC, nonSibling.lookupModes());

        // Special case, sibling inner classes in the same parent class
        MethodHandles.Lookup inner2 = Inner1.lookup.in(Inner2.class);
        assertEquals(PUBLIC | PRIVATE | PACKAGE, inner2.lookupModes());

        try {
            MethodHandles.lookup().in(null);
            fail();
        } catch (NullPointerException expected) {
        }

        // Callers cannot change the lookup context to anything within the java.lang.invoke package.
        try {
            MethodHandles.lookup().in(MethodHandle.class);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public static class Inner1 {
        public static MethodHandles.Lookup lookup = MethodHandles.lookup();
    }

    public static class Inner2 {
    }
}

class PackageSibling {
}

