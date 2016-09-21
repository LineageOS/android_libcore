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

package libcore.java.lang.reflect;

import junit.framework.TestCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Tests for {@link Parameter}. For annotation-related tests see
 * {@link libcore.java.lang.reflect.annotations.AnnotatedElementParameterTest} and
 * {@link libcore.java.lang.reflect.annotations.ExecutableParameterTest}.
 */
public class ParameterTest extends TestCase {

    /**
     * A source annotation used to mark tests below with behavior that is highly dependent on
     * parameter metadata. It is intended to bring readers here for the following:
     *
     * <p>Unless the compiler supports (and is configured to enable) storage of metadata
     * for parameters, the runtime does not have access to the parameter name from the source and
     * some modifier information like "implicit" (AKA "mandated"), "synthetic" and "final".
     * These tests are currently expected to be compiled without requesting the metadata and can
     * only test the negative case without the metadata present (the expected, common case).
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    private @interface DependsOnParameterMetadata {}

    private static class SingleParameter {
        @SuppressWarnings("unused")
        SingleParameter(String p0) {}

        @SuppressWarnings("unused")
        void oneParameter(String p0) {}
    }

    public void testSingleParameterConstructor() throws Exception {
        Constructor<?> constructor = SingleParameter.class.getDeclaredConstructor(String.class);
        checkSingleStringParameter(constructor);
    }

    public void testSingleParameterMethod() throws Exception {
        Method method = SingleParameter.class.getDeclaredMethod("oneParameter", String.class);
        checkSingleStringParameter(method);
    }

    private static void checkSingleStringParameter(Executable executable) {
        ExecutableTestHelper helper = new ExecutableTestHelper(executable);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.String arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(String.class)
                .checkGetParameterizedType("class java.lang.String");
    }

    private static class GenericParameter {
        @SuppressWarnings("unused")
        GenericParameter(Function<String, Integer> p0) {}

        @SuppressWarnings("unused")
        void genericParameter(Function<String, Integer> p0) {}
    }

    public void testGenericParameterConstructor() throws Exception {
        Constructor<?> constructor = GenericParameter.class.getDeclaredConstructor(Function.class);
        checkGenericParameter(constructor);
    }

    public void testGenericParameterMethod() throws Exception {
        Method method = GenericParameter.class.getDeclaredMethod(
                "genericParameter", Function.class);
        checkGenericParameter(method);
    }

    private static void checkGenericParameter(Executable executable) {
        ExecutableTestHelper helper = new ExecutableTestHelper(executable);
        helper.checkStandardParametersBehavior()
                .checkParametersToString(
                        "[java.util.function.Function<java.lang.String, java.lang.Integer> arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(Function.class)
                .checkGetParameterizedType(
                        "java.util.function.Function<java.lang.String, java.lang.Integer>");
    }

    private static class TwoParameters {
        @SuppressWarnings("unused")
        TwoParameters(String p0, Integer p1) {}
        @SuppressWarnings("unused")
        void twoParameters(String p0, Integer p1) {}
    }

    public void testTwoParameterConstructor() throws Exception {
        Constructor<?> constructor =
                TwoParameters.class.getDeclaredConstructor(String.class, Integer.class);
        checkTwoParameters(constructor);
    }

    public void testTwoParameterMethod() throws Exception {
        Method method = TwoParameters.class.getDeclaredMethod(
                "twoParameters", String.class, Integer.class);
        checkTwoParameters(method);
    }

    private static void checkTwoParameters(Executable executable) {
        ExecutableTestHelper helper = new ExecutableTestHelper(executable);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.String arg0, java.lang.Integer arg1]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(String.class)
                .checkGetParameterizedType("class java.lang.String");

        helper.getParameterTestHelper(1)
                .checkGetType(Integer.class)
                .checkGetParameterizedType("class java.lang.Integer");
    }

    private static class FinalParameter {
        @SuppressWarnings("unused")
        FinalParameter(final String p0) {}
        @SuppressWarnings("unused")
        void finalParameter(final String p0) {}
    }

    public void testFinalParameterConstructor() throws Exception {
        Constructor<?> constructor = FinalParameter.class.getDeclaredConstructor(String.class);
        checkFinalParameter(constructor);
    }

    public void testFinalParameterMethod() throws Exception {
        Method method = FinalParameter.class.getDeclaredMethod("finalParameter", String.class);
        checkFinalParameter(method);
    }

    private static void checkFinalParameter(Executable executable) {
        ExecutableTestHelper helper = new ExecutableTestHelper(executable);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.String arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(String.class)
                .checkGetParameterizedType("class java.lang.String");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0).checkModifiers(Modifier.FINAL);
    }

    /**
     * An inner class, used for checking compiler-inserted parameters: The first parameter is an
     * instance of the surrounding class.
     */
    private class InnerClass {
        @SuppressWarnings("unused")
        public InnerClass() {}
        @SuppressWarnings("unused")
        public InnerClass(String p1) {}
        @SuppressWarnings("unused")
        public InnerClass(Function<String, Integer> p1) {}
    }

