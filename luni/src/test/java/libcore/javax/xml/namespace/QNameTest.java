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
package libcore.javax.xml.namespace;

import javax.xml.namespace.QName;
import junit.framework.TestCase;

public class QNameTest extends TestCase {

    public void testConstructor() {
        QName qName = new QName("hello");
        assertEquals("", qName.getNamespaceURI());
        assertEquals("", qName.getPrefix());
        assertEquals("hello", qName.getLocalPart());
    }

    public void testGetNamespaceURI() {
        QName qName = new QName("namespace", "local part", "prefix");
        assertEquals("namespace", qName.getNamespaceURI());
        assertEquals("local part", qName.getLocalPart());
        assertEquals("prefix", qName.getPrefix());

        qName = new QName(null, "local part", "prefix");
        assertEquals("", qName.getNamespaceURI());
        assertEquals("local part", qName.getLocalPart());
        assertEquals("prefix", qName.getPrefix());
    }

    public void testGetPrefix() {
        QName qName = new QName("namespace", "local part", "prefix");
        assertEquals("prefix", qName.getPrefix());

        try {
            new QName("namespace", "local part", null);
            fail("Unexpectedly didn't throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}
    }
}
