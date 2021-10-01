/*
 * Copyright (C) 2021 The Android Open Source Project
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

package libcore.javax.xml.transform.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.transform.stream.StreamSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StreamSourceTest {

    private StreamSource source;

    @Before
    public void setUp() {
        source = new StreamSource();
    }

    @Test
    public void constructor() {
        source = new StreamSource();
        assertNull(source.getSystemId());
        assertNull(source.getPublicId());
        assertNull(source.getInputStream());
        assertNull(source.getReader());
    }

    @Test
    public void constructor_withInputStreamAndString() {
        InputStream is = new ByteArrayInputStream(new byte[]{ 0x00 });
        String systemId = "systemId";
        source = new StreamSource(is, systemId);
        assertEquals(is, source.getInputStream());
        assertEquals(systemId, source.getSystemId());
    }

    @Test
    public void constructor_withReaderAndString() {
        Reader reader = new CharArrayReader(new char[]{ 'a' });
        String systemId = "systemId";
        source = new StreamSource(reader, systemId);
        assertEquals(reader, source.getReader());
        assertEquals(systemId, source.getSystemId());
    }

    @Test
    public void constructor_withString() {
        String systemId = "systemId";
        source = new StreamSource(systemId);
        assertEquals(systemId, source.getSystemId());
    }

    @Test
    public void setInputStream() {
        InputStream is = new ByteArrayInputStream(new byte[]{ 0x00 });
        source.setInputStream(is);
        assertEquals(is, source.getInputStream());

        source.setInputStream(null);
        assertNull(source.getInputStream());
    }

    @Test
    public void setPublicId() {
        String publicId = "publicId";
        source.setPublicId(publicId);
        assertEquals(publicId, source.getPublicId());

        source.setPublicId(null);
        assertNull(source.getPublicId());
    }

    @Test
    public void setSystemId_withFile() {
        source.setSystemId(new File("path"));
        assertEquals("file:///path", source.getSystemId());

        source.setSystemId(new File("."));
        assertEquals("file:///.", source.getSystemId());
    }

    @Test
    public void setSystemId_withString() {
        String systemId = "systemId";
        source.setSystemId(systemId);
        assertEquals(systemId, source.getSystemId());

        source.setSystemId((String)null);
        assertNull(source.getSystemId());
    }
}
