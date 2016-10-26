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
    public EmulatedStackFrame() {
        throw new AssertionError("not implemented yet.");
    }
}
