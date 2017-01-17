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
import dalvik.system.EmulatedStackFrame.StackFrameReader;
import dalvik.system.EmulatedStackFrame.StackFrameWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import sun.invoke.util.Wrapper;
import sun.misc.Unsafe;

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

        protected Transformer(MethodType type, int invokeKind) {
            super(TRANSFORM_INTERNAL.getArtMethod(), invokeKind, type);
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

    /**
     * Implementation of MethodHandles.arrayElementGetter for reference types.
     */
    public static class ReferenceArrayElementGetter extends Transformer {
        private final Class<?> arrayClass;
        private final StackFrameReader reader;
        private final StackFrameWriter writer;

        public ReferenceArrayElementGetter(Class<?> arrayClass) {
            super(MethodType.methodType(arrayClass.getComponentType(),
                    new Class<?>[]{arrayClass, int.class}));
            this.arrayClass = arrayClass;
            reader = new StackFrameReader();
            writer = new StackFrameWriter();
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            reader.attach(emulatedStackFrame);
            writer.attach(emulatedStackFrame);

            // Read the array object and the index from the stack frame.
            final Object[] array = (Object[]) reader.nextReference(arrayClass);
            final int index = reader.nextInt();

            // Write the array element back to the stack frame.
            writer.makeReturnValueAccessor();
            writer.putNextReference(array[index], arrayClass.getComponentType());
        }
    }

    /**
     * Implementation of MethodHandles.arrayElementSetter for reference types.
     */
    public static class ReferenceArrayElementSetter extends Transformer {
        private final Class<?> arrayClass;
        private final StackFrameReader reader;

        public ReferenceArrayElementSetter(Class<?> arrayClass) {
            super(MethodType.methodType(void.class,
                    new Class<?>[] { arrayClass, int.class, arrayClass.getComponentType() }));
            this.arrayClass = arrayClass;
            reader = new StackFrameReader();
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            reader.attach(emulatedStackFrame);

            // Read the array object, index and the value to write from the stack frame.
            final Object[] array = (Object[]) reader.nextReference(arrayClass);
            final int index = reader.nextInt();
            final Object value = reader.nextReference(arrayClass.getComponentType());

            array[index] = value;
        }
    }

    /**
     * Implementation of MethodHandles.identity() for reference types.
     */
    public static class ReferenceIdentity extends Transformer {
        private final Class<?> type;
        private final StackFrameReader reader;
        private final StackFrameWriter writer;

        public ReferenceIdentity(Class<?> type) {
            super(MethodType.methodType(type, type));
            this.type = type;

            reader = new StackFrameReader();
            writer = new StackFrameWriter();
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            reader.attach(emulatedStackFrame);
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();

            writer.putNextReference(reader.nextReference(type), type);
        }
    }

    /**
     * Implementation of MethodHandles.constant.
     */
    public static class Constant extends Transformer {
        private final Class<?> type;

        // NOTE: This implementation turned out to be more awkward than expected becuase
        // of the type system. We could simplify this considerably at the cost of making
        // the emulated stack frame API uglier or by transitioning into JNI.
        //
        // We could consider implementing this in terms of bind() once that's implemented.
        // This would then just become : MethodHandles.identity(type).bind(value).
        private int asInt;
        private long asLong;
        private float asFloat;
        private double asDouble;
        private Object asReference;

        private char typeChar;

        private final EmulatedStackFrame.StackFrameWriter writer;

        public Constant(Class<?> type, Object value) {
            super(MethodType.methodType(type));
            this.type = type;

            if (!type.isPrimitive()) {
                asReference = value;
                typeChar = 'L';
            } else if (type == int.class) {
                asInt = (int) value;
                typeChar = 'I';
            } else if (type == char.class) {
                asInt = (int) (char) value;
                typeChar = 'C';
            } else if (type == short.class) {
                asInt = (int) (short) value;
                typeChar = 'S';
            } else if (type == byte.class) {
                asInt = (int) (byte) value;
                typeChar = 'B';
            } else if (type == boolean.class) {
                asInt = ((boolean) value) ? 1 : 0;
                typeChar = 'Z';
            } else if (type == long.class) {
                asLong = (long) value;
                typeChar = 'J';
            } else if (type == float.class) {
                asFloat = (float) value;
                typeChar = 'F';
            } else if (type == double.class) {
                asDouble = (double) value;
                typeChar = 'D';
            } else {
                throw new AssertionError("unknown type: " + typeChar);
            }

            writer = new EmulatedStackFrame.StackFrameWriter();
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            writer.attach(emulatedStackFrame);
            writer.makeReturnValueAccessor();

            switch (typeChar) {
                case 'L' : { writer.putNextReference(asReference, type); break; }
                case 'I' : { writer.putNextInt(asInt); break; }
                case 'C' : { writer.putNextChar((char) asInt); break; }
                case 'S' : { writer.putNextShort((short) asInt); break; }
                case 'B' : { writer.putNextByte((byte) asInt); break; }
                case 'Z' : { writer.putNextBoolean(asInt == 1); break; }
                case 'J' : { writer.putNextLong(asLong); break; }
                case 'F' : { writer.putNextFloat(asFloat); break; }
                case 'D' : { writer.putNextDouble(asDouble); break; }
                default:
                    throw new AssertionError("Unexpected typeChar: " + typeChar);
            }
        }
    }

    /*package*/ static class Construct extends Transformer {
        private final MethodHandle constructorHandle;
        private final EmulatedStackFrame.Range callerRange;

        /*package*/ Construct(MethodHandle constructorHandle, MethodType returnedType) {
            super(returnedType);
            this.constructorHandle = constructorHandle;
            this.callerRange = EmulatedStackFrame.Range.all(type());
        }

        private static boolean isAbstract(Class<?> klass) {
            return (klass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
        }

        private static void checkInstantiable(Class<?> klass) throws InstantiationException {
            if (isAbstract(klass)) {
                String s = klass.isInterface() ? "interface " : "abstract class ";
                throw new InstantiationException("Can't instantiate " + s + klass);
            }
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            final Class<?> receiverType = type().rtype();
            checkInstantiable(receiverType);

            // Allocate memory for receiver.
            Object receiver = Unsafe.getUnsafe().allocateInstance(receiverType);

            // The MethodHandle type for the caller has the form of
            // {rtype=T,ptypes=A1..An}. The constructor MethodHandle is of
            // the form {rtype=void,ptypes=T,A1...An}. So the frame for
            // the constructor needs to have a slot with the receiver
            // in position 0.
            EmulatedStackFrame constructorFrame =
                    EmulatedStackFrame.create(constructorHandle.type());
            constructorFrame.setReference(0, receiver);
            emulatedStackFrame.copyRangeTo(constructorFrame, callerRange, 1, 0);
            constructorHandle.invoke(constructorFrame);

            // Set return result for caller.
            emulatedStackFrame.setReturnValueTo(receiver);
        }
    }

    /**
     * Implements MethodHandle.bindTo.
     *
     * @hide
     */
    public static class BindTo extends Transformer {
        private final MethodHandle delegate;
        private final Object receiver;

        private final EmulatedStackFrame.Range range;

        public BindTo(MethodHandle delegate, Object receiver) {
            super(delegate.type().dropParameterTypes(0, 1));

            this.delegate = delegate;
            this.receiver = receiver;

            this.range = EmulatedStackFrame.Range.all(this.type());
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            // Create a new emulated stack frame with the full type (including the leading
            // receiver reference).
            EmulatedStackFrame stackFrame = EmulatedStackFrame.create(delegate.type());

            // The first reference argument must be the receiver.
            stackFrame.setReference(0, receiver);
            // Copy all other arguments.
            emulatedStackFrame.copyRangeTo(stackFrame, range,
                    1 /* referencesStart */, 0 /* stackFrameStart */);

            // Perform the invoke.
            delegate.invoke(stackFrame);
            stackFrame.copyReturnValueTo(emulatedStackFrame);
        }
    }

    /**
     * Implements MethodHandle.filterReturnValue.
     */
    public static class FilterReturnValue extends Transformer {
        private final MethodHandle target;
        private final MethodHandle filter;

        private final EmulatedStackFrame.StackFrameReader returnValueReader;
        private final EmulatedStackFrame.StackFrameWriter filterWriter;

        private final EmulatedStackFrame.Range allArgs;

        public FilterReturnValue(MethodHandle target, MethodHandle filter) {
            super(MethodType.methodType(filter.type().rtype(), target.type().ptypes()));

            this.target = target;
            this.filter = filter;

            returnValueReader = new EmulatedStackFrame.StackFrameReader();
            filterWriter = new EmulatedStackFrame.StackFrameWriter();

            allArgs = EmulatedStackFrame.Range.all(type());
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            // Create a new frame with the target's type and copy all arguments over.
            // This frame differs in return type with |emulatedStackFrame| but will have
            // the same parameter shapes.
            EmulatedStackFrame targetFrame = EmulatedStackFrame.create(target.type());
            emulatedStackFrame.copyRangeTo(targetFrame, allArgs, 0, 0);

            target.invoke(targetFrame);

            // Perform the invoke.
            returnValueReader.attach(targetFrame);
            returnValueReader.makeReturnValueAccessor();

            // Create an emulated frame for the filter and copy all its arguments across.
            EmulatedStackFrame filterFrame = EmulatedStackFrame.create(filter.type());
            filterWriter.attach(filterFrame);

            final Class<?> returnType = target.type().rtype();
            if (!returnType.isPrimitive()) {
                filterWriter.putNextReference(returnValueReader.nextReference(returnType),
                        returnType);
            } else if (returnType == boolean.class) {
                filterWriter.putNextBoolean(returnValueReader.nextBoolean());
            } else if (returnType == byte.class) {
                filterWriter.putNextByte(returnValueReader.nextByte());
            } else if (returnType == char.class) {
                filterWriter.putNextChar(returnValueReader.nextChar());
            } else if (returnType == short.class) {
                filterWriter.putNextShort(returnValueReader.nextShort());
            } else if (returnType == int.class) {
                filterWriter.putNextInt(returnValueReader.nextInt());
            } else if (returnType == long.class) {
                filterWriter.putNextLong(returnValueReader.nextLong());
            } else if (returnType == float.class) {
                filterWriter.putNextFloat(returnValueReader.nextFloat());
            } else if (returnType == double.class) {
                filterWriter.putNextDouble(returnValueReader.nextDouble());
            }

            // Invoke the filter and copy its return value back to the original frame.
            filter.invoke(filterFrame);
            filterFrame.copyReturnValueTo(emulatedStackFrame);
        }
    }

    /*
     * Implements MethodHandles.permuteArguments.
     *
     * @hide
     */
    public static class PermuteArguments extends Transformer {
        private final MethodHandle target;
        private final int[] reorder;

        private final EmulatedStackFrame.StackFrameWriter writer;
        private final EmulatedStackFrame.StackFrameReader reader;

        public PermuteArguments(MethodType type, MethodHandle target, int[] reorder) {
            super(type);

            this.target = target;
            this.reorder = reorder;

            writer = new EmulatedStackFrame.StackFrameWriter();
            reader = new EmulatedStackFrame.StackFrameReader();
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            reader.attach(emulatedStackFrame);
            final Class<?>[] ptypes = type().ptypes();

            // In the interests of simplicity, we box / unbox arguments while performing
            // the permutation. We first iterate through the incoming stack frame and box
            // each argument. We then unbox and write out the argument to the target frame
            // according to the specified reordering.
            Object[] arguments = new Object[reorder.length];

            for (int i = 0; i < ptypes.length; ++i) {
                final Class<?> ptype = ptypes[i];
                if (!ptype.isPrimitive()) {
                    arguments[i] = reader.nextReference(ptype);
                } else if (ptype == boolean.class) {
                    arguments[i] = reader.nextBoolean();
                } else if (ptype == byte.class) {
                    arguments[i] = reader.nextByte();
                } else if (ptype == char.class) {
                    arguments[i] = reader.nextChar();
                } else if (ptype == short.class) {
                    arguments[i] = reader.nextShort();
                } else if (ptype == int.class) {
                    arguments[i] = reader.nextInt();
                } else if (ptype == long.class) {
                    arguments[i] = reader.nextLong();
                } else if (ptype == float.class) {
                    arguments[i] = reader.nextFloat();
                } else if (ptype == double.class) {
                    arguments[i] = reader.nextDouble();
                } else {
                    throw new AssertionError("Unexpected type: " + ptype);
                }
            }

            EmulatedStackFrame calleeFrame = EmulatedStackFrame.create(target.type());
            writer.attach(calleeFrame);

            for (int i = 0; i < ptypes.length; ++i) {
                int idx = reorder[i];
                final Class<?> ptype = ptypes[idx];
                final Object argument = arguments[idx];

                if (!ptype.isPrimitive()) {
                    writer.putNextReference(argument, ptype);
                } else if (ptype == boolean.class) {
                    writer.putNextBoolean((boolean) argument);
                } else if (ptype == byte.class) {
                    writer.putNextByte((byte) argument);
                } else if (ptype == char.class) {
                    writer.putNextChar((char) argument);
                } else if (ptype == short.class) {
                    writer.putNextShort((short) argument);
                } else if (ptype == int.class) {
                    writer.putNextInt((int) argument);
                } else if (ptype == long.class) {
                    writer.putNextLong((long) argument);
                } else if (ptype == float.class) {
                    writer.putNextFloat((float) argument);
                } else if (ptype == double.class) {
                    writer.putNextDouble((double) argument);
                } else {
                    throw new AssertionError("Unexpected type: " + ptype);
                }
            }

            target.invoke(calleeFrame);
            calleeFrame.copyReturnValueTo(emulatedStackFrame);
        }
    }

    /**
     * Converts methods with a trailing array argument to variable arity
     * methods. So (A,B,C[])R can be invoked with any number of convertible
     * arguments after B, e.g. (A,B)R or (A, B, C0)R or (A, B, C0...Cn)R.
     *
     * @hide
     */
    /*package*/ static class VarargsCollector extends Transformer {
        final MethodHandle target;

        /*package*/ VarargsCollector(MethodHandle target) {
            super(target.type(), MethodHandle.INVOKE_CALLSITE_TRANSFORM);
            if (!lastParameterTypeIsAnArray(target.type().ptypes())) {
                throw new IllegalArgumentException("target does not have array as last parameter");
            }
            this.target = target;
        }

        private static boolean lastParameterTypeIsAnArray(Class<?>[] parameterTypes) {
            if (parameterTypes.length == 0) return false;
            return parameterTypes[parameterTypes.length - 1].isArray();
        }

        @Override
        public boolean isVarargsCollector() { return true; }

        @Override
        public MethodHandle asFixedArity() { return target; }

        @Override
        public void transform(EmulatedStackFrame callerFrame) throws Throwable {
            MethodType callerFrameType = callerFrame.getMethodType();
            Class<?>[] callerPTypes = callerFrameType.ptypes();
            Class<?>[] targetPTypes = type().ptypes();

            int lastTargetIndex = targetPTypes.length - 1;
            if (callerPTypes.length == targetPTypes.length &&
                targetPTypes[lastTargetIndex].isAssignableFrom(callerPTypes[lastTargetIndex])) {
                // Caller frame matches target frame in the arity array parameter. Invoke
                // immediately, and let the invoke() dispatch perform any necessary conversions
                // on the other parameters present.
                target.invoke(callerFrame);
                return;
            }

            if (callerPTypes.length < targetPTypes.length - 1) {
                // Too few arguments to be compatible with variable arity invocation.
                throwWrongMethodTypeException(callerFrameType, type());
            }

            if (!MethodType.canConvert(type().rtype(), callerFrameType.rtype())) {
                // Incompatible return type.
                throwWrongMethodTypeException(callerFrameType, type());
            }

            Class<?> elementType = targetPTypes[lastTargetIndex].getComponentType();
            if (!arityArgumentsConvertible(callerPTypes, lastTargetIndex, elementType)) {
                // Wrong types to be compatible with variable arity invocation.
                throwWrongMethodTypeException(callerFrameType, type());
            }

            // Allocate targetFrame.
            MethodType targetFrameType = makeTargetFrameType(callerFrameType, type());
            EmulatedStackFrame targetFrame = EmulatedStackFrame.create(targetFrameType);
            prepareFrame(callerFrame, targetFrame);

            // Invoke target.
            target.invoke(targetFrame);

            // Copy return value to the caller's frame.
            targetFrame.copyReturnValueTo(callerFrame);
        }

        private static void throwWrongMethodTypeException(MethodType from, MethodType to) {
            throw new WrongMethodTypeException("Cannot convert " + from + " to " + to);
        }

        private static boolean arityArgumentsConvertible(Class<?>[] ptypes, int arityStart,
                                                         Class<?> elementType) {
            if (ptypes.length - 1 == arityStart) {
                if (ptypes[arityStart].isArray() &&
                    ptypes[arityStart].getComponentType() == elementType) {
                    // The last ptype is in the same position as the arity
                    // array and has the same type.
                    return true;
                }
            }

            for (int i = arityStart; i < ptypes.length; ++i) {
                if (!MethodType.canConvert(ptypes[i], elementType)) {
                    return false;
                }
            }
            return true;
        }

        private static Object referenceArray(StackFrameReader reader, Class<?>[] ptypes,
                                             Class<?> elementType, int offset, int length) {
            Object arityArray = Array.newInstance(elementType, length);
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                Object o = null;
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'L': { o = reader.nextReference(argumentType); break; }
                    case 'I': { o = reader.nextInt(); break; }
                    case 'J': { o = reader.nextLong(); break; }
                    case 'B': { o = reader.nextByte(); break; }
                    case 'S': { o = reader.nextShort(); break; }
                    case 'C': { o = reader.nextChar(); break; }
                    case 'Z': { o = reader.nextBoolean(); break; }
                    case 'F': { o = reader.nextFloat(); break; }
                    case 'D': { o = reader.nextDouble(); break; }
                }
                if (o != null && !elementType.isAssignableFrom(o.getClass())) {
                    throw new ClassCastException(
                        o.getClass() + " not assignable to " + elementType);
                }
                Array.set(arityArray, i, o);
            }
            return arityArray;
        }

        private static Object intArray(StackFrameReader reader, Class<?> ptypes[],
                                       int offset, int length) {
            int[] arityArray = new int[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'I': { arityArray[i] = reader.nextInt(); break; }
                    case 'S': { arityArray[i] = reader.nextShort(); break; }
                    case 'B': { arityArray[i] = reader.nextByte(); break; }
                    default: {
                        arityArray[i] = (Integer) reader.nextReference(argumentType);
                        break;
                    }
                }
            }
            return arityArray;
        }

        private static Object longArray(StackFrameReader reader, Class<?> ptypes[],
                                        int offset, int length) {
            long[] arityArray = new long[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'J': { arityArray[i] = reader.nextLong(); break; }
                    case 'I': { arityArray[i] = reader.nextInt(); break; }
                    case 'S': { arityArray[i] = reader.nextShort(); break; }
                    case 'B': { arityArray[i] = reader.nextByte(); break; }
                    default: { arityArray[i] = (Long) reader.nextReference(argumentType); break; }
                }
            }
            return arityArray;
        }

        private static Object byteArray(StackFrameReader reader, Class<?> ptypes[],
                                        int offset, int length) {
            byte[] arityArray = new byte[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'B': { arityArray[i] = reader.nextByte(); break; }
                    default: { arityArray[i] = (Byte) reader.nextReference(argumentType); break; }
                }
            }
            return arityArray;
        }

        private static Object shortArray(StackFrameReader reader, Class<?> ptypes[],
                                        int offset, int length) {
            short[] arityArray = new short[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'S': { arityArray[i] = reader.nextShort(); break; }
                    case 'B': { arityArray[i] = reader.nextByte(); break; }
                    default: { arityArray[i] = (Short) reader.nextReference(argumentType); break; }
                }
            }
            return arityArray;
        }

        private static Object charArray(StackFrameReader reader, Class<?> ptypes[],
                                        int offset, int length) {
            char[] arityArray = new char[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'C': { arityArray[i] = reader.nextChar(); break; }
                    default: {
                        arityArray[i] = (Character) reader.nextReference(argumentType);
                        break;
                    }
                }
            }
            return arityArray;
        }

        private static Object booleanArray(StackFrameReader reader, Class<?> ptypes[],
                                        int offset, int length) {
            boolean[] arityArray = new boolean[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'Z': { arityArray[i] = reader.nextBoolean(); break; }
                    default:
                        arityArray[i] = (Boolean) reader.nextReference(argumentType);
                        break;
                }
            }
            return arityArray;
        }

        private static Object floatArray(StackFrameReader reader, Class<?> ptypes[],
                                        int offset, int length) {
            float[] arityArray = new float[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'F': { arityArray[i] = reader.nextFloat(); break; }
                    case 'J': { arityArray[i] = reader.nextLong(); break; }
                    case 'I': { arityArray[i] = reader.nextInt(); break; }
                    case 'S': { arityArray[i] = reader.nextShort(); break; }
                    case 'B': { arityArray[i] = reader.nextByte(); break; }
                    default: {
                        arityArray[i] = (Float) reader.nextReference(argumentType);
                        break;
                    }
                }
            }
            return arityArray;
        }

        private static Object doubleArray(StackFrameReader reader, Class<?> ptypes[],
                                        int offset, int length) {
            double[] arityArray = new double[length];
            for (int i = 0; i < length; ++i) {
                Class<?> argumentType = ptypes[i + offset];
                switch (Wrapper.basicTypeChar(argumentType)) {
                    case 'D': { arityArray[i] = reader.nextDouble(); break; }
                    case 'F': { arityArray[i] = reader.nextFloat(); break; }
                    case 'J': { arityArray[i] = reader.nextLong(); break; }
                    case 'I': { arityArray[i] = reader.nextInt(); break; }
                    case 'S': { arityArray[i] = reader.nextShort(); break; }
                    case 'B': { arityArray[i] = reader.nextByte(); break; }
                    default: {
                        arityArray[i] = (Double) reader.nextReference(argumentType);
                        break;
                    }
                }
            }
            return arityArray;
        }

        private static Object makeArityArray(MethodType callerFrameType,
                                             StackFrameReader callerFrameReader,
                                             int indexOfArityArray,
                                             Class<?> arityArrayType) {
            int arityArrayLength = callerFrameType.ptypes().length - indexOfArityArray;
            Class<?> elementType = arityArrayType.getComponentType();
            Class<?>[] callerPTypes = callerFrameType.ptypes();

            char elementBasicType = Wrapper.basicTypeChar(elementType);
            switch (elementBasicType) {
                case 'L': return referenceArray(callerFrameReader, callerPTypes, elementType,
                                                indexOfArityArray, arityArrayLength);
                case 'I': return intArray(callerFrameReader, callerPTypes,
                                          indexOfArityArray, arityArrayLength);
                case 'J': return longArray(callerFrameReader, callerPTypes,
                                           indexOfArityArray, arityArrayLength);
                case 'B': return byteArray(callerFrameReader, callerPTypes,
                                           indexOfArityArray, arityArrayLength);
                case 'S': return shortArray(callerFrameReader, callerPTypes,
                                            indexOfArityArray, arityArrayLength);
                case 'C': return charArray(callerFrameReader, callerPTypes,
                                           indexOfArityArray, arityArrayLength);
                case 'Z': return booleanArray(callerFrameReader, callerPTypes,
                                              indexOfArityArray, arityArrayLength);
                case 'F': return floatArray(callerFrameReader, callerPTypes,
                                            indexOfArityArray, arityArrayLength);
                case 'D': return doubleArray(callerFrameReader, callerPTypes,
                                             indexOfArityArray, arityArrayLength);
            }
            throw new InternalError("Unexpected type: " + elementType);
        }

        private static void copyParameter(StackFrameReader reader, StackFrameWriter writer,
                                          Class<?> ptype) {
            switch (Wrapper.basicTypeChar(ptype)) {
                case 'L': { writer.putNextReference(reader.nextReference(ptype), ptype); break; }
                case 'I': { writer.putNextInt(reader.nextInt()); break; }
                case 'J': { writer.putNextLong(reader.nextLong()); break; }
                case 'B': { writer.putNextByte(reader.nextByte()); break; }
                case 'S': { writer.putNextShort(reader.nextShort()); break; }
                case 'C': { writer.putNextChar(reader.nextChar()); break; }
                case 'Z': { writer.putNextBoolean(reader.nextBoolean()); break; }
                case 'F': { writer.putNextFloat(reader.nextFloat()); break; }
                case 'D': { writer.putNextDouble(reader.nextDouble()); break; }
                default: throw new InternalError("Unexpected type: " + ptype);
            }
        }

        private static void prepareFrame(EmulatedStackFrame callerFrame,
                                         EmulatedStackFrame targetFrame) {
            StackFrameWriter targetWriter = new StackFrameWriter();
            targetWriter.attach(targetFrame);
            StackFrameReader callerReader = new StackFrameReader();
            callerReader.attach(callerFrame);

            // Copy parameters from |callerFrame| to |targetFrame| leaving room for arity array.
            MethodType targetMethodType = targetFrame.getMethodType();
            int indexOfArityArray = targetMethodType.ptypes().length - 1;
            for (int i = 0; i < indexOfArityArray; ++i) {
                Class<?> ptype = targetMethodType.ptypes()[i];
                copyParameter(callerReader, targetWriter, ptype);
            }

            // Add arity array as last parameter in |targetFrame|.
            Class<?> arityArrayType = targetMethodType.ptypes()[indexOfArityArray];
            Object arityArray = makeArityArray(callerFrame.getMethodType(), callerReader,
                                               indexOfArityArray, arityArrayType);
            targetWriter.putNextReference(arityArray, arityArrayType);
        }

        /**
         * Computes the frame type to invoke the target method handle with. This
         * is the same as the caller frame type, but with the trailing argument
         * being the array type that is the trailing argument in the target method
         * handle.
         *
         * Suppose the targetType is (T0, T1, T2[])RT and the callerType is (C0, C1, C2, C3)RC
         * then the constructed type is (C0, C1, T2[])RC.
         */
        private static MethodType makeTargetFrameType(MethodType callerType,
                                                      MethodType targetType) {
            final int ptypesLength = targetType.ptypes().length;
            final Class<?>[] ptypes = new Class<?>[ptypesLength];
            // Copy types from caller types to new methodType.
            System.arraycopy(callerType.ptypes(), 0, ptypes, 0, ptypesLength - 1);
            // Set the last element in the type array to be the
            // varargs array of the target.
            ptypes[ptypesLength - 1] = targetType.ptypes()[ptypesLength - 1];
            return MethodType.methodType(callerType.rtype(), ptypes);
        }
    }

    /**
     * Implements MethodHandles.invoker & MethodHandles.exactInvoker.
     */
    static class Invoker extends Transformer {
        private final MethodType targetType;
        private final boolean isExactInvoker;
        private final EmulatedStackFrame.Range copyRange;

        Invoker(MethodType targetType, boolean isExactInvoker) {
            super(targetType.insertParameterTypes(0, MethodHandle.class));
            this.targetType = targetType;
            this.isExactInvoker = isExactInvoker;
            copyRange = EmulatedStackFrame.Range.of(type(), 1, type().parameterCount());
        }

        @Override
        public void transform(EmulatedStackFrame emulatedStackFrame) throws Throwable {
            // We need to artifically throw a WrongMethodTypeException here because we
            // can't call invokeExact on the target inside the transformer.
            if (isExactInvoker) {
                // TODO: We should do the comparison by hand if this new type creation
                // on every invoke proves too expensive.
                MethodType callType = emulatedStackFrame.getCallsiteType().dropParameterTypes(0, 1);
                if (!targetType.equals(callType)) {
                    throw new WrongMethodTypeException("Wrong type, Expected: " + targetType
                            + " was: " + callType);
                }
            }

            // The first argument to the stack frame is the handle that needs to be invoked.
            MethodHandle target = emulatedStackFrame.getReference(0, MethodHandle.class);

            // All other arguments must be copied to the target frame.
            EmulatedStackFrame targetFrame = EmulatedStackFrame.create(targetType);
            emulatedStackFrame.copyRangeTo(targetFrame, copyRange, 0, 0);

            // Finally, invoke the handle and copy the return value.
            target.invoke(targetFrame);
            targetFrame.copyReturnValueTo(emulatedStackFrame);
        }
    }
}
