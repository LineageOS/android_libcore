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

package libcore.libcore.util;

import org.junit.Test;

import android.icu.util.TimeZone;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import libcore.util.CountryTimeZones;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CountryTimeZonesTest {

    private static final int HOUR_MILLIS = 60 * 60 * 1000;

    // Zones used in the tests. NEW_YORK_TZ and LONDON_TZ chosen because they never overlap but both
    // have DST.
    private static final TimeZone NEW_YORK_TZ = TimeZone.getTimeZone("America/New_York");
    private static final TimeZone LONDON_TZ = TimeZone.getTimeZone("Europe/London");
    // A zone that matches LONDON_TZ for WHEN_NO_DST. It does not have DST so differs for WHEN_DST.
    private static final TimeZone REYKJAVIK_TZ = TimeZone.getTimeZone("Atlantic/Reykjavik");
    // Another zone that matches LONDON_TZ for WHEN_NO_DST. It does not have DST so differs for
    // WHEN_DST.
    private static final TimeZone UTC_TZ = TimeZone.getTimeZone("Etc/UTC");

    // 22nd July 2017, 13:14:15 UTC (DST time in all the timezones used in these tests that observe
    // DST).
    private static final long WHEN_DST = 1500729255000L;
    // 22nd January 2018, 13:14:15 UTC (non-DST time in all timezones used in these tests).
    private static final long WHEN_NO_DST = 1516626855000L;

    private static final int LONDON_DST_OFFSET_MILLIS = HOUR_MILLIS;
    private static final int LONDON_NO_DST_OFFSET_MILLIS = 0;

    private static final int NEW_YORK_DST_OFFSET_MILLIS = -4 * HOUR_MILLIS;
    private static final int NEW_YORK_NO_DST_OFFSET_MILLIS = -5 * HOUR_MILLIS;

    @Test
    public void createValidated() throws Exception {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "gb", "Europe/London", list("Europe/London"), "test");
        assertEquals("gb", countryTimeZones.getCountryIso());
        assertEquals("Europe/London", countryTimeZones.getDefaultTimeZoneId());
        assertZoneEquals(zone("Europe/London"), countryTimeZones.getDefaultTimeZone());
        assertEquals(list("Europe/London"), countryTimeZones.getTimeZoneIds());
        assertZonesEqual(zones("Europe/London"), countryTimeZones.getIcuTimeZones());
    }

    @Test
    public void createValidated_nullDefault() throws Exception {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "gb", null, list("Europe/London"), "test");
        assertNull(countryTimeZones.getDefaultTimeZoneId());
    }

    @Test
    public void createValidated_invalidDefault() throws Exception {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "gb", "Moon/Tranquility_Base", list("Europe/London", "Moon/Tranquility_Base"),
                "test");

        // "Moon/Tranquility_Base" is not a valid time zone ID so should not be used.
        assertNull(countryTimeZones.getDefaultTimeZoneId());
        assertEquals(list("Europe/London"), countryTimeZones.getTimeZoneIds());
        assertZonesEqual(zones("Europe/London"), countryTimeZones.getIcuTimeZones());
    }

    @Test
    public void createValidated_unknownTimeZoneIdIgnored() throws Exception {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "gb", "Europe/London", list("Unknown_Id", "Europe/London"), "test");
        assertEquals(list("Europe/London"), countryTimeZones.getTimeZoneIds());
        assertZonesEqual(zones("Europe/London"), countryTimeZones.getIcuTimeZones());
    }

    @Test
    public void structuresAreImmutable() throws Exception {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "gb", "Europe/London", list("Europe/London"), "test");

        assertImmutableTimeZone(countryTimeZones.getDefaultTimeZone());

        List<TimeZone> tzList = countryTimeZones.getIcuTimeZones();
        assertEquals(1, tzList.size());
        assertImmutableList(tzList);
        assertImmutableTimeZone(tzList.get(0));

        List<String> tzIdList = countryTimeZones.getTimeZoneIds();
        assertEquals(1, tzIdList.size());
        assertImmutableList(tzIdList);
    }

    @Test
    public void lookupByOffsetWithBias_oneCandidate() throws Exception {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "gb", "Europe/London", list("Europe/London"), "test");

        // The three parameters match the configured zone: offset, isDst and when.
        assertZoneEquals(LONDON_TZ,
                countryTimeZones.lookupByOffsetWithBias(LONDON_DST_OFFSET_MILLIS,
                        true /* isDst */, WHEN_DST, null /* bias */));
        assertZoneEquals(LONDON_TZ,
                countryTimeZones.lookupByOffsetWithBias(LONDON_NO_DST_OFFSET_MILLIS,
                        false /* isDst */, WHEN_NO_DST, null /* bias */));

        // Some lookup failure cases where the offset, isDst and when do not match the configured
        // zone.
        TimeZone noDstMatch1 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, true /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch1);

        TimeZone noDstMatch2 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, false /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch2);

        TimeZone noDstMatch3 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_NO_DST_OFFSET_MILLIS, true /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch3);

        TimeZone noDstMatch4 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_NO_DST_OFFSET_MILLIS, true /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch4);

        TimeZone noDstMatch5 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, false /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch5);

        TimeZone noDstMatch6 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_NO_DST_OFFSET_MILLIS, false /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch6);

        // Some bias cases below.

        // The bias is irrelevant here: it matches what would be returned anyway.
        assertZoneEquals(LONDON_TZ,
                countryTimeZones.lookupByOffsetWithBias(LONDON_DST_OFFSET_MILLIS,
                        true /* isDst */, WHEN_DST, LONDON_TZ /* bias */));
        assertZoneEquals(LONDON_TZ,
                countryTimeZones.lookupByOffsetWithBias(LONDON_NO_DST_OFFSET_MILLIS,
                        false /* isDst */, WHEN_NO_DST, LONDON_TZ /* bias */));
        // A sample of a non-matching case with bias.
        assertNull(countryTimeZones.lookupByOffsetWithBias(LONDON_DST_OFFSET_MILLIS,
                true /* isDst */, WHEN_NO_DST, LONDON_TZ /* bias */));

        // The bias should be ignored: it doesn't match any of the country's zones.
        assertZoneEquals(LONDON_TZ,
                countryTimeZones.lookupByOffsetWithBias(LONDON_DST_OFFSET_MILLIS,
                        true /* isDst */, WHEN_DST, NEW_YORK_TZ /* bias */));

        // The bias should still be ignored even though it matches the offset information given:
        // it doesn't match any of the country's configured zones.
        assertNull(countryTimeZones.lookupByOffsetWithBias(NEW_YORK_DST_OFFSET_MILLIS,
                true /* isDst */, WHEN_DST, NEW_YORK_TZ /* bias */));
    }

    @Test
    public void lookupByOffsetWithBias_multipleNonOverlappingCandidates()
            throws Exception {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "xx", "Europe/London", list("America/New_York", "Europe/London"), "test");

        // The three parameters match the configured zone: offset, isDst and when.
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, true /* isDst */, WHEN_DST, null /* bias */));
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(
                LONDON_NO_DST_OFFSET_MILLIS, false /* isDst */, WHEN_NO_DST, null /* bias */));
        assertZoneEquals(NEW_YORK_TZ, countryTimeZones.lookupByOffsetWithBias(
                NEW_YORK_DST_OFFSET_MILLIS, true /* isDst */, WHEN_DST, null /* bias */));
        assertZoneEquals(NEW_YORK_TZ, countryTimeZones.lookupByOffsetWithBias(
                NEW_YORK_NO_DST_OFFSET_MILLIS, false /* isDst */, WHEN_NO_DST, null /* bias */));

        // Some lookup failure cases where the offset, isDst and when do not match the configured
        // zone. This is a sample, not complete.
        TimeZone noDstMatch1 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, true /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch1);

        TimeZone noDstMatch2 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, false /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch2);

        TimeZone noDstMatch3 = countryTimeZones.lookupByOffsetWithBias(
                NEW_YORK_NO_DST_OFFSET_MILLIS, true /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch3);

        TimeZone noDstMatch4 = countryTimeZones.lookupByOffsetWithBias(
                NEW_YORK_NO_DST_OFFSET_MILLIS, true /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch4);

        TimeZone noDstMatch5 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, false /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch5);

        TimeZone noDstMatch6 = countryTimeZones.lookupByOffsetWithBias(
                LONDON_NO_DST_OFFSET_MILLIS, false /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch6);

        // Some bias cases below.

        // The bias is irrelevant here: it matches what would be returned anyway.
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, true /* isDst */, WHEN_DST, LONDON_TZ /* bias */));
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(
                LONDON_NO_DST_OFFSET_MILLIS, false /* isDst */, WHEN_NO_DST, LONDON_TZ /* bias */));
        // A sample of a non-matching case with bias.
        assertNull(countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, true /* isDst */, WHEN_NO_DST, LONDON_TZ /* bias */));

        // The bias should be ignored: it matches a configured zone, but the offset is wrong so
        // should not be considered a match.
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, true /* isDst */, WHEN_DST, NEW_YORK_TZ /* bias */));
    }

    // This is an artificial case very similar to America/Denver and America/Phoenix in the US: both
    // have the same offset for 6 months of the year but diverge. Australia/Lord_Howe too.
    @Test
    public void lookupByOffsetWithBias_multipleOverlappingCandidates() throws Exception {
        // Three zones that have the same offset for some of the year. Europe/London changes
        // offset WHEN_DST, the others do not.
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "xx", "Europe/London", list("Atlantic/Reykjavik", "Europe/London", "Etc/UTC"),
                "test");

        // This is the no-DST offset for LONDON_TZ, REYKJAVIK_TZ. UTC_TZ.
        final int noDstOffset = LONDON_NO_DST_OFFSET_MILLIS;
        // This is the DST offset for LONDON_TZ.
        final int dstOffset = LONDON_DST_OFFSET_MILLIS;

        // The three parameters match the configured zone: offset, isDst and when.
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(dstOffset,
                true /* isDst */, WHEN_DST, null /* bias */));
        assertZoneEquals(REYKJAVIK_TZ, countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                false /* isDst */, WHEN_NO_DST, null /* bias */));
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(dstOffset,
                true /* isDst */, WHEN_DST, null /* bias */));
        assertZoneEquals(REYKJAVIK_TZ, countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                false /* isDst */, WHEN_NO_DST, null /* bias */));
        assertZoneEquals(REYKJAVIK_TZ, countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                false /* isDst */, WHEN_DST, null /* bias */));

        // Some lookup failure cases where the offset, isDst and when do not match the configured
        // zones.
        TimeZone noDstMatch1 = countryTimeZones.lookupByOffsetWithBias(dstOffset,
                true /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch1);

        TimeZone noDstMatch2 = countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                true /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch2);

        TimeZone noDstMatch3 = countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                true /* isDst */, WHEN_NO_DST, null /* bias */);
        assertNull(noDstMatch3);

        TimeZone noDstMatch4 = countryTimeZones.lookupByOffsetWithBias(dstOffset,
                false /* isDst */, WHEN_DST, null /* bias */);
        assertNull(noDstMatch4);


        // Some bias cases below.

        // The bias is relevant here: it overrides what would be returned naturally.
        assertZoneEquals(REYKJAVIK_TZ, countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                false /* isDst */, WHEN_NO_DST, null /* bias */));
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                false /* isDst */, WHEN_NO_DST, LONDON_TZ /* bias */));
        assertZoneEquals(UTC_TZ, countryTimeZones.lookupByOffsetWithBias(noDstOffset,
                false /* isDst */, WHEN_NO_DST, UTC_TZ /* bias */));

        // The bias should be ignored: it matches a configured zone, but the offset is wrong so
        // should not be considered a match.
        assertZoneEquals(LONDON_TZ, countryTimeZones.lookupByOffsetWithBias(
                LONDON_DST_OFFSET_MILLIS, true /* isDst */, WHEN_DST, REYKJAVIK_TZ /* bias */));
    }

    @Test
    public void isDefaultOkForCountryTimeZoneDetection_noZones() {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "xx", "Europe/London", list(), "test");
        assertFalse(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_DST));
        assertFalse(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_NO_DST));
    }

    @Test
    public void isDefaultOkForCountryTimeZoneDetection_oneZone() {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "xx", "Europe/London", list("Europe/London"), "test");
        assertTrue(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_DST));
        assertTrue(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_NO_DST));
    }

    @Test
    public void isDefaultOkForCountryTimeZoneDetection_twoZones_overlap() {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "xx", "Europe/London", list("Europe/London", "Etc/UTC"), "test");
        // Europe/London is the same as UTC in the Winter, so all the zones have the same offset
        // in Winter, but not in Summer.
        assertFalse(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_DST));
        assertTrue(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_NO_DST));
    }

    @Test
    public void isDefaultOkForCountryTimeZoneDetection_twoZones_noOverlap() {
        CountryTimeZones countryTimeZones = CountryTimeZones.createValidated(
                "xx", "Europe/London", list("Europe/London", "America/New_York"), "test");
        // The zones have different offsets all year, so it would never be ok to use the default
        // zone for the country of "xx".
        assertFalse(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_DST));
        assertFalse(countryTimeZones.isDefaultOkForCountryTimeZoneDetection(WHEN_NO_DST));
    }

    private void assertImmutableTimeZone(TimeZone timeZone) {
        try {
            timeZone.setRawOffset(1000);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    private static <X> void assertImmutableList(List<X> list) {
        try {
            list.add(null);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    private static void assertZoneEquals(TimeZone expected, TimeZone actual) {
        // TimeZone.equals() only checks the ID, but that's ok for these tests.
        assertEquals(expected, actual);
    }

    private static void assertZonesEqual(List<TimeZone> expected, List<TimeZone> actual) {
        // TimeZone.equals() only checks the ID, but that's ok for these tests.
        assertEquals(expected, actual);
    }

    private static <X> List<X> list(X... values) {
        return Arrays.asList(values);
    }

    private static TimeZone zone(String id) {
        return TimeZone.getTimeZone(id);
    }

    private static List<TimeZone> zones(String... ids) {
        return Arrays.stream(ids).map(TimeZone::getTimeZone).collect(Collectors.toList());
    }
}
