/*
 * Copyright (C) 2018 The Android Open Source Project
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.icu.ICU;
import libcore.util.TimeZoneFinder;
import libcore.util.ZoneInfoDB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that compare ICU and libcore time zone behavior and similar cross-cutting concerns.
 */
public class TimeZoneIntegrationTest {

    // http://b/28949992
    @Test
    public void testJavaSetDefaultAppliesToIcuTimezone() {
        java.util.TimeZone origTz = java.util.TimeZone.getDefault();
        try {
            android.icu.util.TimeZone origIcuTz = android.icu.util.TimeZone.getDefault();
            assertEquals(origTz.getID(), origIcuTz.getID());

            java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT-05:00");
            java.util.TimeZone.setDefault(tz);
            android.icu.util.TimeZone icuTz = android.icu.util.TimeZone.getDefault();
            assertEquals(tz.getID(), icuTz.getID());
        } finally {
            java.util.TimeZone.setDefault(origTz);
        }
    }

    // http://b/30937209
    @Test
    public void testSetDefaultDeadlock() throws InterruptedException, BrokenBarrierException {
        // Since this tests a deadlock, the test has two fundamental problems:
        // - it is probabilistic: it's not guaranteed to fail if the problem exists
        // - if it fails, it will effectively hang the current runtime, as no other thread will
        //   be able to call TimeZone.getDefault()/setDefault() successfully any more.

        // 10 was too low to be reliable, 100 failed more than half the time (on a bullhead).
        final int iterations = 100;
        java.util.TimeZone otherTimeZone = java.util.TimeZone.getTimeZone("Europe/London");
        AtomicInteger setterCount = new AtomicInteger();
        CyclicBarrier startBarrier = new CyclicBarrier(2);
        Thread setter = new Thread(() -> {
            waitFor(startBarrier);
            for (int i = 0; i < iterations; i++) {
                java.util.TimeZone.setDefault(otherTimeZone);
                java.util.TimeZone.setDefault(null);
                setterCount.set(i+1);
            }
        });
        setter.setName("testSetDefaultDeadlock setter");

        AtomicInteger getterCount = new AtomicInteger();
        Thread getter = new Thread(() -> {
            waitFor(startBarrier);
            for (int i = 0; i < iterations; i++) {
                android.icu.util.TimeZone.getDefault();
                getterCount.set(i+1);
            }
        });
        getter.setName("testSetDefaultDeadlock getter");

        setter.start();
        getter.start();

        // 2 seconds is plenty: If successful, we usually complete much faster.
        setter.join(1000);
        getter.join(1000);
        if (setter.isAlive() || getter.isAlive()) {
            fail("Threads are still alive. Getter iteration count: " + getterCount.get()
                    + ", setter iteration count: " + setterCount.get());
        }
        // Guard against unexpected uncaught exceptions.
        assertEquals("Setter iterations", iterations, setterCount.get());
        assertEquals("Getter iterations", iterations, getterCount.get());
    }

    // http://b/30979219
    @Test
    public void testSetDefaultRace() throws InterruptedException {
        // Since this tests a race condition, the test is probabilistic: it's not guaranteed to
        // fail if the problem exists

        // These iterations are significantly faster than the ones in #testSetDefaultDeadlock
        final int iterations = 10000;
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        Thread.UncaughtExceptionHandler handler = (t, e) -> exceptions.add(e);

        CyclicBarrier startBarrier = new CyclicBarrier(2);
        Thread clearer = new Thread(() -> {
            waitFor(startBarrier);
            for (int i = 0; i < iterations; i++) {
                // This is not public API but can effectively be invoked via
                // java.util.TimeZone.setDefault. Call it directly to reduce the amount of code
                // involved in this test.
                android.icu.util.TimeZone.setICUDefault(null);
            }
        });
        clearer.setName("testSetDefaultRace clearer");
        clearer.setUncaughtExceptionHandler(handler);

        Thread getter = new Thread(() -> {
            waitFor(startBarrier);
            for (int i = 0; i < iterations; i++) {
                android.icu.util.TimeZone.getDefault();
            }
        });
        getter.setName("testSetDefaultRace getter");
        getter.setUncaughtExceptionHandler(handler);

        clearer.start();
        getter.start();

        // 20 seconds is plenty: If successful, we usually complete much faster.
        clearer.join(10000);
        getter.join(10000);

        if (!exceptions.isEmpty()) {
            Throwable firstException = exceptions.get(0);
            firstException.printStackTrace();
            fail("Threads did not succeed successfully: " + firstException);
        }
        assertFalse("clearer thread is still alive", clearer.isAlive());
        assertFalse("getter thread is still alive", getter.isAlive());
    }

