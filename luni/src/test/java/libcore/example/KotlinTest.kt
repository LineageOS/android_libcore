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

package libcore.example

import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * An example unit test written in Kotlin.
 */
@RunWith(JUnit4::class)
class KotlinTest {

    @Test
    fun passing() {
        assertEquals(3, 1 + 2)
    }

    // Comment out the @Ignore annotation if you want to see a failing test.
    @Test
    @Ignore("It fails")
    fun failing() {
        assertEquals(5, 2 + 2)
    }

    @Test
    fun usingStandardLibrary() {
        val numbers = listOf(1, 2, 3)
        val actualStrings = numbers.map { n -> n.toString() }
        val expectedStrings = listOf("1", "2", "3")
        assertEquals(expectedStrings, actualStrings)
    }
}
