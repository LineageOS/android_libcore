/*
 * Copyright (C) 2022 The Android Open Source Project
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

import static org.junit.Assert.assertThrows;

import java.util.SplittableRandom;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SplittableRandomTest {

    @Test
    public void testNextBoolean() {
        long seed = 0x1234567890L;
        SplittableRandom random1 = new SplittableRandom(seed);
        SplittableRandom random2 = new SplittableRandom(seed);

        assertEquals(random1, random2);
    }

    @Test
    public void split_throwsNullWhenSourceIsNull() {
        SplittableRandom random = new SplittableRandom(42);

        assertThrows(NullPointerException.class, () -> random.split(/* source= */ null));
    }

    @Test
    public void splitInstances_areTheSameWhenTheyAreSplitWithIdenticalSource() {
        var seed = 1001;
        var random1 = new SplittableRandom(seed);
        var random2 = new SplittableRandom(seed);

        var sourceSeed = 9999;

        var splitRandom1 = random1.split(new SplittableRandom(sourceSeed));
        var splitRandom2 = random2.split(new SplittableRandom(sourceSeed));

        assertEquals(splitRandom1, splitRandom2);
    }

    private static void assertEquals(SplittableRandom random1, SplittableRandom random2) {
        for (int i = 0; i < 1_000; ++i) {
            Assert.assertEquals(random1.nextLong(), random2.nextLong());
        }
    }

}
