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

package dalvik.system;

import java.lang.invoke.MethodType;

/**
 * Provides typed (read-only) access to method arguments and a slot to store a return value.
 *
 * Used to implement method handle transforms. See {@link java.lang.invoke.Transformers}.
 *
 * @hide
 */
public class EmulatedStackFrame {
    /**
     * The type of this stack frame, i.e, the types of its arguments and the type of its
     * return value.
     */
    private final MethodType type;

    /**
     * All reference arguments and reference return values that belong to this argument array.
     *
     * If the return type is a reference, it will be the last element of this array.
     */
    private final Object[] references;

    /**
     * Contains all primitive values on the stack. Primitive values always take 4 or 8 bytes of
     * space and all {@code short}, {@code char} and {@code boolean} arguments are promoted to ints.
     *
     * Reference values do not appear on the stack frame but they appear (in order)
     * in the {@code references} array. No additional slots or space for reference arguments or
     * return values are reserved in the stackFrame.
     *
     * By convention, if the return value is a primitive, it will occupy the last 4 or 8 bytes
     * of the stack frame, depending on the type.
     *
     * The size of this array is known at the time of creation of this {@code EmulatedStackFrame}
     * and is determined by the {@code MethodType} of the frame.
     *
     * Example :
     * <pre>
     *     Function : String foo(String a, String b, int c, long d) { }
     *
     *     EmulatedStackFrame :
     *     references = { a, b, [return_value] }
     *     stackFrame = { c0, c1, c2, c3, d0, d1, d2, d3, d4, d5, d6, d7 }
     *
     *     Function : int foo(String a)
     *
     *     EmulatedStackFrame :
     *     references = { a }
     *     stackFrame = { rv0, rv1, rv2, rv3 }  // rv is the return value.
     *
     * </pre>
     *
     */
    private final byte[] stackFrame;

    private EmulatedStackFrame(MethodType type, Object[] references, byte[] stackFrame) {
        this.type = type;
        this.references = references;
        this.stackFrame = stackFrame;
    }

    /**
     * Represents a range of arguments on an {@code EmulatedStackFrame}.
     */
    public static final class Range {
        public final int referencesStart;
        public final int numReferences;

        public final int stackFrameStart;
        public final int numBytes;

        private Range(int referencesStart, int numReferences, int stackFrameStart, int numBytes) {
            this.referencesStart = referencesStart;
            this.numReferences = numReferences;
            this.stackFrameStart = stackFrameStart;
            this.numBytes = numBytes;
        }


        public static Range of(MethodType frameType, int startArg, int endArg) {
            final Class<?>[] ptypes = frameType.ptypes();

            int referencesStart = 0;
            int numReferences = 0;
            int stackFrameStart = 0;
            int numBytes = 0;

            for (int i = 0; i < startArg; ++i) {
                Class<?> cl = ptypes[i];
                if (!cl.isPrimitive()) {
                    referencesStart++;
                } else {
                    stackFrameStart += getSize(cl);
                }
            }

            for (int i = startArg; i < endArg; ++i) {
                Class<?> cl = ptypes[i];
                if (!cl.isPrimitive()) {
                    numReferences++;
                } else {
                    numBytes += getSize(cl);
                }
            }

            return new Range(referencesStart, numReferences, stackFrameStart, numBytes);
        }
    }

    /**
     * Creates an emulated stack frame for a given {@code MethodType}.
     */
    public static EmulatedStackFrame create(MethodType frameType) {
        int numRefs = 0;
        int frameSize = 0;
        for (Class<?> ptype : frameType.ptypes()) {
            if (!ptype.isPrimitive()) {
                numRefs++;
            } else {
                frameSize += getSize(ptype);
            }
        }

        final Class<?> rtype = frameType.rtype();
        if (!rtype.isPrimitive()) {
            numRefs++;
        } else {
            frameSize += getSize(rtype);
        }

        return new EmulatedStackFrame(frameType, new Object[numRefs], new byte[frameSize]);
    }

    /**
     * Sets the {@code idx} to {@code reference}. Type checks are performed.
     */
    public void setReference(int idx, Object reference) {
        final Class<?>[] ptypes = type.ptypes();
        if (idx < 0 || idx >= ptypes.length) {
            throw new IllegalArgumentException("Invalid index: " + idx);
        }

        if (!ptypes[idx].isInstance(reference)) {
            throw new IllegalStateException("reference is not of type: " + type.ptypes()[idx]);
        }

        references[idx] = reference;
    }

    /**
     * Gets the reference at {@code idx}, checking that it's of type {@code referenceType}.
     */
    public <T> T getReference(int idx, Class<T> referenceType) {
        if (referenceType != type.ptypes()[idx]) {
            throw new IllegalArgumentException("Argument: " + idx +
                    " is not of type " + referenceType);
        }

        return (T) references[idx];
    }

    /**
     * Copies a specified range of arguments, given by {@code fromRange} to a specified
     * EmulatedStackFrame {@code other}, with references starting at {@code referencesStart}
     * and primitives starting at {@code primitivesStart}.
     */
    public void copyRangeTo(EmulatedStackFrame other, Range fromRange, int referencesStart,
                            int primitivesStart) {
        if (fromRange.numReferences > 0) {
            System.arraycopy(references, fromRange.referencesStart,
                    other.references, referencesStart, fromRange.numReferences);
        }

        if (fromRange.numBytes > 0) {
            System.arraycopy(stackFrame, fromRange.stackFrameStart,
                    other.stackFrame, primitivesStart, fromRange.numBytes);
        }
    }

    /**
     * Copies the return value from this stack frame to {@code other}.
     */
    public void copyReturnValueTo(EmulatedStackFrame other) {
        final Class<?> returnType = type.returnType();
        if (!returnType.isPrimitive()) {
            other.references[other.references.length - 1] = references[references.length - 1];
        } else if (!is64BitPrimitive(returnType)) {
            System.arraycopy(stackFrame, stackFrame.length - 4,
                    other.stackFrame, other.stackFrame.length - 4, 4);
        } else {
            System.arraycopy(stackFrame, stackFrame.length - 8,
                    other.stackFrame, other.stackFrame.length - 8, 8);
        }
    }

    /**
     * Returns true iff. the input {@code type} needs 64 bits (8 bytes) of storage on an
     * {@code EmulatedStackFrame}.
     */
    private static boolean is64BitPrimitive(Class<?> type) {
        return type == double.class || type == long.class;
    }

    /**
     * Returns the size (in bytes) occupied by a given primitive type on an
     * {@code EmulatedStackFrame}.
     */
    private static int getSize(Class<?> type) {
        if (!type.isPrimitive()) {
            throw new IllegalArgumentException("type.isPrimitive() == false: " + type);
        }

        if (is64BitPrimitive(type)) {
            return 8;
        } else {
            return 4;
        }
    }
}
