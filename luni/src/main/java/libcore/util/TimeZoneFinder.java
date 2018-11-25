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

package libcore.util;

import android.icu.util.TimeZone;

import java.util.List;

// Used by com.google.android.setupwizard.hiddenapi.reflection.TimeZoneFinderReflection
// Used by org.robolectric.shadows.ShadowTimeZoneFinder
// Used by org.robolectric.shadows.ShadowTimeZoneFinderTest
/**
 * A shim class over {@link libcore.timezone.TimeZoneFinder} which used to be in
 * {@code libcore.util}. This class provides just enough API to keep robolectric and SUW
 * (setup wizard) working util those have been updated to use replacement public SDK APIs or adjust
 * to the new package. See http://b/119921242 and http://b/116544863.
 * @hide
 */
public final class TimeZoneFinder {

    private static TimeZoneFinder instance;
    private final libcore.timezone.TimeZoneFinder delegate;

    private TimeZoneFinder(libcore.timezone.TimeZoneFinder delegate) {
        this.delegate = delegate;
    }

    // Used by com.google.android.setupwizard.hiddenapi.reflection.TimeZoneFinderReflection
    // Used by org.robolectric.shadows.ShadowTimeZoneFinderTest
    /**
     * Obtains an instance for use when resolving time zones. This method never returns
     * {@code null}.
     */
    public static TimeZoneFinder getInstance() {
        synchronized(TimeZoneFinder.class) {
            if (instance == null) {
                instance = new TimeZoneFinder(libcore.timezone.TimeZoneFinder.getInstance());
            }
        }
        return instance;
    }

    // Used by org.robolectric.shadows.ShadowTimeZoneFinder
    /** Used to create an instance using an in-memory XML String instead of a file. */
    public static TimeZoneFinder createInstanceForTests(String xml) {
        return new TimeZoneFinder(libcore.timezone.TimeZoneFinder.createInstanceForTests(xml));
    }

    // Used by com.google.android.setupwizard.hiddenapi.reflection.TimeZoneFinderReflection
    // Used by org.robolectric.shadows.ShadowTimeZoneFinderTest
    /**
     * Returns an immutable list of frozen ICU time zones known to be used in the specified country.
     * If the country code is not recognized or there is an error during lookup this can return
     * null. The TimeZones returned will never contain {@link TimeZone#UNKNOWN_ZONE}. This method
     * can return an empty list in a case when the underlying data files reference only unknown
     * zone IDs.
     */
    public List<TimeZone> lookupTimeZonesByCountry(String countryIso) {
        return delegate.lookupTimeZonesByCountry(countryIso);
    }
}
