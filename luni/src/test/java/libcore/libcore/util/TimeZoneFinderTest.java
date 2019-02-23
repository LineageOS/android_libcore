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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.icu.util.TimeZone;

import java.util.List;
import libcore.util.TimeZoneFinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class TimeZoneFinderTest {

    @Test
    public void getInstance() throws Exception {
        TimeZoneFinder finder1 = TimeZoneFinder.getInstance();
        TimeZoneFinder finder2 = TimeZoneFinder.getInstance();
        assertSame(finder1, finder2);
    }

    @Test
    public void lookupTimeZonesByCountry() throws Exception {
        TimeZoneFinder finder = TimeZoneFinder.createInstanceForTests(
                "<timezones ianaversion=\"2017b\">\n"
                + "  <countryzones>\n"
                + "    <country code=\"gb\" default=\"Europe/London\" everutc=\"y\">\n"
                + "      <id>Europe/London</id>\n"
                + "    </country>\n"
                + "  </countryzones>\n"
                + "</timezones>\n");

        List<TimeZone> gbList = finder.lookupTimeZonesByCountry("gb");
        assertEquals(1, gbList.size());
        assertImmutableList(gbList);
        assertImmutableTimeZone(gbList.get(0));

        // Check country code normalization works too.
        assertEquals(1, finder.lookupTimeZonesByCountry("GB").size());

        assertNull(finder.lookupTimeZonesByCountry("unknown"));
    }

    private static void assertImmutableTimeZone(TimeZone timeZone) {
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
}
