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

package libcore.sun.security.x509;

import junit.framework.TestCase;

import java.util.function.Function;

import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;


public class AlgorithmIdTest extends TestCase {

    public void test_get_String() throws Exception {
        assertEquals("2.16.840.1.101.3.4.2.4", AlgorithmId.get("SHA-224").getOID().toString());
        assertEquals("2.16.840.1.101.3.4.2.4", AlgorithmId.get("SHA224").getOID().toString());
        assertEquals(
                "2.16.840.1.101.3.4.3.1", AlgorithmId.get("SHA224WithDSA").getOID().toString());
        assertEquals(
                "2.16.840.1.101.3.4.3.2", AlgorithmId.get("SHA256WithDSA").getOID().toString());
        // Case is irrelevant.
        assertEquals(
                "2.16.840.1.101.3.4.3.1", AlgorithmId.get("sHA224withDSA").getOID().toString());
        assertEquals(
                "2.16.840.1.101.3.4.3.2", AlgorithmId.get("sHA256withDSA").getOID().toString());
    }

    public void test_getName() throws Exception {
        assertEquals("SHA224", getOidName("2.16.840.1.101.3.4.2.4"));
        assertEquals("SHA224withDSA", getOidName("2.16.840.1.101.3.4.3.1"));
        assertEquals("SHA256withDSA", getOidName("2.16.840.1.101.3.4.3.2"));
        assertEquals("SHA224withRSA", getOidName("1.2.840.113549.1.1.14"));
    }

    private String getOidName(String oid) throws Exception {
        return new AlgorithmId(new ObjectIdentifier(oid)).getName();
    }
}