    public void testInnerClassSingleParameter() throws Exception {
        Constructor<?> constructor =
                InnerClass.class.getDeclaredConstructor(ParameterTest.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(constructor);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[libcore.java.lang.reflect.ParameterTest arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(ParameterTest.class)
                .checkGetParameterizedType("class libcore.java.lang.reflect.ParameterTest");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0)
        //         .checkModifiers(32784)
        //         .checkImplicitAndSynthetic(true, false);
        // i.e. 32784 == Modifier.MANDATED & Modifier.FINAL
    }

    public void testInnerClassTwoParameters() throws Exception {
        Constructor<?> constructor =
                InnerClass.class.getDeclaredConstructor(ParameterTest.class, String.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(constructor);
        helper.checkStandardParametersBehavior()
                .checkParametersToString(
                        "[libcore.java.lang.reflect.ParameterTest arg0, java.lang.String arg1]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(ParameterTest.class)
                .checkGetParameterizedType("class libcore.java.lang.reflect.ParameterTest");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0)
        //         .checkModifiers(32784)
        //         .checkImplicitAndSynthetic(true, false);
        // i.e. 32784 == Modifier.MANDATED & Modifier.FINAL


        helper.getParameterTestHelper(1)
                .checkGetType(String.class)
                .checkGetParameterizedType("class java.lang.String");
    }

    public void testInnerClassGenericParameter() throws Exception {
        Constructor<?> constructor =
                InnerClass.class.getDeclaredConstructor(
                        ParameterTest.class, Function.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(constructor);
        helper.checkStandardParametersBehavior()
                .checkParametersToString(
                        "[libcore.java.lang.reflect.ParameterTest arg0, "
                                + "java.util.function.Function arg1]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(ParameterTest.class)
                .checkGetParameterizedType("class libcore.java.lang.reflect.ParameterTest");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0)
        //         .checkModifiers(32784)
        //         .checkImplicitAndSynthetic(true, false);
        // i.e. 32784 == Modifier.MANDATED & Modifier.FINAL

        helper.getParameterTestHelper(1)
                .checkGetType(Function.class)
                .checkGetParameterizedType("interface java.util.function.Function");

        // The non-genericised string above is probably the result of a bug due to a mismatch
        // between the generic signature for the constructor (which suggests a single parameter)
        // and the actual parameters (which suggests two).
    }

    @SuppressWarnings("unused")
    enum TestEnum { ONE, TWO }

    /**
     * Enums are a documented example of a type of class with synthetic constructor parameters and
     * generated methods. This test may be brittle as it may rely on the compiler's implementation
     * of enums.
     */
    @DependsOnParameterMetadata
    public void testEnumConstructor() throws Exception {
        Constructor<?> constructor = TestEnum.class.getDeclaredConstructor(String.class, int.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(constructor);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.String arg0, int arg1]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(String.class)
                .checkGetParameterizedType("class java.lang.String");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0)
        //         .checkModifiers(4096)
        //         .checkImplicitAndSynthetic(false, true);
        // i.e. 4096 == Modifier.SYNTHETIC

        helper.getParameterTestHelper(1)
                .checkGetType(int.class)
                .checkGetParameterizedType("int");
    }

    public void testEnumValueOf() throws Exception {
        Method method = TestEnum.class.getDeclaredMethod("valueOf", String.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(method);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.String arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(String.class)
                .checkGetParameterizedType("class java.lang.String");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0)
        //         .checkModifiers(32768)
        //         .checkImplicitAndSynthetic(true, false);
        // i.e. 32768 == Modifier.MANDATED
    }

    private static class SingleVarArgs {
        @SuppressWarnings("unused")
        SingleVarArgs(String... p0) {}

        @SuppressWarnings("unused")
        void varArgs(String... p0) {}
    }

    public void testSingleVarArgsConstructor() throws Exception {
        Constructor<?> constructor = SingleVarArgs.class.getDeclaredConstructor(String[].class);
        checkSingleVarArgsParameter(constructor);
    }

    public void testSingleVarArgsMethod() throws Exception {
        Method method = SingleVarArgs.class.getDeclaredMethod("varArgs", String[].class);
        checkSingleVarArgsParameter(method);
    }

    private static void checkSingleVarArgsParameter(Executable executable) {
        ExecutableTestHelper helper = new ExecutableTestHelper(executable);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.String... arg0]")
                .checkParametersMetadataNotAvailable();


        helper.getParameterTestHelper(0)
                .checkGetType(String[].class)
                .checkIsVarArg(true)
                .checkGetParameterizedType("class [Ljava.lang.String;");
    }

    private static class MixedVarArgs {
        @SuppressWarnings("unused")
        MixedVarArgs(Integer[] p0, String... p1) {}
        @SuppressWarnings("unused")
        void both(Integer[] p0, String... p1) {}
    }

    public void testMixedVarArgsConstructor() throws Exception {
        Constructor<?> constructor =
                MixedVarArgs.class.getDeclaredConstructor(Integer[].class, String[].class);
        checkMixedVarArgsParameter(constructor);
    }

    public void testMixedVarArgsMethod() throws Exception {
        Method method = MixedVarArgs.class.getDeclaredMethod("both", Integer[].class, String[].class);
        checkMixedVarArgsParameter(method);
    }

    private static void checkMixedVarArgsParameter(Executable executable) {
        ExecutableTestHelper helper = new ExecutableTestHelper(executable);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.Integer[] arg0, java.lang.String... arg1]")
                .checkParametersMetadataNotAvailable();

        helper.getParameterTestHelper(0)
                .checkGetType(Integer[].class)
                .checkIsVarArg(false)
                .checkGetParameterizedType("class [Ljava.lang.Integer;");

        helper.getParameterTestHelper(1)
                .checkGetType(String[].class)
                .checkIsVarArg(true)
                .checkGetParameterizedType("class [Ljava.lang.String;");
    }

