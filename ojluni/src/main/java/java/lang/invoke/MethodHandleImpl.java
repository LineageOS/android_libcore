/*
 * Copyright 2016 Google Inc.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Google designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Google in the LICENSE file that accompanied this code.
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
 */

package java.lang.invoke;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A method handle that's directly associated with an ArtField or an ArtMethod and
 * specifies no additional transformations.
 *
 * @hide
 */
public class MethodHandleImpl extends MethodHandle implements Cloneable {
    MethodHandleImpl(long artFieldOrMethod, int handleKind, MethodType type) {
        super(artFieldOrMethod, handleKind, type);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
