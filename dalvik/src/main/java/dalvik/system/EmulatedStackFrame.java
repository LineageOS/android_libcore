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

    /**
     * EmulatedStackFrame instances are currently only constructed by the runtime.
     */
    // TODO(narayan): Future changes will allow managed code to construct EmulatedStackFrames.
    private EmulatedStackFrame(MethodType type, Object[] references, byte[] stackFrame) {
        this.type = type;
        this.references = references;
        this.stackFrame = stackFrame;
    }
}
