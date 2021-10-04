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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import javax.xml.transform.stream.StreamResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StreamResultTest {

    private StreamResult result;

    @Before
    public void setUp() {
        result = new StreamResult();
    }

    @Test
    public void constructor() {
        result = new StreamResult();
        assertNotNull(result);
        assertNull(result.getSystemId());
    }

    @Test
    public void constructor_File() {
        result = new StreamResult(new File("path"));
        assertEquals("file:///path", result.getSystemId());
    }

    @Test
    public void constructor_String() {
        result = new StreamResult((String)null);
        assertNull(result.getSystemId());

        result = new StreamResult("");
        assertEquals("", result.getSystemId());

        result = new StreamResult("some string");
        assertEquals("some string", result.getSystemId());
    }

    @Test
    public void getSystemId() {
        assertNull(result.getSystemId());

        result = new StreamResult("string constructor");
        assertEquals("string constructor", result.getSystemId());

        result = new StreamResult(new File("path"));
        assertEquals("file:///path", result.getSystemId());

        result.setSystemId("hello");
        assertEquals("hello", result.getSystemId());
    }

    @Test
    public void setSystemId_File() {
        result.setSystemId(new File("path"));
        assertEquals("file:///path", result.getSystemId());

        result.setSystemId(new File("."));
        assertEquals("file:///.", result.getSystemId());
    }

    @Test
    public void setSystemId_String() {
        result.setSystemId((String)null);
        assertNull(result.getSystemId());

        result.setSystemId("hello");
        assertEquals("hello", result.getSystemId());
    }
}
