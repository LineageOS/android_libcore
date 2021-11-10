/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

// BEGIN Android-added: Clarification for usage of StringUTF16 and not StringLatin1.
/**
 * In upstream, this class (StringUTF16) contains implementations of various APIs and their helper
 * methods for uncompressed strings. Libcore makes use of it to implement APIs for both compressed
 * and uncompressed strings.
 *
 * Upstream also has StringLatin1 class which contains same implementations for
 * compressed strings. In our case we use only StringUTF16 (and StringLatin1 is intentionally not
 * imported) as string characters are managed by ART and everything is implemented in terms of
 * {@link String#charAt(int)} an other primitives which do not have direct access to underlying
 * character array. From those two, if we choose to implement everything in those primitives,
 * StringUTF16 has less difference with upstream, so it is chosen here.
 */
// END Android-added: Clarification for usage of StringUTF16 and not StringLatin1.
final class StringUTF16 {

    // BEGIN Android-changed: Pass String instead of byte[].
    /*
    public static int indexOfNonWhitespace(byte[] value) {
        int length = value.length >> 1;
     */
    public static int indexOfNonWhitespace(String value) {
        int length = value.length();
        int left = 0;
        while (left < length) {
            /*
            int codepoint = codePointAt(value, left, length);
             */
            int codepoint = value.codePointAt(left);
    // END Android-changed: Pass String instead of byte[].
            if (codepoint != ' ' && codepoint != '\t' && !Character.isWhitespace(codepoint)) {
                break;
            }
            left += Character.charCount(codepoint);
        }
        return left;
    }

    // BEGIN Android-changed: Pass String instead of byte[].
    /*
    public static int lastIndexOfNonWhitespace(byte[] value) {
        int length = value.length >> 1;
        int right = length;
     */
    public static int lastIndexOfNonWhitespace(String value) {
        int right = value.length();
        while (0 < right) {
            /*
            int codepoint = codePointBefore(value, right);
             */
            int codepoint = value.codePointBefore(right);
    // END Android-changed: Pass String instead of byte[].
            if (codepoint != ' ' && codepoint != '\t' && !Character.isWhitespace(codepoint)) {
                break;
            }
            right -= Character.charCount(codepoint);
        }
        return right;
    }

    // BEGIN Android-changed: Pass String instead of byte[].
    /*
    public static String strip(byte[] value) {
        int length = value.length >> 1;
     */
    public static String strip(String value) {
        int length = value.length();
        int left = indexOfNonWhitespace(value);
        if (left == length) {
            return "";
        }
        int right = lastIndexOfNonWhitespace(value);
        /*
        return ((left > 0) || (right < length)) ? newString(value, left, right - left) : null;
         */
        return ((left > 0) || (right < length)) ? value.substring(left, right) : null;
    // END Android-changed: Pass String instead of byte[].
    }

    // BEGIN Android-changed: Pass String instead of byte[].
    /*
    public static String stripLeading(byte[] value) {
        int length = value.length >> 1;
     */
    public static String stripLeading(String value) {
        int length = value.length();
        int left = indexOfNonWhitespace(value);
        if (left == length) {
            return "";
        }
        /*
        return (left != 0) ? newString(value, left, length - left) : null;
         */
        return (left != 0) ? value.substring(left, length) : null;
    // END Android-changed: Pass String instead of byte[].
    }

    // BEGIN Android-changed: Pass String instead of byte[].
    /*
    public static String stripTrailing(byte[] value) {
        int length = value.length >> 1;
     */
    public static String stripTrailing(String value) {
        int length = value.length();
        int right = lastIndexOfNonWhitespace(value);
        if (right == 0) {
            return "";
        }
        /*
        return (right != length) ? newString(value, 0, right) : null;
         */
        return (right != length) ? value.substring(0, right) : null;
    // END Android-changed: Pass String instead of byte[].
    }


}
