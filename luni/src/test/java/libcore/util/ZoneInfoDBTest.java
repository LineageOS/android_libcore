/*
 * Copyright (C) 2013 The Android Open Source Project
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

package libcore.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TimeZone;

public class ZoneInfoDBTest extends junit.framework.TestCase {

  // The base tzdata file, always present on a device.
  private static final String TZDATA_IN_ROOT =
      System.getenv("ANDROID_ROOT") + "/usr/share/zoneinfo/tzdata";

  // An empty override file should fall back to the default file.
  public void testLoadTzDataWithFallback_emptyOverrideFile() throws Exception {
    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzData(TZDATA_IN_ROOT);
    String emptyFilePath = makeEmptyFile().getPath();

    ZoneInfoDB.TzData dataWithEmptyOverride =
        ZoneInfoDB.TzData.loadTzDataWithFallback(emptyFilePath, TZDATA_IN_ROOT);
    assertEquals(data.getVersion(), dataWithEmptyOverride.getVersion());
    assertEquals(data.getAvailableIDs().length, dataWithEmptyOverride.getAvailableIDs().length);
  }

  // A corrupt override file should fall back to the default file.
  public void testLoadTzDataWithFallback_corruptOverrideFile() throws Exception {
    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzData(TZDATA_IN_ROOT);
    String corruptFilePath = makeCorruptFile().getPath();

    ZoneInfoDB.TzData dataWithCorruptOverride =
        ZoneInfoDB.TzData.loadTzDataWithFallback(corruptFilePath, TZDATA_IN_ROOT);
    assertEquals(data.getVersion(), dataWithCorruptOverride.getVersion());
    assertEquals(data.getAvailableIDs().length, dataWithCorruptOverride.getAvailableIDs().length);
  }

  // Given no tzdata files we can use, we should fall back to built-in "GMT".
  public void testLoadTzDataWithFallback_noGoodFile() throws Exception {
    String emptyFilePath = makeEmptyFile().getPath();
    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzDataWithFallback(emptyFilePath);
    assertEquals("missing", data.getVersion());
    assertEquals(1, data.getAvailableIDs().length);
    assertEquals("GMT", data.getAvailableIDs()[0]);
  }

  // Given a valid override file, we should find ourselves using that.
  public void testLoadTzDataWithFallback_goodOverrideFile() throws Exception {
    RandomAccessFile in = new RandomAccessFile(TZDATA_IN_ROOT, "r");
    byte[] content = new byte[(int) in.length()];
    in.readFully(content);
    // Bump the version number to one long past where humans will be extinct.
    content[6] = '9';
    content[7] = '9';
    content[8] = '9';
    content[9] = '9';
    content[10] = 'z';
    in.close();

    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzData(TZDATA_IN_ROOT);
    File goodFile = makeTemporaryFile(content);
    try {
      ZoneInfoDB.TzData dataWithOverride =
              ZoneInfoDB.TzData.loadTzDataWithFallback(goodFile.getPath(), TZDATA_IN_ROOT);
      assertEquals("9999z", dataWithOverride.getVersion());
      assertEquals(data.getAvailableIDs().length, dataWithOverride.getAvailableIDs().length);
    } finally {
      goodFile.delete();
    }
  }

  // Confirms any caching that exists correctly handles TimeZone mutability.
  public void testMakeTimeZone_timeZoneMutability() throws Exception {
    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzData(TZDATA_IN_ROOT);
    String tzId = "Europe/London";
    ZoneInfo first = data.makeTimeZone(tzId);
    ZoneInfo second = data.makeTimeZone(tzId);
    assertNotSame(first, second);

    assertTrue(first.hasSameRules(second));

    first.setID("Not Europe/London");

    assertFalse(first.getID().equals(second.getID()));

    first.setRawOffset(3600);
    assertFalse(first.getRawOffset() == second.getRawOffset());
  }

  public void testMakeTimeZone_notFound() throws Exception {
    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzData(TZDATA_IN_ROOT);
    assertNull(data.makeTimeZone("THIS_TZ_DOES_NOT_EXIST"));
    assertFalse(data.hasTimeZone("THIS_TZ_DOES_NOT_EXIST"));
  }

  public void testMakeTimeZone_found() throws Exception {
    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzData(TZDATA_IN_ROOT);
    assertNotNull(data.makeTimeZone("Europe/London"));
    assertTrue(data.hasTimeZone("Europe/London"));
  }

  public void testGetRulesVersion() throws Exception {
    ZoneInfoDB.TzData data = ZoneInfoDB.TzData.loadTzData(TZDATA_IN_ROOT);

    String rulesVersion = ZoneInfoDB.TzData.getRulesVersion(new File(TZDATA_IN_ROOT));
    assertEquals(data.getVersion(), rulesVersion);
  }

  public void testGetRulesVersion_corruptFile() throws Exception {
    File corruptFilePath = makeCorruptFile();
    try {
      ZoneInfoDB.TzData.getRulesVersion(corruptFilePath);
      fail();
    } catch (IOException expected) {
    }
  }

  public void testGetRulesVersion_emptyFile() throws Exception {
    File emptyFilePath = makeEmptyFile();
    try {
      ZoneInfoDB.TzData.getRulesVersion(emptyFilePath);
      fail();
    } catch (IOException expected) {
    }
  }

  public void testGetRulesVersion_missingFile() throws Exception {
    File missingFile = makeMissingFile();
    try {
      ZoneInfoDB.TzData.getRulesVersion(missingFile);
      fail();
    } catch (IOException expected) {
    }
  }

  private static File makeMissingFile() throws Exception {
    File file = File.createTempFile("temp-", ".txt");
    assertTrue(file.delete());
    assertFalse(file.exists());
    return file;
  }

  private static File makeCorruptFile() throws Exception {
    return makeTemporaryFile("invalid content".getBytes());
  }

  private static File makeEmptyFile() throws Exception {
    return makeTemporaryFile(new byte[0]);
  }

  private static File makeTemporaryFile(byte[] content) throws Exception {
    File f = File.createTempFile("temp-", ".txt");
    FileOutputStream fos = new FileOutputStream(f);
    fos.write(content);
    fos.close();
    return f;
  }
}
