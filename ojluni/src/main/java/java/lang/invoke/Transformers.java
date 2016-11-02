/*
 * Copyright 2016 Google Inc.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Google designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Google in the LICENSE file that accompanied this code.
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
 */

package java.lang.invoke;

import dalvik.system.EmulatedStackFrame;

import java.lang.reflect.Method;

/**
 * @hide Public for testing only.
 */
public class Transformers {
    private Transformers() {}

    static {
        try {
            TRANSFORM_INTERNAL = MethodHandle.class.getDeclaredMethod("transformInternal",
                    EmulatedStackFrame.class);
        } catch (NoSuchMethodException nsme) {
            throw new AssertionError();
        }
    }

    /**
     * Method reference to the private {@code MethodHandle.transformInternal} method. This is
     * cached here because it's the point of entry for all transformers.
     */
    private static final Method TRANSFORM_INTERNAL;

    /** @hide */
    public static abstract class Transformer extends MethodHandle implements Cloneable {
        protected Transformer(MethodType type) {
            super(TRANSFORM_INTERNAL.getArtMethod(), MethodHandle.INVOKE_TRANSFORM, type);
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    /**
     * A method handle that always throws an exception of a specified type.
     *
     * The handle declares a nominal return type, which is immaterial to the execution
     * of the handle because it never returns.
     *
     * @hide
     */
    public static class AlwaysThrow extends Transformer {
        private final Class<? extends Throwable> exceptionType;

        public AlwaysThrow(Class<?> nominalReturnType, Class<? extends  Throwable> exType) {
            super(MethodType.methodType(nominalReturnType, exType));
            this.exceptionType = exType;
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            throw emulatedStackFrame.getReference(0, exceptionType);
        }
    }

    /**
     * Implements {@code MethodHandles.dropArguments}.
     */
    public static class DropArguments extends Transformer {
        private final MethodHandle delegate;

        private final EmulatedStackFrame.Range range1;

        /**
         * Note that {@code range2} will be null if the arguments that are being dropped
         * are the last {@code n}.
         */
        /* @Nullable */ private final EmulatedStackFrame.Range range2;

        public DropArguments(MethodType type, MethodHandle delegate,
                             int startPos, int numDropped) {
            super(type);

            this.delegate = delegate;

            // We pre-calculate the ranges of values we have to copy through to the delegate
            // handle at the time of instantiation so that the actual invoke is performant.
            this.range1 = EmulatedStackFrame.Range.of(type, 0, startPos);
            final int numArgs = type.ptypes().length;
            if (startPos + numDropped < numArgs) {
                this.range2 = EmulatedStackFrame.Range.of(type, startPos + numDropped, numArgs);
            } else {
                this.range2 = null;
            }
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            EmulatedStackFrame calleeFrame = EmulatedStackFrame.create(delegate.type());

            emulatedStackFrame.copyRangeTo(calleeFrame, range1,
                    0 /* referencesStart */, 0 /* stackFrameStart */);

            if (range2 != null) {
                final int referencesStart = range1.numReferences;
                final int stackFrameStart = range1.numBytes;

                emulatedStackFrame.copyRangeTo(calleeFrame, range2,
                        referencesStart, stackFrameStart);
            }

            delegate.invoke(calleeFrame);
            calleeFrame.copyReturnValueTo(emulatedStackFrame);
        }
    }

    /**
     * Implements {@code MethodHandles.catchException}.
     */
    public static class CatchException extends Transformer {
        private final MethodHandle target;
        private final MethodHandle handler;
        private final Class<?> exType;

        private final EmulatedStackFrame.Range handlerArgsRange;

        public CatchException(MethodHandle target, MethodHandle handler, Class<?> exType) {
            super(target.type());

            this.target = target;
            this.handler = handler;
            this.exType = exType;

            // We only copy the first "count" args, dropping others if required. Note that
            // we subtract one because the first handler arg is the exception thrown by the
            // target.
            handlerArgsRange = EmulatedStackFrame.Range.of(target.type(), 0,
                    (handler.type().parameterCount() - 1));
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            try {
                target.invoke(emulatedStackFrame);
            } catch (Throwable th) {
                if (th.getClass() == exType) {
                    // We've gotten an exception of the appropriate type, so we need to call
                    // the handler. Create a new frame of the appropriate size.
                    EmulatedStackFrame fallback = EmulatedStackFrame.create(handler.type());

                    // The first argument to the handler is the actual exception.
                    fallback.setReference(0, th);

                    // We then copy other arguments that need to be passed through to the handler.
                    // Note that we might drop arguments at the end, if needed. Note that
                    // referencesStart == 1 because the first argument is the exception type.
                    emulatedStackFrame.copyRangeTo(fallback, handlerArgsRange,
                            1 /* referencesStart */, 0 /* stackFrameStart */);

                    // Perform the invoke and return the appropriate value.
                    handler.invoke(fallback);
                    fallback.copyReturnValueTo(emulatedStackFrame);
                } else {
                    // The exception is not of the expected type, we throw it.
                    throw th;
                }
            }
        }
    }

    /**
     * Implements {@code MethodHandles.GuardWithTest}.
     */
    public static class GuardWithTest extends Transformer {
        private final MethodHandle test;
        private final MethodHandle target;
        private final MethodHandle fallback;

        private final EmulatedStackFrame.Range testArgsRange;

        public GuardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback) {
            super(target.type());

            this.test = test;
            this.target = target;
            this.fallback = fallback;

            // The test method might have a subset of the arguments of the handle / target.
            testArgsRange = EmulatedStackFrame.Range.of(target.type(), 0, test.type().parameterCount());
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            EmulatedStackFrame testFrame = EmulatedStackFrame.create(test.type());
            emulatedStackFrame.copyRangeTo(testFrame, testArgsRange, 0, 0);

            // We know that the return value for test is going to be boolean.class, so we don't have
            // to do the copyReturnValue dance.
            final boolean value = (boolean) test.invoke(testFrame);
            if (value) {
                target.invoke(emulatedStackFrame);
            } else {
                fallback.invoke(emulatedStackFrame);
            }
        }
    }
}
