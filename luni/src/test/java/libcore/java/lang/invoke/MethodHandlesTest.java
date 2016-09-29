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
import java.lang.invoke.MethodType;
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

    public void test_findStatic() throws Exception {
        MethodHandles.Lookup defaultLookup = MethodHandles.lookup();

        // Handle for String String#valueOf(char[]).
        MethodHandle handle = defaultLookup.findStatic(String.class, "valueOf",
                MethodType.methodType(String.class, char[].class));
        assertNotNull(handle);

        assertEquals(String.class, handle.type().returnType());
        assertEquals(1, handle.type().parameterCount());
        assertEquals(char[].class, handle.type().parameterArray()[0]);
        assertEquals(MethodHandle.INVOKE_STATIC, handle.getHandleKind());

        MethodHandles.Lookup inUtil = defaultLookup.in(Vector.class);

        // Package private in a public class in a different package from the lookup.
        try {
            inUtil.findStatic(MethodHandlesTest.class, "packagePrivateStaticMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Protected in a public class in a different package from the lookup.
        try {
            inUtil.findStatic(MethodHandlesTest.class, "protectedStaticMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Private in a public class in a different package from the lookup.
        try {
            inUtil.findStatic(MethodHandlesTest.class, "privateStaticMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Public method in a package private class in a different package from the lookup.
        try {
            inUtil.findStatic(PackageSibling.class, "publicStaticMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Public virtual method should not discoverable via findStatic.
        try {
            inUtil.findStatic(MethodHandlesTest.class, "publicMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }
    }

    public void test_findConstructor() throws Exception {
        MethodHandles.Lookup defaultLookup = MethodHandles.lookup();

        // Handle for String.<init>(String). The requested type of the constructor declares
        // a void return type (to match the bytecode) but the handle that's created will declare
        // a return type that's equal to the type being constructed.
        MethodHandle handle = defaultLookup.findConstructor(String.class,
                MethodType.methodType(void.class, String.class));
        assertNotNull(handle);

        assertEquals(String.class, handle.type().returnType());
        assertEquals(1, handle.type().parameterCount());

        assertEquals(String.class, handle.type().parameterArray()[0]);
        assertEquals(MethodHandle.INVOKE_DIRECT, handle.getHandleKind());

        MethodHandles.Lookup inUtil = defaultLookup.in(Vector.class);

        // Package private in a public class in a different package from the lookup.
        try {
            inUtil.findConstructor(ConstructorTest.class,
                    MethodType.methodType(void.class, String.class, int.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Protected in a public class in a different package from the lookup.
        try {
            inUtil.findConstructor(ConstructorTest.class,
                    MethodType.methodType(void.class, String.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Private in a public class in a different package from the lookup.
        try {
            inUtil.findConstructor(ConstructorTest.class,
                    MethodType.methodType(void.class, String.class, char.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Protected constructor in a package private class in a different package from the lookup.
        try {
            inUtil.findConstructor(PackageSibling.class,
                    MethodType.methodType(void.class, String.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Public constructor in a package private class in a different package from the lookup.
        try {
            inUtil.findConstructor(PackageSibling.class,
                    MethodType.methodType(void.class, String.class, char.class));
            fail();
        } catch (IllegalAccessException expected) {
        }
    }

    public void test_findVirtual() throws Exception {
        MethodHandles.Lookup defaultLookup = MethodHandles.lookup();

        // String.replaceAll(String, String);
        MethodHandle handle = defaultLookup.findVirtual(String.class, "replaceAll",
                MethodType.methodType(String.class, String.class, String.class));
        assertNotNull(handle);

        assertEquals(String.class, handle.type().returnType());
        // Note that the input type was (String,String)String but the handle's type is
        // (String, String, String)String - since it's a non static call, we prepend the
        // receiver to the type.
        assertEquals(3, handle.type().parameterCount());
        MethodType expectedType = MethodType.methodType(String.class,
                new Class<?>[] { String.class, String.class, String.class});

        assertEquals(expectedType, handle.type());
        assertEquals(MethodHandle.INVOKE_VIRTUAL, handle.getHandleKind());

        MethodHandles.Lookup inUtil = defaultLookup.in(Vector.class);

        // Package private in a public class in a different package from the lookup.
        try {
            inUtil.findVirtual(MethodHandlesTest.class, "packagePrivateMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Protected in a public class in a different package from the lookup.
        try {
            inUtil.findVirtual(MethodHandlesTest.class, "protectedMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Protected in a public class in a different package from the lookup.
        try {
            inUtil.findVirtual(MethodHandlesTest.class, "privateMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Public method in a package private class in a different package from the lookup.
        try {
            inUtil.findVirtual(PackageSibling.class, "publicMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }

        // Public static method should not discoverable via findVirtual.
        try {
            inUtil.findVirtual(MethodHandlesTest.class, "publicStaticMethod",
                    MethodType.methodType(void.class));
            fail();
        } catch (IllegalAccessException expected) {
        }
    }

    public static class Inner1 {
        public static MethodHandles.Lookup lookup = MethodHandles.lookup();
    }

    public static class Inner2 {
    }

    private static void privateStaticMethod() {}
    public static void publicStaticMethod() {}
    static void packagePrivateStaticMethod() {}
    protected static void protectedStaticMethod() {}

    public void publicMethod() {}
    private void privateMethod() {}
    void packagePrivateMethod() {}
    protected void protectedMethod() {}

    public static class ConstructorTest {
        ConstructorTest(String unused, int unused2) {}
        protected ConstructorTest(String unused) {}
        private ConstructorTest(String unused, char unused2) {}
    }
}

class PackageSibling {
    public void publicMethod() {}
    public static void publicStaticMethod() {}

    protected PackageSibling(String unused) {}
    public PackageSibling(String unused, char unused2) {}
}

