/*
 * Copyright (C) 2017 The Android Open Source Project
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
package libcore.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import junit.framework.TestCase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.sax2.Driver;

public class XmlToSax2DriverTest extends TestCase {

    private Driver driver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        driver = createEmptyDriver();
    }

    public void testConstructor() {
        Driver driver = null;
        try {
            driver = new Driver();
        } catch (XmlPullParserException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        assertEquals(0, driver.getLength());
        assertEquals(1, driver.getColumnNumber());
        assertEquals(1, driver.getLineNumber());
    }

    public void testParametrizedConstructor() {
        XmlPullParserFactory factory;
        XmlPullParser parser = null;

        try {
            factory = XmlPullParserFactory.newInstance(null, null);
            parser = factory.newPullParser();
        } catch (XmlPullParserException e) {
            fail("Couldn't create factory and parser");
        }
        Driver driver = null;

        try {
             driver = new Driver(parser);
        } catch (XmlPullParserException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        assertEquals(0, driver.getLength());
        assertEquals(1, driver.getColumnNumber());
        assertEquals(1, driver.getLineNumber());
    }

    public void testGetColumnNumber() throws XmlPullParserException, IOException, SAXException {
        assertEquals(driver.getColumnNumber(), 1);
    }

    public void testGetContentHandler() throws XmlPullParserException {
        assertTrue(driver.getContentHandler() instanceof DefaultHandler);

        ContentHandler handler = new DefaultHandler();
        driver.setContentHandler(handler);
        assertEquals(driver.getContentHandler(), handler);

        driver.setContentHandler(null);
        assertNull(driver.getContentHandler());
    }

    public void testGetDTDHandler() {
        assertNull(driver.getDTDHandler());

        driver.setDTDHandler(new DefaultHandler());
        assertNull(driver.getDTDHandler());
    }

    public void testGetEntityResolver() {
        assertNull(driver.getEntityResolver());

        driver.setEntityResolver(new DefaultHandler());
        assertNull(driver.getEntityResolver());
    }

    public void testGetErrorHandler() {
        assertTrue(driver.getContentHandler() instanceof DefaultHandler);

        driver.setErrorHandler(null);
        assertNull(driver.getErrorHandler());
    }

    public void testGetFeature() {
        final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
        final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
        final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
        final String PROCESS_DOCDECL_FEATURE =
                "http://xmlpull.org/v1/doc/features.html#process-docdecl";
        final String REPORT_NAMESPACE_ATTRIBUTES_FEATURE =
                "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes";

        final Object[][] expectations = {
                {NAMESPACE_PREFIXES_FEATURE, false},
                {VALIDATION_FEATURE, false},
                {PROCESS_DOCDECL_FEATURE, false},
                {REPORT_NAMESPACE_ATTRIBUTES_FEATURE, false},
                {NAMESPACES_FEATURE, true},
        };

        for (Object[] f : expectations) {
            final String feature = (String) f[0];
            final boolean result = (boolean) f[1];
            try {
                assertEquals(result, driver.getFeature(feature));
            } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    public void testGetIndex() throws NoSuchFieldException, IllegalAccessException {
        assertEquals(-1, driver.getIndex("hello"));
        assertEquals(-1, driver.getIndex("encoding"));
        assertEquals(-1, driver.getIndex("version"));
    }

    public void testGetIndex_namespaced() {
        assertEquals(-1, driver.getIndex("", "version"));
    }

    public void testGetLength() {
        assertEquals(0, driver.getLength());
    }

    public void testGetLineNumber() {
        assertEquals(1, driver.getLineNumber());
    }

    public void testGetLocalName() {
        try {
            driver.getLocalName(0);
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testGetProperty() {
        try {
            driver.getProperty("");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testGetPublicId() {
        assertNull(driver.getPublicId());
    }

    public void testGetQName() {
        try {
            driver.getQName(0);
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testGetSystemId() {
        assertNull(driver.getSystemId());
    }

    private Driver createEmptyDriver() throws XmlPullParserException {
        return new Driver();
    }
}
