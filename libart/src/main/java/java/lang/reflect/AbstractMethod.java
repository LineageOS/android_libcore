/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2012 The Android Open Source Project
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

package java.lang.reflect;

import com.android.dex.Dex;

import java.lang.annotation.Annotation;
import libcore.reflect.GenericSignatureParser;
import libcore.reflect.ListOfTypes;
import libcore.reflect.Types;
import libcore.util.EmptyArray;

/**
 * This class represents an abstract method. Abstract methods are either methods or constructors.
 * @hide
 */
public abstract class AbstractMethod extends Executable {
    /** Bits encoding access (e.g. public, private) as well as other runtime specific flags */
    @SuppressWarnings("unused") // set by runtime
    private int accessFlags;

    /**
     * The ArtMethod associated with this Method, required for dispatching due to entrypoints
     * Classloader is held live by the declaring class.
     * Hidden to workaround b/16828157.
     * @hide
     */
    @SuppressWarnings("unused") // set by runtime
    private long artMethod;

    /** Method's declaring class */
    @SuppressWarnings("unused") // set by runtime
    private Class<?> declaringClass;

    /** Overriden method's declaring class (same as declaringClass unless declaringClass
     * is a proxy class)
     */
    @SuppressWarnings("unused") // set by runtime
    private Class<?> declaringClassOfOverriddenMethod;

    /** The method index of this method within its defining dex file */
    @SuppressWarnings("unused") // set by runtime
    private int dexMethodIndex;

    /**
     * Hidden to workaround b/16828157.
     * @hide
     */
    protected AbstractMethod() {
    }

    /**
     * We insert native method stubs for abstract methods so we don't have to
     * check the access flags at the time of the method call.  This results in
     * "native abstract" methods, which can't exist.  If we see the "abstract"
     * flag set, clear the "native" flag.
     *
     * We also move the DECLARED_SYNCHRONIZED flag into the SYNCHRONIZED
     * position, because the callers of this function are trying to convey
     * the "traditional" meaning of the flags to their callers.
     */
    private static int fixMethodFlags(int flags) {
        if ((flags & Modifier.ABSTRACT) != 0) {
            flags &= ~Modifier.NATIVE;
        }
        flags &= ~Modifier.SYNCHRONIZED;
        int ACC_DECLARED_SYNCHRONIZED = 0x00020000;
        if ((flags & ACC_DECLARED_SYNCHRONIZED) != 0) {
            flags |= Modifier.SYNCHRONIZED;
        }
        return flags & 0xffff;  // mask out bits not used by Java
    }

    // Overrides {@link Executable#getModifiers()} - for ART behavior see fixMethodFlags().
    @Override
    public int getModifiers() {
        return fixMethodFlags(accessFlags);
    }

    // Overrides {@link Executable#isSynthetic()} - we can do it cheaply here.
    @Override
    public boolean isSynthetic() {
        return (accessFlags & Modifier.SYNTHETIC) != 0;
    }

    // Overrides {@link Executable#isVarArgs()} - we can do it cheaply here.
    @Override
    public boolean isVarArgs() {
        return (accessFlags & Modifier.VARARGS) != 0;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Returns an array of {@code Class} objects associated with the parameter types of this
     * abstract method. If the method was declared with no parameters, an
     * empty array will be returned.
     *
     * @return the parameter types
     */
    @Override
    public Class<?>[] getParameterTypes() {
        Dex dex = declaringClassOfOverriddenMethod.getDex();
        short[] types = dex.parameterTypeIndicesFromMethodIndex(dexMethodIndex);
        if (types.length == 0) {
            return EmptyArray.CLASS;
        }
        Class<?>[] parametersArray = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            // Note, in the case of a Proxy the dex cache types are equal.
            parametersArray[i] = declaringClassOfOverriddenMethod.getDexCacheType(dex, types[i]);
        }
        return parametersArray;
    }

    @Override
    public int getParameterCount() {
        Dex dex = declaringClassOfOverriddenMethod.getDex();
        short[] types = dex.parameterTypeIndicesFromMethodIndex(dexMethodIndex);
        return types.length;
    }

    @Override
    public Type[] getGenericParameterTypes() {
        return Types.getTypeArray(
                getMethodOrConstructorGenericInfoInternal().genericParameterTypes, false);
    }

    @Override
    public Type[] getGenericExceptionTypes() {
        return Types.getTypeArray(
                getMethodOrConstructorGenericInfoInternal().genericExceptionTypes, false);
    }

    @Override public native Annotation[] getDeclaredAnnotations();

