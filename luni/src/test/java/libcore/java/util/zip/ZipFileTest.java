/*
 * Copyright (C) 2015 The Android Open Source Project
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

package libcore.java.util.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipFileTest extends AbstractZipFileTest {

    @Override
    protected ZipOutputStream createZipOutputStream(OutputStream wrapped) {
        return new ZipOutputStream(wrapped);
    }

    // b/28901232
    // Test for OPEN_DELETE throwing an exception when used in
    // /storage/emulated (fuse fs).
    public void testOpenDeleteOnExternalStorage() throws Exception {
        File file = new File("/storage/emulated/0/Download/foo.zip");

        try {
            ZipOutputStream test = new ZipOutputStream(new FileOutputStream(file));
            test.putNextEntry(new ZipEntry("somefile.txt"));
            test.write(1);
            test.close();

            ZipFile z = new ZipFile(file, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
            InputStream inputStream = z.getInputStream(z.getEntry("somefile.txt"));
            assertEquals(1, inputStream.read());
            inputStream.close();
            z.close();

            assertFalse(file.exists());
        } finally {
            file.delete();
        }
    }
}
