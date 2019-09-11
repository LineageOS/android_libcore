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

package libcore.timezone;

import android.icu.util.TimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Information about a country's time zones.
 * @hide
 */
@libcore.api.CorePlatformApi
public final class CountryTimeZones {

    /**
     * The result of lookup up a time zone using offset information (and possibly more).
     * @hide
     */
    @libcore.api.CorePlatformApi
    public final static class OffsetResult {

        /** A zone that matches the supplied criteria. See also {@link #mOneMatch}. */
        @libcore.api.CorePlatformApi
        public final TimeZone mTimeZone;

        /** True if there is one match for the supplied criteria */
        @libcore.api.CorePlatformApi
        public final boolean mOneMatch;

        public OffsetResult(TimeZone timeZone, boolean oneMatch) {
            mTimeZone = java.util.Objects.requireNonNull(timeZone);
            mOneMatch = oneMatch;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "mTimeZone='" + mTimeZone + '\'' +
                    ", mOneMatch=" + mOneMatch +
                    '}';
        }
    }

    /**
     * A mapping to a time zone ID with some associated metadata.
     *
     * @hide
     */
    @libcore.api.CorePlatformApi
    public final static class TimeZoneMapping {
        @libcore.api.CorePlatformApi
        public final String timeZoneId;
        @libcore.api.CorePlatformApi
        public final boolean showInPicker;
        @libcore.api.CorePlatformApi
        public final Long notUsedAfter;

        /** Memoized TimeZone object for {@link #timeZoneId}. */
        private TimeZone timeZone;

        TimeZoneMapping(String timeZoneId, boolean showInPicker, Long notUsedAfter) {
            this.timeZoneId = Objects.requireNonNull(timeZoneId);
            this.showInPicker = showInPicker;
            this.notUsedAfter = notUsedAfter;
        }

        /**
         * Returns a {@link TimeZone} object for this mapping, or {@code null} if the ID is unknown.
         */
        @libcore.api.CorePlatformApi
        public TimeZone getTimeZone() {
            synchronized (this) {
                if (timeZone == null) {
                    TimeZone tz = TimeZone.getFrozenTimeZone(timeZoneId);
                    timeZone = tz;
                    if (TimeZone.UNKNOWN_ZONE_ID.equals(timeZone.getID())) {
                        // This shouldn't happen given the validation that takes place in
                        // createValidatedCountryTimeZones().
                        System.logW("Skipping invalid zone in TimeZoneMapping: " + timeZoneId);
                    }
                }
            }

            return TimeZone.UNKNOWN_ZONE_ID.equals(timeZone.getID()) ? null : timeZone;
        }

        /**
         * Returns {@code true} if the mapping is "effective" after {@code whenMillis}, i.e.
         * it is distinct from other "effective" times zones used in the country at/after that
         * time. This uses the {@link #notUsedAfter} metadata which ensures there is one time
         * zone remaining when there are multiple candidate zones with the same rules. The one
         * kept is based on country specific factors like population covered.
         */
        boolean isEffectiveAt(long whenMillis) {
            return notUsedAfter == null || whenMillis <= notUsedAfter;
        }

        // VisibleForTesting
        @libcore.api.CorePlatformApi
        public static TimeZoneMapping createForTests(
                String timeZoneId, boolean showInPicker, Long notUsedAfter) {
            return new TimeZoneMapping(timeZoneId, showInPicker, notUsedAfter);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TimeZoneMapping that = (TimeZoneMapping) o;
            return showInPicker == that.showInPicker &&
                    Objects.equals(timeZoneId, that.timeZoneId) &&
                    Objects.equals(notUsedAfter, that.notUsedAfter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeZoneId, showInPicker, notUsedAfter);
        }

        @Override
        public String toString() {
            return "TimeZoneMapping{"
                    + "timeZoneId='" + timeZoneId + '\''
                    + ", showInPicker=" + showInPicker
                    + ", notUsedAfter=" + notUsedAfter
                    + '}';
        }

