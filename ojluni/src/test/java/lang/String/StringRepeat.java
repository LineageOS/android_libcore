/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @test
 * @summary This exercises String#repeat patterns and limits.
 * @run main/othervm -Xmx4G StringRepeat
 */
package test.java.lang.String;

import org.testng.Assert;
import org.testng.annotations.Test;


public class StringRepeat {
    /*
     * Varitions of repeat count.
     */
    static int[] REPEATS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        32, 64, 128, 256, 512, 1024, 64 * 1024, 1024 * 1024,
        16 * 1024 * 1024
    };

    /*
     * Varitions of Strings.
     */
    static String[] STRINGS = new String[] {
            "", "\0",  " ", "a", "$", "\u2022",
            "ab", "abc", "abcd", "abcde",
            "The quick brown fox jumps over the lazy dog."
    };

    /*
     * Repeat String function tests.
     */
    @Test
    public void test1() {
        for (int repeat : REPEATS) {
            for (String string : STRINGS) {
                long limit = (long)string.length() * (long)repeat;

                // Android-changed: lowered max length limit
                // if ((long)(Integer.MAX_VALUE >> 1) <= limit) {
                if ((long)(Integer.MAX_VALUE >> 5) <= limit) {
                    break;
                }

                verify(string.repeat(repeat), string, repeat);
            }
        }
    }

    /*
     * Repeat String exception tests.
     */
    @Test
    public void test2() {
        try {
            "abc".repeat(-1);
            throw new RuntimeException("No exception for negative repeat count");
        } catch (IllegalArgumentException ex) {
            // Correct
        }

        try {
            "abc".repeat(Integer.MAX_VALUE - 1);
            throw new RuntimeException("No exception for large repeat count");
        } catch (OutOfMemoryError ex) {
            // Correct
        }
    }

    // Android-added: more tests
    @Test
    public void testEdgeCases() {
        Assert.assertThrows(IllegalArgumentException.class, () -> "a".repeat(-1));
        Assert.assertThrows(IllegalArgumentException.class, () -> "\u03B1".repeat(-1));
        Assert.assertThrows(OutOfMemoryError.class, () -> "\u03B1\u03B2".repeat(Integer.MAX_VALUE));
    }

    @Test
    public void testCompressed() {
        Assert.assertEquals("a".repeat(0), "");
        Assert.assertEquals("a".repeat(1), "a");
        Assert.assertEquals("a".repeat(5), "aaaaa");

        Assert.assertEquals("abc".repeat(0), "");
        Assert.assertEquals("abc".repeat(1), "abc");
        Assert.assertEquals("abc".repeat(5), "abcabcabcabcabc");
    }

    @Test
    public void testUncompressed() {
        Assert.assertEquals("\u2022".repeat(0), "");
        Assert.assertEquals("\u2022".repeat(1), "\u2022");
        Assert.assertEquals("\u2022".repeat(5), "\u2022\u2022\u2022\u2022\u2022");

        Assert.assertEquals("\u03B1\u03B2\u03B3".repeat(0), "");
        Assert.assertEquals("\u03B1\u03B2\u03B3".repeat(1), "αβγ");
        Assert.assertEquals("\u03B1\u03B2\u03B3".repeat(5), "αβγαβγαβγαβγαβγ");
    }

    static String truncate(String string) {
        if (string.length() < 80) {
            return string;
        }
        return string.substring(0, 80) + "...";
    }

    /*
     * Verify string repeat patterns.
     */
    static void verify(String result, String string, int repeat) {
        if (string.isEmpty() || repeat == 0) {
            if (!result.isEmpty()) {
                String message = String.format("\"%s\".repeat(%d)%n", truncate(string), repeat) +
                        String.format("Result \"%s\"%n", truncate(result)) +
                        String.format("Result expected to be empty, found string of length %d%n", result.length());
                Assert.fail(message);
            }
        } else {
            int expected = 0;
            int count = 0;
            for (int offset = result.indexOf(string, expected);
                 0 <= offset;
                 offset = result.indexOf(string, expected)) {
                count++;
                if (offset != expected) {
                    String message = String.format("\"%s\".repeat(%d)%n", truncate(string), repeat) +
                            String.format("Result \"%s\"%n", truncate(result)) +
                            String.format("Repeat expected at %d, found at = %d%n", expected, offset);
                    Assert.fail(message);
                }
                expected += string.length();
            }
            if (count != repeat) {
                String message = String.format("\"%s\".repeat(%d)%n", truncate(string), repeat) +
                        String.format("Result \"%s\"%n", truncate(result)) +
                        String.format("Repeat count expected to be %d, found %d%n", repeat, count);
                Assert.fail(message);
            }
        }
    }
}