    private static void waitFor(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Confirms that ICU agrees with the rest of libcore about the version of the TZ data in use.
     */
    @Test
    public void testTimeZoneDataVersion() {
        String icu4cTzVersion = ICU.getTZDataVersion();

        String zoneInfoTzVersion = ZoneInfoDB.getInstance().getVersion();
        assertEquals(icu4cTzVersion, zoneInfoTzVersion);

        String icu4jTzVersion = android.icu.util.TimeZone.getTZDataVersion();
        assertEquals(icu4jTzVersion, zoneInfoTzVersion);

        String tzLookupTzVersion = TimeZoneFinder.getInstance().getIanaVersion();
        assertEquals(icu4jTzVersion, tzLookupTzVersion);
    }

    /**
     * Confirms that ICU can recognize all the time zone IDs used by the ZoneInfoDB data.
     * ICU's IDs may be a superset.
     */
    @Test
    public void testTimeZoneIdLookup() {
        String[] zoneInfoDbAvailableIds = ZoneInfoDB.getInstance().getAvailableIDs();

        // ICU has a known set of IDs. We want ANY because we don't want to filter to ICU's
        // canonical IDs only.
        Set<String> icuAvailableIds = android.icu.util.TimeZone.getAvailableIDs(
                android.icu.util.TimeZone.SystemTimeZoneType.ANY, null /* region */,
                null /* rawOffset */);

        List<String> nonIcuAvailableIds = new ArrayList<>();
        List<String> creationFailureIds = new ArrayList<>();
        List<String> noCanonicalLookupIds = new ArrayList<>();
        List<String> nonSystemIds = new ArrayList<>();
        for (String zoneInfoDbId : zoneInfoDbAvailableIds) {
            if (!icuAvailableIds.contains(zoneInfoDbId)) {
                nonIcuAvailableIds.add(zoneInfoDbId);
            }

            boolean[] isSystemId = new boolean[1];
            String canonicalId = android.icu.util.TimeZone.getCanonicalID(zoneInfoDbId, isSystemId);
            if (canonicalId == null) {
                noCanonicalLookupIds.add(zoneInfoDbId);
            }
            if (!isSystemId[0]) {
                nonSystemIds.add(zoneInfoDbId);
            }

            android.icu.util.TimeZone icuTimeZone =
                    android.icu.util.TimeZone.getTimeZone(zoneInfoDbId);
            if (icuTimeZone.getID().equals(android.icu.util.TimeZone.UNKNOWN_ZONE_ID)) {
                creationFailureIds.add(zoneInfoDbId);
            }
        }
        assertTrue("Non-ICU available IDs: " + nonIcuAvailableIds
                        + ", creation failed IDs: " + creationFailureIds
                        + ", non-system IDs: " + nonSystemIds
                        + ", ids without canonical IDs: " + noCanonicalLookupIds,
                nonIcuAvailableIds.isEmpty()
                        && creationFailureIds.isEmpty()
                        && nonSystemIds.isEmpty()
                        && noCanonicalLookupIds.isEmpty());
    }

    // http://b/30527513
    @Test
    public void testDisplayNamesWithScript() throws Exception {
        Locale latinLocale = Locale.forLanguageTag("sr-Latn-RS");
        Locale cyrillicLocale = Locale.forLanguageTag("sr-Cyrl-RS");
        Locale noScriptLocale = Locale.forLanguageTag("sr-RS");
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("Europe/London");

        final String latinName = "Srednje vreme po Griniču";
        final String cyrillicName = "Средње време по Гриничу";

        // Check java.util.TimeZone
        assertEquals(latinName, tz.getDisplayName(latinLocale));
        assertEquals(cyrillicName, tz.getDisplayName(cyrillicLocale));
        assertEquals(cyrillicName, tz.getDisplayName(noScriptLocale));

        // Check ICU TimeZoneNames
        // The one-argument getDisplayName() override uses LONG_GENERIC style which is different
        // from what java.util.TimeZone uses. Force the LONG style to get equivalent results.
        final int style = android.icu.util.TimeZone.LONG;
        android.icu.util.TimeZone utz = android.icu.util.TimeZone.getTimeZone(tz.getID());
        assertEquals(latinName, utz.getDisplayName(false, style, latinLocale));
        assertEquals(cyrillicName, utz.getDisplayName(false, style, cyrillicLocale));
        assertEquals(cyrillicName, utz.getDisplayName(false, style, noScriptLocale));
    }
}
