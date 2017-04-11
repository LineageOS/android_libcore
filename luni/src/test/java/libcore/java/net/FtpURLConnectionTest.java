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

package libcore.java.net;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * Tests URLConnections for ftp:// URLs.
 */
public class FtpURLConnectionTest extends TestCase {

    private static final String FILE_PATH = "test/file/for/FtpURLConnectionTest.txt";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String SERVER_HOSTNAME = "localhost";
    private static final String USER_HOME_DIR = "/home/user";

    // http://b/35784677
    public void testCRLFInUserinfo() throws Exception {
        int serverPort = 1234;
        // '/r/n' in the username, no password
        String url1String = String.format(Locale.US, "ftp://foo%%0D%%0Acommand@%s:%s/%s",
            SERVER_HOSTNAME, serverPort, FILE_PATH);
        // '/r/n' in the username with password
        String url2String = String.format(Locale.US, "ftp://foo%%0D%%0Acommand:foo@%s:%s/%s",
            SERVER_HOSTNAME, serverPort, FILE_PATH);
        // '/r/n' in the password
        String url3String = String.format(Locale.US, "ftp://foo:bar%%0D%%0Acommand@%s:%s/%s",
            SERVER_HOSTNAME, serverPort, FILE_PATH);
        // just '/r' in the password
        String url4String = String.format(Locale.US, "ftp://foo:bar%%0Dcommand@%s:%s/%s",
            SERVER_HOSTNAME, serverPort, FILE_PATH);
        // just '/n' in the username
        String url5String = String.format(Locale.US, "ftp://foo%%0Acommand:bar@%s:%s/%s",
            SERVER_HOSTNAME, serverPort, FILE_PATH);

        for (String urlString : new String[]{ url1String, url2String, url3String, url4String,
                url5String }) {
            try {
                new URL(urlString).openConnection();
                fail();
            } catch(IOException expected) {}
        }
    }
}