        /**
         * Returns {@code true} if one of the supplied {@link TimeZoneMapping} objects is for the
         * specified time zone ID.
         */
        static boolean containsTimeZoneId(
                List<TimeZoneMapping> timeZoneMappings, String timeZoneId) {
            for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
                if (timeZoneMapping.timeZoneId.equals(timeZoneId)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final String countryIso;
    private final String defaultTimeZoneId;
    private final List<TimeZoneMapping> timeZoneMappings;
    private final boolean everUsesUtc;

    /**
     * Memoized frozen ICU TimeZone object for the default. Can be {@link TimeZone#UNKNOWN_ZONE} if
     * the {@link #defaultTimeZoneId} is missing or unrecognized.
     */
    private TimeZone defaultTimeZone;

    private CountryTimeZones(String countryIso, String defaultTimeZoneId, boolean everUsesUtc,
            List<TimeZoneMapping> timeZoneMappings) {
        this.countryIso = java.util.Objects.requireNonNull(countryIso);
        this.defaultTimeZoneId = defaultTimeZoneId;
        this.everUsesUtc = everUsesUtc;
        // Create a defensive copy of the mapping list.
        this.timeZoneMappings = Collections.unmodifiableList(new ArrayList<>(timeZoneMappings));
    }

    /**
     * Creates a {@link CountryTimeZones} object containing only known time zone IDs.
     */
    public static CountryTimeZones createValidated(String countryIso, String defaultTimeZoneId,
            boolean everUsesUtc, List<TimeZoneMapping> timeZoneMappings, String debugInfo) {

        // We rely on ZoneInfoDB to tell us what the known valid time zone IDs are. ICU may
        // recognize more but we want to be sure that zone IDs can be used with java.util as well as
        // android.icu and ICU is expected to have a superset.
        String[] validTimeZoneIdsArray = ZoneInfoDB.getInstance().getAvailableIDs();
        HashSet<String> validTimeZoneIdsSet = new HashSet<>(Arrays.asList(validTimeZoneIdsArray));
        List<TimeZoneMapping> validCountryTimeZoneMappings = new ArrayList<>();
        for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
            String timeZoneId = timeZoneMapping.timeZoneId;
            if (!validTimeZoneIdsSet.contains(timeZoneId)) {
                System.logW("Skipping invalid zone: " + timeZoneId + " at " + debugInfo);
            } else {
                validCountryTimeZoneMappings.add(timeZoneMapping);
            }
        }

        // We don't get too strict at runtime about whether the defaultTimeZoneId must be
        // one of the country's time zones because this is the data we have to use (we also
        // assume the data was validated by earlier steps). The default time zone ID must just
        // be a recognized zone ID: if it's not valid we leave it null.
        if (!validTimeZoneIdsSet.contains(defaultTimeZoneId)) {
            System.logW("Invalid default time zone ID: " + defaultTimeZoneId
                    + " at " + debugInfo);
            defaultTimeZoneId = null;
        }

        String normalizedCountryIso = normalizeCountryIso(countryIso);
        return new CountryTimeZones(
                normalizedCountryIso, defaultTimeZoneId, everUsesUtc, validCountryTimeZoneMappings);
    }

    /**
     * Returns the ISO code for the country.
     */
    @libcore.api.CorePlatformApi
    public String getCountryIso() {
        return countryIso;
    }

    /**
     * Returns true if the ISO code for the country is a match for the one specified.
     */
    @libcore.api.CorePlatformApi
    public boolean isForCountryCode(String countryIso) {
        return this.countryIso.equals(normalizeCountryIso(countryIso));
    }

    /**
     * Returns the default time zone for the country. Can return null in cases when no data is
     * available or the time zone ID provided to
     * {@link #createValidated(String, String, boolean, List, String)} was not recognized.
     */
    @libcore.api.CorePlatformApi
    public synchronized TimeZone getDefaultTimeZone() {
        if (defaultTimeZone == null) {
            TimeZone timeZone;
            if (defaultTimeZoneId == null) {
                timeZone = TimeZone.UNKNOWN_ZONE;
            } else {
                timeZone = TimeZone.getFrozenTimeZone(defaultTimeZoneId);
            }
            this.defaultTimeZone = timeZone;
        }
        return TimeZone.UNKNOWN_ZONE_ID.equals(defaultTimeZone.getID()) ? null : defaultTimeZone;
    }

    /**
     * Returns the default time zone ID for the country. Can return null in cases when no data is
     * available or the time zone ID provided to
     * {@link #createValidated(String, String, boolean, List, String)} was not recognized.
     */
    @libcore.api.CorePlatformApi
    public String getDefaultTimeZoneId() {
        return defaultTimeZoneId;
    }

    /**
     * Returns an immutable, ordered list of time zone mappings for the country in an undefined but
     * "priority" order. The list can be empty if there were no zones configured or the configured
     * zone IDs were not recognized.
     */
    @libcore.api.CorePlatformApi
    public List<TimeZoneMapping> getTimeZoneMappings() {
        return timeZoneMappings;
    }

    /**
     * Returns an immutable, ordered list of time zone mappings for the country in an undefined but
     * "priority" order, filtered so that only "effective" time zone IDs are returned. An
     * "effective" time zone is one that differs from another time zone used in the country after
     * {@code whenMillis}. The list can be empty if there were no zones configured or the configured
     * zone IDs were not recognized.
     */
    @libcore.api.CorePlatformApi
    public List<TimeZoneMapping> getEffectiveTimeZoneMappingsAt(long whenMillis) {
        ArrayList<TimeZoneMapping> filteredList = new ArrayList<>(timeZoneMappings.size());
        for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
            if (timeZoneMapping.isEffectiveAt(whenMillis)) {
                filteredList.add(timeZoneMapping);
            }
        }
        return Collections.unmodifiableList(filteredList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CountryTimeZones that = (CountryTimeZones) o;

        if (everUsesUtc != that.everUsesUtc) {
            return false;
        }
        if (!countryIso.equals(that.countryIso)) {
            return false;
        }
        if (!Objects.equals(defaultTimeZoneId, that.defaultTimeZoneId)) {
            return false;
        }
        return timeZoneMappings.equals(that.timeZoneMappings);
    }

    @Override
    public int hashCode() {
        int result = countryIso.hashCode();
        result = 31 * result + (defaultTimeZoneId != null ? defaultTimeZoneId.hashCode() : 0);
        result = 31 * result + timeZoneMappings.hashCode();
        result = 31 * result + (everUsesUtc ? 1 : 0);
        return result;
    }

    /**
     * Returns true if the country has at least one zone that is the same as UTC at the given time.
     */
    @libcore.api.CorePlatformApi
    public boolean hasUtcZone(long whenMillis) {
        // If the data tells us the country never uses UTC we don't have to check anything.
        if (!everUsesUtc) {
            return false;
        }

        for (TimeZoneMapping timeZoneMapping : getEffectiveTimeZoneMappingsAt(whenMillis)) {
            TimeZone timeZone = timeZoneMapping.getTimeZone();
            if (timeZone != null && timeZone.getOffset(whenMillis) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a time zone for the country, if there is one, that matches the desired properties. If
     * there are multiple matches and the {@code bias} is one of them then it is returned, otherwise
     * an arbitrary match is returned based on the {@link #getEffectiveTimeZoneMappingsAt(long)}
     * ordering.
     *
     * @param totalOffsetMillis the offset from UTC at {@code whenMillis}
     * @param isDst the Daylight Savings Time state at {@code whenMillis}. {@code true} means DST,
     *     {@code false} means not DST, {@code null} means unknown
     * @param dstOffsetMillis the part of {@code totalOffsetMillis} contributed by DST, only used if
     *     {@code isDst} is {@code true}. The value can be {@code null} if the DST offset is
     *     unknown
     * @param whenMillis the UTC time to match against
     * @param bias the time zone to prefer, can be {@code null}
     */
    @libcore.api.CorePlatformApi
    public OffsetResult lookupByOffsetWithBias(int totalOffsetMillis, Boolean isDst,
            Integer dstOffsetMillis, long whenMillis, TimeZone bias) {
        List<TimeZoneMapping> timeZoneMappings = getEffectiveTimeZoneMappingsAt(whenMillis);
        if (timeZoneMappings.isEmpty()) {
            return null;
        }

        TimeZone firstMatch = null;
        boolean biasMatched = false;
        boolean oneMatch = true;
        for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
            TimeZone match = timeZoneMapping.getTimeZone();
            if (match == null || !offsetMatchesAtTime(match, totalOffsetMillis, isDst,
                    dstOffsetMillis, whenMillis)) {
                continue;
            }

            if (firstMatch == null) {
                firstMatch = match;
            } else {
                oneMatch = false;
            }
            if (bias != null && match.getID().equals(bias.getID())) {
                biasMatched = true;
            }
            if (firstMatch != null && !oneMatch && (bias == null || biasMatched)) {
                break;
            }
        }
        if (firstMatch == null) {
            return null;
        }

        TimeZone toReturn = biasMatched ? bias : firstMatch;
        return new OffsetResult(toReturn, oneMatch);
    }

    /**
     * Returns {@code true} if the specified {@code totalOffset}, {@code isDst},
     * {@code dstOffsetMillis} would be valid in the {@code timeZone} at time {@code whenMillis}.
     * {@code totalOffetMillis} is always matched.
     * If {@code isDst} is {@code null} this means the DST state is unknown, so
     * {@code dstOffsetMillis} is ignored.
     * If {@code isDst} is {@code false}, {@code dstOffsetMillis} is ignored.
     * If {@code isDst} is {@code true}, the DST state is considered. When considering DST state
     * {@code dstOffsetMillis} can be {@code null} if it is unknown but when {@code dstOffsetMillis}
     * is known then it is also matched.
     */
    private static boolean offsetMatchesAtTime(TimeZone timeZone, int totalOffsetMillis,
            Boolean isDst, Integer dstOffsetMillis, long whenMillis) {
        int[] offsets = new int[2];
        timeZone.getOffset(whenMillis, false /* local */, offsets);

        if (totalOffsetMillis != (offsets[0] + offsets[1])) {
            return false;
        }

        if (isDst == null) {
            return true;
        } else if (!isDst) {
            return offsets[1] == 0;
        } else {
            // isDst
            return (dstOffsetMillis == null && offsets[1] != 0)
                    || (dstOffsetMillis != null && dstOffsetMillis == offsets[1]);
        }
    }

    private static String normalizeCountryIso(String countryIso) {
        // Lowercase ASCII is normalized for the purposes of the code in this class.
        return countryIso.toLowerCase(Locale.US);
    }
}
