/*
 * Copyright (C) 2018 The Android Open Source Project
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

package libcore.libcore.io;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import junit.framework.TestCase;

import libcore.io.Libcore;

public class FdsanTest extends TestCase {
    public void testFileInputStream() throws Exception {
        try (FileInputStream fis = new FileInputStream("/dev/null")) {
            FileDescriptor fd = fis.getFD();
            long tag = Libcore.os.android_fdsan_get_owner_tag(fd);
            assertTrue(tag != 0);
            assertEquals("FileInputStream", Libcore.os.android_fdsan_get_tag_type(tag));
            assertEquals(System.identityHashCode(fis), Libcore.os.android_fdsan_get_tag_value(tag));
        }
    }

    public void testFileOutputStream() throws Exception {
        try (FileOutputStream fis = new FileOutputStream("/dev/null")) {
            FileDescriptor fd = fis.getFD();
            long tag = Libcore.os.android_fdsan_get_owner_tag(fd);
            assertTrue(tag != 0);
            assertEquals("FileOutputStream", Libcore.os.android_fdsan_get_tag_type(tag));
            assertEquals(System.identityHashCode(fis), Libcore.os.android_fdsan_get_tag_value(tag));
        }
    }

    public void testRandomAccessFile() throws Exception {
        try (RandomAccessFile fis = new RandomAccessFile("/dev/null", "r")) {
            FileDescriptor fd = fis.getFD();
            long tag = Libcore.os.android_fdsan_get_owner_tag(fd);
            assertTrue(tag != 0);
            assertEquals("RandomAccessFile", Libcore.os.android_fdsan_get_tag_type(tag));
            assertEquals(System.identityHashCode(fis), Libcore.os.android_fdsan_get_tag_value(tag));
        }
    }

    public void testParcelFileDescriptor() throws Exception {
        Class pfdClass;
        try {
            pfdClass = Class.forName("android.os.ParcelFileDescriptor");
        } catch (ClassNotFoundException ex) {
            // Don't fail if ParcelFileDescriptor isn't on our classpath.
            return;
        }

        try (FileInputStream fis = new FileInputStream("/dev/null")) {
            Method pfdMethodDup = pfdClass.getMethod("dup", FileDescriptor.class);
            Method pfdMethodClose = pfdClass.getMethod("close");
            Method pfdMethodGetFileDescriptor = pfdClass.getMethod("getFileDescriptor");
            Field readonly = pfdClass.getField("MODE_READ_ONLY");

            Object pfd = pfdMethodDup.invoke(null, fis.getFD());
            FileDescriptor fd = (FileDescriptor)pfdMethodGetFileDescriptor.invoke(pfd);
            long tag = Libcore.os.android_fdsan_get_owner_tag(fd);
            assertTrue(tag != 0);
            assertEquals("ParcelFileDescriptor", Libcore.os.android_fdsan_get_tag_type(tag));
            assertEquals(System.identityHashCode(pfd), Libcore.os.android_fdsan_get_tag_value(tag));
            pfdMethodClose.invoke(pfd);
        }
    }
}