    @Override public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            throw new NullPointerException("annotationType == null");
        }
        return isAnnotationPresentNative(annotationType);
    }
    private native boolean isAnnotationPresentNative(Class<? extends Annotation> annotationType);

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException("annotationClass == null");
        }
        return getAnnotationNative(annotationClass);
    }
    private native <T extends Annotation> T getAnnotationNative(Class<T> annotationClass);

    @Override
    public Annotation[][] getParameterAnnotations() {
        Annotation[][] parameterAnnotations = getParameterAnnotationsNative();
        if (parameterAnnotations == null) {
            parameterAnnotations = new Annotation[getParameterTypes().length][0];
        }
        return parameterAnnotations;
    }
    private native Annotation[][] getParameterAnnotationsNative();

    /**
     * @hide
     */
    public final int getAccessFlags() {
        return accessFlags;
    }

    static final class GenericInfo {
        final ListOfTypes genericExceptionTypes;
        final ListOfTypes genericParameterTypes;
        final Type genericReturnType;
        final TypeVariable<?>[] formalTypeParameters;

        GenericInfo(ListOfTypes exceptions, ListOfTypes parameters, Type ret,
                    TypeVariable<?>[] formal) {
            genericExceptionTypes = exceptions;
            genericParameterTypes = parameters;
            genericReturnType = ret;
            formalTypeParameters = formal;
        }
    }

    boolean hasGenericInformationInternal() {
        return getSignatureAnnotation() != null;
    }

    /**
     * Returns generic information associated with this method/constructor member.
     */
    final GenericInfo getMethodOrConstructorGenericInfoInternal() {
        String signatureAttribute = getSignatureAttribute();
        Class<?>[] exceptionTypes = this.getExceptionTypes();
        GenericSignatureParser parser =
            new GenericSignatureParser(this.getDeclaringClass().getClassLoader());
        if (this instanceof Method) {
            parser.parseForMethod(this, signatureAttribute, exceptionTypes);
        } else {
            parser.parseForConstructor(this, signatureAttribute, exceptionTypes);
        }
        return new GenericInfo(parser.exceptionTypes, parser.parameterTypes,
                               parser.returnType, parser.formalTypeParameters);
    }

    private String getSignatureAttribute() {
        String[] annotation = getSignatureAnnotation();
        if (annotation == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String s : annotation) {
            result.append(s);
        }
        return result.toString();
    }
    private native String[] getSignatureAnnotation();

    final boolean equalNameAndParametersInternal(Method m) {
        return getName().equals(m.getName()) && equalMethodParameters(m.getParameterTypes());
    }

    private boolean equalMethodParameters(Class<?>[] params) {
        Dex dex = declaringClassOfOverriddenMethod.getDex();
        short[] types = dex.parameterTypeIndicesFromMethodIndex(dexMethodIndex);
        if (types.length != params.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (declaringClassOfOverriddenMethod.getDexCacheType(dex, types[i]) != params[i]) {
                return false;
            }
        }
        return true;
    }

    final int compareMethodParametersInternal(Class<?>[] params) {
        Dex dex = declaringClassOfOverriddenMethod.getDex();
        short[] types = dex.parameterTypeIndicesFromMethodIndex(dexMethodIndex);
        int length = Math.min(types.length, params.length);
        for (int i = 0; i < length; i++) {
            Class<?> aType = declaringClassOfOverriddenMethod.getDexCacheType(dex, types[i]);
            Class<?> bType = params[i];
            if (aType != bType) {
                int comparison = aType.getName().compareTo(bType.getName());
                if (comparison != 0) {
                    return comparison;
                }
            }
        }
        return types.length - params.length;
    }

    final String getMethodNameInternal() {
        Dex dex = declaringClassOfOverriddenMethod.getDex();
        int nameIndex = dex.nameIndexFromMethodIndex(dexMethodIndex);
        return declaringClassOfOverriddenMethod.getDexCacheString(dex, nameIndex);
    }

    final Class<?> getMethodReturnTypeInternal() {
        Dex dex = declaringClassOfOverriddenMethod.getDex();
        int returnTypeIndex = dex.returnTypeIndexFromMethodIndex(dexMethodIndex);
        // Note, in the case of a Proxy the dex cache types are equal.
        return declaringClassOfOverriddenMethod.getDexCacheType(dex, returnTypeIndex);
    }

    /** A cheap implementation for {@link Method#isDefault()}. */
    final boolean isDefaultMethodInternal() {
        return (accessFlags & Modifier.DEFAULT) != 0;
    }

    /** A cheap implementation for {@link Method#isBridge()}. */
    final boolean isBridgeMethodInternal() {
        return (accessFlags & Modifier.BRIDGE) != 0;
    }
}
