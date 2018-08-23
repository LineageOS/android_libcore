/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package java.lang.reflect;

import java.lang.annotation.Annotation;

@SuppressWarnings({"unchecked", "deprecation", "all"})
public abstract class Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration {

Executable() { throw new RuntimeException("Stub!"); }

@libcore.util.NonNull public abstract java.lang.Class<?> getDeclaringClass();

@libcore.util.NonNull public abstract java.lang.String getName();

public abstract int getModifiers();

@libcore.util.NonNull public abstract java.lang.reflect.TypeVariable<?>[] getTypeParameters();

@libcore.util.NonNull public abstract java.lang.Class<?>[] getParameterTypes();

public int getParameterCount() { throw new RuntimeException("Stub!"); }

@libcore.util.NonNull public java.lang.reflect.Type[] getGenericParameterTypes() { throw new RuntimeException("Stub!"); }

@libcore.util.NonNull public java.lang.reflect.Parameter[] getParameters() { throw new RuntimeException("Stub!"); }

@libcore.util.NonNull public abstract java.lang.Class<?>[] getExceptionTypes();

@libcore.util.NonNull public java.lang.reflect.Type[] getGenericExceptionTypes() { throw new RuntimeException("Stub!"); }

@libcore.util.NonNull public abstract java.lang.String toGenericString();

public boolean isVarArgs() { throw new RuntimeException("Stub!"); }

public boolean isSynthetic() { throw new RuntimeException("Stub!"); }

@libcore.util.NonNull public abstract java.lang.annotation.Annotation[][] getParameterAnnotations();

public <T extends java.lang.annotation.Annotation> T getAnnotation(java.lang.Class<T> annotationClass) { throw new RuntimeException("Stub!"); }

public <T extends java.lang.annotation.Annotation> T[] getAnnotationsByType(java.lang.Class<T> annotationClass) { throw new RuntimeException("Stub!"); }

@libcore.util.NonNull public java.lang.annotation.Annotation[] getDeclaredAnnotations() { throw new RuntimeException("Stub!"); }

public final boolean isAnnotationPresent(java.lang.Class<? extends java.lang.annotation.Annotation> annotationType) { throw new RuntimeException("Stub!"); }
}

