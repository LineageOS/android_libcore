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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sun.net.ftp.FtpLoginException;

/**
 * Tests URLConnections for ftp:// URLs.
 */
public class FtpURLConnectionTest extends TestCase {

    private static final String FILE_PATH = "test/file/for/FtpURLConnectionTest.txt";
    private static final String SERVER_HOSTNAME = "localhost";

     // http://b/35784677
    public void testCRLFInUserinfo() throws Exception {
        List<String> encodedUserInfos = Arrays.asList(
                // '\r\n' in the username with password
                "user%0D%0Acommand:password",
                // '\r\n' in the password
                "user:password%0D%0Acommand",
                // just '\n' in the password
                "user:password%0Acommand",
                // just '\n' in the username
                "user%0Acommand:password"
        );
        for (String encodedUserInfo : encodedUserInfos) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            ServerSocket mockFtpServerSocket = new ServerSocket(0);
            Future<Void> future = executor.submit(new Callable<Void>() {
                @Override public Void call() throws Exception {
                    Socket clientSocket = mockFtpServerSocket.accept();
                    clientSocket.getOutputStream().write("220 o/".getBytes());
                    clientSocket.close();
                    return null;
                }
              });
            executor.shutdown();

            String urlString = String.format(Locale.US, "ftp://%s@%s:%s/%s",
                    encodedUserInfo, SERVER_HOSTNAME, mockFtpServerSocket.getLocalPort(), FILE_PATH);
            try {
                new URL(urlString).openConnection().connect();
                fail("Connection shouldn't have succeeded: " + urlString);
            } catch (FtpLoginException expected) {
                // The original message "Illegal carriage return" gets lost
                // where FtpURLConnection.connect() translates the
                // original FtpProtocolException into FtpLoginException.
                assertEquals("Invalid username/password", expected.getMessage());
            }

            // Cleanup
            future.get();
            mockFtpServerSocket.close();
        }
    }
}
