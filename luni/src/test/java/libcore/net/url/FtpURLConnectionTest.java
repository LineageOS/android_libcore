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

package libcore.net.url;

import java.io.IOException;

import junit.framework.TestCase;

import libcore.net.url.FtpURLConnection;

public class FtpURLConnectionTest extends TestCase {
    public void testValidateCommand() throws Exception {
        FtpURLConnection.validateCommand("\r\n");
        FtpURLConnection.validateCommand("USER foo\r\n");

        try {
            FtpURLConnection.validateCommand("\r");
            fail();
        } catch (IOException expected) {
        }

        try {
            FtpURLConnection.validateCommand("\n");
            fail();
        } catch (IOException expected) {
        }

        try {
            FtpURLConnection.validateCommand("USER foo\rbar\r\n");
            fail();
        } catch (IOException expected) {
        }

        try {
            FtpURLConnection.validateCommand("USER foo\nbar\r\n");
            fail();
        } catch (IOException expected) {
        }

        try {
            FtpURLConnection.validateCommand("USER foo\r\nbar\r\n");
            fail();
        } catch (IOException expected) {
        }
    }
}
