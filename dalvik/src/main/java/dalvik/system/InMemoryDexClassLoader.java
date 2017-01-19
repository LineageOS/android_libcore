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

package dalvik.system;

import java.nio.ByteBuffer;
import sun.misc.Cleaner;

/**
 * A {@link ClassLoader} implementation that loads classes from a
 * buffer containing a DEX file. This can be used to execute code that
 * has not been written to the local file system.
 */
public final class InMemoryDexClassLoader extends ClassLoader {
    private final DexData dexData;

    /**
     * Creates a new in-memory DEX class loader.
     *
     * @param dexBuffer buffer containing DEX file contents between
     *                       <tt>buffer.position()</tt> and <tt>buffer.limit()</tt>.
     * @param parent the parent class loader for delegation.
     */
    public InMemoryDexClassLoader(ByteBuffer dexBuffer, ClassLoader parent) {
        super(parent);
        if (dexBuffer == null) {
            throw new NullPointerException("dexData == null");
        }
        this.dexData = new DexData(dexBuffer);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return dexData.findClass(name, this);
        } catch (Exception e) {
            throw new ClassNotFoundException("Didn't find class \"" + name + "\"", e);
        }
    }

    @Override
    protected synchronized Package getPackage(String name) {
        // This is duplicated from BaseDexClassLoader.getPackage which
        // has an extensive comment on why this needs to be defined.
        if (name == null || name.isEmpty()) {
            return null;
        }

        Package pkg = super.getPackage(name);
        if (pkg == null) {
            pkg = definePackage(name, "Unknown", "0.0", "Unknown",
                                "Unknown", "0.0", "Unknown", null);
        }
        return pkg;
    }

    /**
     * Representation of native resources associated with DEX files loaded by
     * InMemoryDexClassLoader.
     */
    private static final class DexData {
        private final long cookie;

        DexData(ByteBuffer buffer) {
            if (buffer.isDirect()) {
                cookie = initializeWithDirectBuffer(buffer, buffer.position(), buffer.limit());
            } else {
                cookie = initializeWithArray(buffer.array(), buffer.position(), buffer.limit());
            }

            if (cookie != 0) {
                // Register for clean-up when this instance is phantom
                // reachable. Cleaner instances are inserted in a global
                // queue to preserve liveness. DexDataDeallocator must not
                // have a pointer to DexData otherwise it will prevent
                // cleaning and the freeing of the native resources.
                Cleaner.create(this, new DexDataDeallocator(cookie));
            }
        }

        protected Class<?> findClass(String name, ClassLoader loader) {
            return findClass(name, loader, cookie);
        }

        private static native long initializeWithDirectBuffer(ByteBuffer buffer,
                                                              int start, int end);
        private static native long initializeWithArray(byte[] array, int start, int end);
        private static native void uninitialize(long cookie);
        private native Class findClass(String name, ClassLoader loader, long cookie);
    }

    /**
     * Helper class to release native resources associated with DexData instances.
     */
    private static final class DexDataDeallocator implements Runnable {
        private final long cookie;

        DexDataDeallocator(long cookie) {
            this.cookie = cookie;
        }

        public void run() {
            DexData.uninitialize(cookie);
        }
    }
}
