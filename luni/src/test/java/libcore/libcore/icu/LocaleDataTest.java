/*
 * Copyright (C) 2012 The Android Open Source Project
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

package libcore.libcore.icu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.icu.text.DateTimePatternGenerator;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import libcore.icu.LocaleData;
import libcore.junit.util.SwitchTargetSdkVersionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LocaleDataTest {

  @Rule
  public TestRule switchTargetSdkVersionRule = SwitchTargetSdkVersionRule.getInstance();

  @Test
  public void test_en_US() throws Exception {
    LocaleData l = LocaleData.get(Locale.US);
    assertEquals("AM", l.amPm[0]);

    assertEquals("BC", l.eras[0]);

    assertEquals("January", l.longMonthNames[0]);
    assertEquals("Jan", l.shortMonthNames[0]);
    assertEquals("J", l.tinyMonthNames[0]);

    assertEquals("January", l.longStandAloneMonthNames[0]);
    assertEquals("Jan", l.shortStandAloneMonthNames[0]);
    assertEquals("J", l.tinyStandAloneMonthNames[0]);

    assertEquals("Sunday", l.longWeekdayNames[1]);
    assertEquals("Sun", l.shortWeekdayNames[1]);
    assertEquals("S", l.tinyWeekdayNames[1]);

    assertEquals("Sunday", l.longStandAloneWeekdayNames[1]);
    assertEquals("Sun", l.shortStandAloneWeekdayNames[1]);
    assertEquals("S", l.tinyStandAloneWeekdayNames[1]);

  }

  @Test
  public void test_cs_CZ() throws Exception {
    LocaleData l = LocaleData.get(new Locale("cs", "CZ"));

    assertEquals("ledna", l.longMonthNames[0]);
    assertEquals("led", l.shortMonthNames[0]);
    assertEquals("1", l.tinyMonthNames[0]);

    assertEquals("leden", l.longStandAloneMonthNames[0]);
    assertEquals("led", l.shortStandAloneMonthNames[0]);
    assertEquals("1", l.tinyStandAloneMonthNames[0]);
  }

  @Test
  public void test_ru_RU() throws Exception {
    LocaleData l = LocaleData.get(new Locale("ru", "RU"));

    assertEquals("воскресенье", l.longWeekdayNames[1]);
    assertEquals("вс", l.shortWeekdayNames[1]);
    assertEquals("вс", l.tinyWeekdayNames[1]);

    // Russian stand-alone weekday names have no initial capital since CLDR 28/ICU 56.
    assertEquals("воскресенье", l.longStandAloneWeekdayNames[1]);
    assertEquals("вс", l.shortStandAloneWeekdayNames[1]);
    assertEquals("В", l.tinyStandAloneWeekdayNames[1]);
  }

  /**
   * Check that LocaleData.get() does not throw when the input locale is invalid.
   * http://b/129070579
   */
  @Test
  public void testInvalidLocale() {
    LocaleData.get(new Locale("invalidLocale"));
  }

}