    private static class NonVarArgs {
        @SuppressWarnings("unused")
        NonVarArgs(Integer[] p0) {}
        @SuppressWarnings("unused")
        void notVarArgs(Integer[] p0) {}
    }

    public void testNonVarsArgsConstructor() throws Exception {
        Constructor<?> constructor = NonVarArgs.class.getDeclaredConstructor(Integer[].class);
        checkNonVarsArgsParameter(constructor);
    }

    public void testNonVarsArgsMethod() throws Exception {
        Method method = NonVarArgs.class.getDeclaredMethod("notVarArgs", Integer[].class);
        checkNonVarsArgsParameter(method);
    }

    private static void checkNonVarsArgsParameter(Executable executable) {
        ExecutableTestHelper helper = new ExecutableTestHelper(executable);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[java.lang.Integer[] arg0]")
                .checkParametersMetadataNotAvailable();

        helper.getParameterTestHelper(0)
                .checkGetType(Integer[].class)
                .checkIsVarArg(false)
                .checkGetParameterizedType("class [Ljava.lang.Integer;");
    }

    public void testAnonymousClassConstructor() throws Exception {
        Class<?> clazz = getAnonymousClassWith1ParameterConstructor();
        Constructor<?> constructor = clazz.getDeclaredConstructor(ParameterTest.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(constructor);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[libcore.java.lang.reflect.ParameterTest arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(ParameterTest.class)
                .checkGetParameterizedType("class libcore.java.lang.reflect.ParameterTest");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0)
        //         .checkModifiers(32784)
        //         .checkImplicitAndSynthetic(true, false);
        // i.e. 32784 == Modifier.MANDATED & Modifier.FINAL
    }

    private Class<?> getAnonymousClassWith1ParameterConstructor() {
        // Deliberately not implemented with a lambda. Do not refactor.
        Callable<String> anonymousClassObject = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return ParameterTest.this.outerClassMethod();
            }
        };
        return anonymousClassObject.getClass();
    }

    public void testMethodClassConstructor() throws Exception {
        Class<?> clazz = getMethodClassWith1ImplicitParameterConstructor();
        Constructor<?> constructor = clazz.getDeclaredConstructor(ParameterTest.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(constructor);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[libcore.java.lang.reflect.ParameterTest arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(ParameterTest.class)
                .checkGetParameterizedType("class libcore.java.lang.reflect.ParameterTest");

        // If parameter metadata were included, this would be the expected:
        // helper.getParameterTestHelper(0)
        //         .checkModifiers(32784)
        //         .checkImplicitAndSynthetic(true, false);
        // i.e. 32784 == Modifier.MANDATED & Modifier.FINAL
    }

    private Class<?> getMethodClassWith1ImplicitParameterConstructor() {
        class MethodClass {
            MethodClass() {
                ParameterTest.this.outerClassMethod();
            }
        }
        return MethodClass.class;
    }

    // This behavior is likely to be quite brittle and may not be specified.
    public void testLambdaClassConstructor() throws Exception {
        Class<?> anonymousClass = getLambdaClassWith1ParameterConstructor();
        Constructor<?> constructor = anonymousClass.getDeclaredConstructor(ParameterTest.class);

        ExecutableTestHelper helper = new ExecutableTestHelper(constructor);
        helper.checkStandardParametersBehavior()
                .checkParametersToString("[libcore.java.lang.reflect.ParameterTest arg0]")
                .checkParametersMetadataNotAvailable()
                .checkParametersNoVarArgs();

        helper.getParameterTestHelper(0)
                .checkGetType(ParameterTest.class)
                .checkGetParameterizedType("class libcore.java.lang.reflect.ParameterTest");

        // Unclear what the implicit / synthetic parameter behavior should be.
    }

    private Class<?> getLambdaClassWith1ParameterConstructor() {
        return ((Callable<String>) ParameterTest.this::outerClassMethod).getClass();
    }

    private static class NonIdenticalParameters {
        void method0(String p1) {}
        void method1(String p1) {}
    }

    public void testEquals_checksExecutable() throws Exception {
        Method method0 = NonIdenticalParameters.class.getDeclaredMethod("method0", String.class);
        Method method1 = NonIdenticalParameters.class.getDeclaredMethod("method1", String.class);
        Parameter method0P0 = method0.getParameters()[0];
        Parameter method1P0 = method1.getParameters()[0];
        assertFalse(method0P0.equals(method1P0));
        assertFalse(method1P0.equals(method0P0));
        assertTrue(method0P0.equals(method0P0));
    }

    /** A non-static method that exists to be called by inner classes, lambdas, etc. */
    private String outerClassMethod() {
        return "Howdy";
    }

    private static class ExecutableTestHelper {
        private final Executable executable;

        ExecutableTestHelper(Executable executable) {
            this.executable = executable;
        }

        ExecutableTestHelper checkParametersToString(String expectedString) {
            assertEquals(expectedString, Arrays.toString(executable.getParameters()));
            return this;
        }

        /**
         * Combines checks that should be true of any result from
         * {@link Executable#getParameters()}
         */
        ExecutableTestHelper checkStandardParametersBehavior() {
            return checkGetParametersClonesArray()
                    .checkParametersGetDeclaringExecutable()
                    .checkParametersEquals()
                    .checkParametersHashcode();
        }

        ExecutableTestHelper checkParametersGetDeclaringExecutable() {
            for (Parameter p : executable.getParameters()) {
                assertSame(executable, p.getDeclaringExecutable());
            }
            return this;
        }

        ExecutableTestHelper checkGetParametersClonesArray() {
            Parameter[] parameters1 = executable.getParameters();
            Parameter[] parameters2 = executable.getParameters();
            assertNotSame(parameters1, parameters2);

            assertEquals(parameters1.length, parameters2.length);
            for (int i = 0; i < parameters1.length; i++) {
                assertSame(parameters1[i], parameters2[i]);
            }
            return this;
        }

        ExecutableTestHelper checkParametersEquals() {
            Parameter[] parameters = executable.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                assertEquals(parameters[i], parameters[i]);
                if (i > 0) {
                    assertFalse(parameters[0].equals(parameters[i]));
                    assertFalse(parameters[i].equals(parameters[0]));
                }
            }
            return this;
        }

        ExecutableTestHelper checkParametersHashcode() {
            for (Parameter parameter : executable.getParameters()) {
                // Not much to assert. Just call the method and check it is consistent.
                assertEquals(parameter.hashCode(), parameter.hashCode());
            }
            return this;
        }

        @DependsOnParameterMetadata
        ExecutableTestHelper checkParametersMetadataNotAvailable() {
            ParameterTestHelper[] parameterTestHelpers = getParameterTestHelpers();
            for (int i = 0; i < parameterTestHelpers.length; i++) {
                ParameterTestHelper parameterTestHelper = parameterTestHelpers[i];
                parameterTestHelper.checkName(false, "arg" + i)
                        .checkImplicitAndSynthetic(false, false)
                        .checkModifiers(0);
            }
            return this;
        }

        /**
         * Checks that non of the parameters return {@code true} for {@link Parameter#isVarArgs()}.
         */
        ExecutableTestHelper checkParametersNoVarArgs() {
            for (ParameterTestHelper parameterTestHelper : getParameterTestHelpers()) {
                parameterTestHelper.checkIsVarArg(false);
            }
            return this;
        }

        ParameterTestHelper getParameterTestHelper(int index) {
            return new ParameterTestHelper(executable.getParameters()[index]);
        }

        private ParameterTestHelper[] getParameterTestHelpers() {
            final int parameterCount = executable.getParameterCount();
            ParameterTestHelper[] parameterTestHelpers = new ParameterTestHelper[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                parameterTestHelpers[i] = getParameterTestHelper(i);
            }
            return parameterTestHelpers;
        }

        private static class ParameterTestHelper {
            private final Parameter parameter;

            ParameterTestHelper(Parameter parameter) {
                this.parameter = parameter;
            }

            ParameterTestHelper checkGetType(Class<?> expectedType) {
                assertEquals(expectedType, parameter.getType());
                return this;
            }

            @DependsOnParameterMetadata
            ParameterTestHelper checkName(boolean expectedIsNamePresent, String expectedName) {
                assertEquals(expectedIsNamePresent, parameter.isNamePresent());
                assertEquals(expectedName, parameter.getName());
                return this;
            }

            @DependsOnParameterMetadata
            ParameterTestHelper checkModifiers(int expectedModifiers) {
                assertEquals(expectedModifiers, parameter.getModifiers());
                return this;
            }

            ParameterTestHelper checkGetParameterizedType(String expectedParameterizedTypeString) {
                assertEquals(
                        expectedParameterizedTypeString,
                        parameter.getParameterizedType().toString());
                return this;
            }

            ParameterTestHelper checkImplicitAndSynthetic(
                    boolean expectedIsImplicit, boolean expectedIsSynthetic) {
                assertEquals(expectedIsImplicit, parameter.isImplicit());
                assertEquals(expectedIsSynthetic, parameter.isSynthetic());
                return this;
            }

            ParameterTestHelper checkIsVarArg(boolean expectedIsVarArg) {
                assertEquals(expectedIsVarArg, parameter.isVarArgs());
                return this;
            }
        }
    }
}
