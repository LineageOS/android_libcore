/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.crypto.tests.javax.crypto.interfaces;

import org.junit.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests for <code>DHPublicKey</code> class field
 *
 */
public class DHPublicKeyTest {

    /**
     * Test for <code>serialVersionUID</code> field
     */
    @Test
    public void testField() {
        checkDHPublicKey key = new checkDHPublicKey();
        assertEquals("Incorrect serialVersionUID",
                key.getSerVerUID(), //DHPublicKey.serialVersionUID
                -6628103563352519193L);
    }

    @Test(timeout = 30 * 60 * 1000) // http://b/30741591
    public void test_getParams() throws Exception {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("DH");
        kg.initialize(1024);
        KeyPair kp1 = kg.genKeyPair();
        KeyPair kp2 = kg.genKeyPair();
        DHPublicKey pk1 = (DHPublicKey) kp1.getPublic();
        DHPublicKey pk2 = (DHPublicKey) kp2.getPublic();

        assertTrue(pk1.getY().getClass().getCanonicalName().equals("java.math.BigInteger"));
        assertTrue(pk2.getParams().getClass().getCanonicalName().equals("javax.crypto.spec.DHParameterSpec"));
        assertFalse(pk1.equals(pk2));
        assertTrue(pk1.getY().equals(pk1.getY()));
    }

    public class checkDHPublicKey implements DHPublicKey {
        public String getAlgorithm() {
            return "SecretKey";
        }
        public String getFormat() {
            return "Format";
        }
        public byte[] getEncoded() {
            return new byte[0];
        }
        public long getSerVerUID() {
            return serialVersionUID;
        }
        public BigInteger getY() {
            return null;
        }
        public DHParameterSpec getParams() {
            return null;
        }
    }
}
