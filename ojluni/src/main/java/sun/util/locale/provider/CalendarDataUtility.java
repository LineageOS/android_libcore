/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.util.locale.provider;

import static java.util.Calendar.*;
import java.util.Locale;
import java.util.Map;

/**
 * {@code CalendarDataUtility} is a utility class for getting calendar field name values.
 *
 * @author Masayoshi Okutsu
 * @author Naoto Sato
 */
public class CalendarDataUtility {

    // No instantiation
    private CalendarDataUtility() {
    }

    // Android changed: Removed retrieveFirstDayOfWeek and retrieveMinimalDaysInFirstWeek.
    // use libcore.icu.LocaleData or android.icu.util.Calendar.WeekData instead

    public static String retrieveFieldValueName(String id, int field, int value, int style, Locale locale) {
/*
        LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        return pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, normalizeCalendarType(id),
                                       field, value, style, false);
*/
        return null;
    }

    public static String retrieveJavaTimeFieldValueName(String id, int field, int value, int style, Locale locale) {
/*
        LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        String name;
        name = pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, normalizeCalendarType(id),
                                       field, value, style, true);
        if (name == null) {
            name = pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, normalizeCalendarType(id),
                                           field, value, style, false);
        }
        return name;
*/
        return null;
    }

    public static Map<String, Integer> retrieveFieldValueNames(String id, int field, int style, Locale locale) {
/*
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        return pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale,
                                       normalizeCalendarType(id), field, style, false);
*/
        return null;
    }

    public static Map<String, Integer> retrieveJavaTimeFieldValueNames(String id, int field, int style, Locale locale) {
/*
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        Map<String, Integer> map;
        map = pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale,
                                       normalizeCalendarType(id), field, style, true);
        if (map == null) {
            map = pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale,
                                           normalizeCalendarType(id), field, style, false);
        }
        return map;
*/
        return null;
    }

    static String normalizeCalendarType(String requestID) {
        String type;
        if (requestID.equals("gregorian") || requestID.equals("iso8601")) {
            type = "gregory";
        } else if (requestID.startsWith("islamic")) {
            type = "islamic";
        } else {
            type = requestID;
        }
        return type;
    }

    // Android-changed: Removed CalendarFieldValueNameGetter, CalendarFieldValueNamesMapGetter
    // and CalendarWeekParameterGetter
}
