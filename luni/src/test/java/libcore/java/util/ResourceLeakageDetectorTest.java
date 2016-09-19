/*
 * Copyright (C) 2014 The Android Open Source Project
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
package libcore.java.util;

import dalvik.system.CloseGuard;
import junit.framework.TestCase;

/**
 * Test for {@link ResourceLeakageDetector}
 */
public class ResourceLeakageDetectorTest extends TestCase {
  /**
   * This test will not work on RI as it does not support the <code>CloseGuard</code> or similar
   * mechanism.
   */
  // TODO(paulduffin): b/31542223 - Work out why this is failing in CTS, fix and reenable.
  public void notestDetectsUnclosedCloseGuard() throws Exception {
    ResourceLeakageDetector detector = ResourceLeakageDetector.newDetector();
    try {
      CloseGuard closeGuard = createCloseGuard();
      closeGuard.open("open");
    } finally {
      boolean leaksDetected = true;
      try {
        System.logI("Checking for leaks");
        detector.checkForLeaks();
        leaksDetected = false;
      } catch (AssertionError expected) {
        // The leak detector should throw this error.
      }

      if (!leaksDetected) {
        fail("Did not detect any leaks");
      }
    }
  }

  public void testIgnoresClosedCloseGuard() throws Exception {
    ResourceLeakageDetector detector = ResourceLeakageDetector.newDetector();
    try {
      CloseGuard closeGuard = createCloseGuard();
      closeGuard.open("open");
      closeGuard.close();
    } finally {
      detector.checkForLeaks();
    }
  }

  /**
   * Private method to ensure that the CloseGuard object is garbage collected.
   */
  private CloseGuard createCloseGuard() {
    final CloseGuard closeGuard = CloseGuard.get();
    new Object() {
      @Override
      protected void finalize() throws Throwable {
        try {
          closeGuard.warnIfOpen();
        } finally {
          super.finalize();
        }
      }
    };

    return closeGuard;
  }
}
