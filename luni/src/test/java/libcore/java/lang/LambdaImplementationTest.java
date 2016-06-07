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

package libcore.java.lang;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class LambdaImplementationTest extends TestCase {

    private static final String MSG = "Hello World";

    public void testNonCapturingLambda() throws Exception {
        Callable<String> r1 = () -> MSG;
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaImplementsInterfaces(r1, Callable.class);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertNonSerializableLambdaCharacteristics(r1);
        assertCallableBehavior(r1, MSG);

        Callable<String> r2 = () -> MSG;
        assertMultipleInstanceCharacteristics(r1, r2);
    }

    interface Condition<T> {
        boolean check(T arg);
    }

    public void testInstanceMethodReferenceLambda() throws Exception {
        Condition<String> c = String::isEmpty;
        Class<?> lambdaClass = c.getClass();
        assertGeneralLambdaClassCharacteristics(c);
        assertLambdaImplementsInterfaces(c, Condition.class);
        assertLambdaMethodCharacteristics(c, Condition.class);
        assertNonSerializableLambdaCharacteristics(c);

        // Check the behavior of the lambda's method.
        assertTrue(c.check(""));
        assertFalse(c.check("notEmpty"));

        Method implCallMethod = lambdaClass.getMethod(
                "check", Object.class /* type erasure => not String.class */);
        assertTrue((Boolean) implCallMethod.invoke(c, ""));
        assertFalse((Boolean) implCallMethod.invoke(c, "notEmpty"));

        Method interfaceCallMethod = Condition.class.getDeclaredMethod(
                "check", Object.class /* type erasure => not String.class */);
        assertTrue((Boolean) interfaceCallMethod.invoke(c, ""));
        assertFalse((Boolean) interfaceCallMethod.invoke(c, "notEmpty"));
    }

    public void testStaticMethodReferenceLambda() throws Exception {
        Callable<String> r1 = LambdaImplementationTest::staticMethod;
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaImplementsInterfaces(r1, Callable.class);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertNonSerializableLambdaCharacteristics(r1);

        assertCallableBehavior(r1, MSG);

        Callable<String> r2 = LambdaImplementationTest::staticMethod;
        assertMultipleInstanceCharacteristics(r1, r2);
    }

    public void testObjectMethodReferenceLambda() throws Exception {
        StringBuilder o = new StringBuilder(MSG);
        Callable<String> r1 = o::toString;
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaImplementsInterfaces(r1, Callable.class);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertNonSerializableLambdaCharacteristics(r1);

        assertCallableBehavior(r1, MSG);

        Callable<String> r2 = o::toString;
        assertMultipleInstanceCharacteristics(r1, r2);
    }

    public void testArgumentCapturingLambda() throws Exception {
        String msg = MSG;
        Callable<String> r1 = () -> msg;
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaImplementsInterfaces(r1, Callable.class);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertNonSerializableLambdaCharacteristics(r1);

        assertCallableBehavior(r1, MSG);

        Callable<String> r2 = () -> msg;
        assertMultipleInstanceCharacteristics(r1, r2);
    }

    public void testSerializableLambda_withoutState() throws Exception {
        Callable<String> r1 = (Callable<String> & Serializable) () -> MSG;
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaImplementsInterfaces(r1, Callable.class, Serializable.class);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertSerializableLambdaCharacteristics(r1);

        assertCallableBehavior(r1, MSG);

        Callable<String> r2 = (Callable<String> & Serializable) () -> MSG;
        assertMultipleInstanceCharacteristics(r1, r2);
    }

    public void testSerializableLambda_withState() throws Exception {
        final int state = 123;
        Callable<String> r1 = (Callable<String> & Serializable) () -> MSG + state;
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaImplementsInterfaces(r1, Callable.class, Serializable.class);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertSerializableLambdaCharacteristics(r1);

        assertCallableBehavior(r1, MSG + state);

        Callable<String> deserializedR1 = roundtripSerialization(r1);
        assertEquals(r1.call(), deserializedR1.call());
    }

    public void testBadSerializableLambda() throws Exception {
        final Object state = new Object(); // Not Serializable
        Callable<String> r1 = (Callable<String> & Serializable) () -> "Hello world: " + state;
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertLambdaImplementsInterfaces(r1, Callable.class, Serializable.class);

        try {
            serializeObject(r1);
            fail();
        } catch (NotSerializableException expected) {
        }
    }

    public void testMultipleInterfaceLambda() throws Exception {
        Callable<String> r1 = (Callable<String> & MarkerInterface) () -> MSG;
        assertTrue(r1 instanceof MarkerInterface);
        assertGeneralLambdaClassCharacteristics(r1);
        assertLambdaMethodCharacteristics(r1, Callable.class);
        assertLambdaImplementsInterfaces(r1, Callable.class, MarkerInterface.class);
        assertNonSerializableLambdaCharacteristics(r1);

        assertCallableBehavior(r1, MSG);
    }

    private static void assertSerializableLambdaCharacteristics(Object r1) throws Exception {
        assertTrue(r1 instanceof Serializable);

        Object deserializedR1 = roundtripSerialization(r1);
        assertFalse(deserializedR1.equals(r1));
        assertNotSame(deserializedR1, r1);
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundtripSerialization(T r1) throws Exception {
        byte[] bytes = serializeObject(r1);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try (ObjectInputStream is = new ObjectInputStream(bais)) {
            return (T) is.readObject();
        }
    }

    private static <T> byte[] serializeObject(T r1) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
            os.writeObject(r1);
            os.flush();
        }
        return baos.toByteArray();
    }

    private static <T> void assertLambdaImplementsInterfaces(T r1, Class<?>... expectedInterfaces)
            throws Exception {
        Class<?> lambdaClass = r1.getClass();

        // Check directly implemented interfaces. Ordering is well-defined.
        Class<?>[] actualInterfaces = lambdaClass.getInterfaces();
        assertEquals(expectedInterfaces.length, actualInterfaces.length);
        List<Class<?>> actual = Arrays.asList(actualInterfaces);
        List<Class<?>> expected = Arrays.asList(expectedInterfaces);
        assertEquals(expected, actual);

        // Confirm that the only method declared on the lambda's class are those defined by
        // interfaces it implements. i.e. there's no additional public contract.
        Set<Method> declaredMethods = new HashSet<>();
        addNonStaticPublicMethods(lambdaClass, declaredMethods);
        Set<Method> expectedMethods = new HashSet<>();
        for (Class<?> interfaceClass : expectedInterfaces) {
            // Obtain methods declared by super-interfaces too.
            while (interfaceClass != null) {
                addNonStaticPublicMethods(interfaceClass, expectedMethods);
                interfaceClass = interfaceClass.getSuperclass();
            }
        }
        assertEquals(expectedMethods.size(), declaredMethods.size());

        // Check the method signatures are compatible.
        for (Method expectedMethod : expectedMethods) {
            Method actualMethod =
                    lambdaClass.getMethod(expectedMethod.getName(),
                            expectedMethod.getParameterTypes());
            assertEquals(expectedMethod.getReturnType(), actualMethod.getReturnType());
        }
    }

    private static void addNonStaticPublicMethods(Class<?> clazz, Set<Method> methodSet) {
        for (Method interfaceMethod : clazz.getDeclaredMethods()) {
            int modifiers = interfaceMethod.getModifiers();
            if ((!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))) {
                methodSet.add(interfaceMethod);
            }
        }
    }

    private static void assertNonSerializableLambdaCharacteristics(Object r1) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(baos)) {
            os.writeObject(r1);
            os.flush();
            fail();
        } catch (NotSerializableException expected) {
        }
    }

    private static void assertMultipleInstanceCharacteristics(Object r1, Object r2)
            throws Exception {

        // Unclear if any of this is *guaranteed* to be true.

        // Check the objects are not the same and do not equal. This could influence collection
        // behavior.
        assertNotSame(r1, r2);
        assertTrue(!r1.equals(r2));

        // Confirm the classes differ.
        Class<?> lambda1Class = r1.getClass();
        Class<?> lambda2Class = r2.getClass();
        assertNotSame(lambda1Class, lambda2Class);
    }

    private static void assertGeneralLambdaClassCharacteristics(Object r1) throws Exception {
        Class<?> lambdaClass = r1.getClass();

        // Lambda objects have classes that have names.
        assertNotNull(lambdaClass.getName());
        assertNotNull(lambdaClass.getSimpleName());
        assertNotNull(lambdaClass.getCanonicalName());

        // Lambda classes are "synthetic classes" that are not arrays.
        assertFalse(lambdaClass.isAnnotation());
        assertFalse(lambdaClass.isInterface());
        assertFalse(lambdaClass.isArray());
        assertFalse(lambdaClass.isEnum());
        assertFalse(lambdaClass.isPrimitive());
        assertTrue(lambdaClass.isSynthetic());
        assertNull(lambdaClass.getComponentType());

        // Expected modifiers
        int classModifiers = lambdaClass.getModifiers();
        assertTrue(Modifier.isFinal(classModifiers));

        // Unexpected modifiers
        assertFalse(Modifier.isPrivate(classModifiers));
        assertFalse(Modifier.isPublic(classModifiers));
        assertFalse(Modifier.isProtected(classModifiers));
        assertFalse(Modifier.isStatic(classModifiers));
        assertFalse(Modifier.isSynchronized(classModifiers));
        assertFalse(Modifier.isVolatile(classModifiers));
        assertFalse(Modifier.isTransient(classModifiers));
        assertFalse(Modifier.isNative(classModifiers));
        assertFalse(Modifier.isInterface(classModifiers));
        assertFalse(Modifier.isAbstract(classModifiers));
        assertFalse(Modifier.isStrict(classModifiers));

        // Check the classloader, inheritance hierarchy and package.
        assertSame(LambdaImplementationTest.class.getClassLoader(), lambdaClass.getClassLoader());
        assertSame(Object.class, lambdaClass.getSuperclass());
        assertSame(Object.class, lambdaClass.getGenericSuperclass());
        assertEquals(LambdaImplementationTest.class.getPackage(), lambdaClass.getPackage());

        // Check the implementation of the non-final public methods that all Objects possess.
        assertNotNull(r1.toString());
        assertTrue(r1.equals(r1));
        assertEquals(System.identityHashCode(r1), r1.hashCode());
    }

    private static <T> void assertLambdaMethodCharacteristics(T r1, Class<?> samInterfaceClass)
            throws Exception {
        // Find the single abstract method on the interface.
        Method singleAbstractMethod = null;
        for (Method method : samInterfaceClass.getDeclaredMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                singleAbstractMethod = method;
                break;
            }
        }
        assertNotNull(singleAbstractMethod);

        // Confirm the lambda implements the method as expected.
        Method implementationMethod = r1.getClass().getMethod(
                singleAbstractMethod.getName(), singleAbstractMethod.getParameterTypes());
        assertSame(singleAbstractMethod.getReturnType(), implementationMethod.getReturnType());
        assertSame(r1.getClass(), implementationMethod.getDeclaringClass());
        assertFalse(implementationMethod.isSynthetic());
        assertFalse(implementationMethod.isBridge());
        assertFalse(implementationMethod.isDefault());
    }

    private static String staticMethod() {
        return MSG;
    }

    private interface MarkerInterface {
    }

    private static <T> void assertCallableBehavior(Callable<T> r1, T expectedResult)
            throws Exception {
        assertEquals(expectedResult, r1.call());

        Method implCallMethod = r1.getClass().getDeclaredMethod("call");
        assertEquals(expectedResult, implCallMethod.invoke(r1));

        Method interfaceCallMethod = Callable.class.getDeclaredMethod("call");
        assertEquals(expectedResult, interfaceCallMethod.invoke(r1));
    }
}
