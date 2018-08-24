/*
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
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


package java.util;

import java.util.function.Function;

@SuppressWarnings({"unchecked", "deprecation", "all"})
public interface Map<K, V> {

public int size();

public boolean isEmpty();

public boolean containsKey(@libcore.util.Nullable java.lang.Object key);

public boolean containsValue(@libcore.util.Nullable java.lang.Object value);

@libcore.util.Nullable public V get(@libcore.util.Nullable java.lang.Object key);

@libcore.util.Nullable public V put(@libcore.util.NullFromTypeParam K key, @libcore.util.NullFromTypeParam V value);

@libcore.util.Nullable public V remove(@libcore.util.Nullable java.lang.Object key);

public void putAll(@libcore.util.NonNull java.util.Map<? extends K,? extends V> m);

public void clear();

@libcore.util.NonNull public java.util.Set<K> keySet();

@libcore.util.NonNull public java.util.Collection<V> values();

@libcore.util.NonNull public java.util.Set<java.util.Map.Entry<K,V>> entrySet();

public boolean equals(@libcore.util.Nullable java.lang.Object o);

public int hashCode();

@libcore.util.Nullable public default V getOrDefault(@libcore.util.Nullable java.lang.Object key, @libcore.util.Nullable V defaultValue) { throw new RuntimeException("Stub!"); }

public default void forEach(@libcore.util.NonNull java.util.function.BiConsumer<? super K,? super V> action) { throw new RuntimeException("Stub!"); }

public default void replaceAll(@libcore.util.NonNull java.util.function.BiFunction<? super K,? super V,? extends V> function) { throw new RuntimeException("Stub!"); }

@libcore.util.Nullable public default V putIfAbsent(@libcore.util.NullFromTypeParam K key, @libcore.util.NullFromTypeParam V value) { throw new RuntimeException("Stub!"); }

public default boolean remove(@libcore.util.Nullable java.lang.Object key, @libcore.util.Nullable java.lang.Object value) { throw new RuntimeException("Stub!"); }

public default boolean replace(@libcore.util.NullFromTypeParam K key, @libcore.util.Nullable V oldValue, @libcore.util.NullFromTypeParam V newValue) { throw new RuntimeException("Stub!"); }

@libcore.util.Nullable public default V replace(@libcore.util.NullFromTypeParam K key, @libcore.util.NullFromTypeParam V value) { throw new RuntimeException("Stub!"); }

@libcore.util.Nullable public default V computeIfAbsent(@libcore.util.NullFromTypeParam K key, @libcore.util.NonNull java.util.function.Function<? super K,? extends V> mappingFunction) { throw new RuntimeException("Stub!"); }

@libcore.util.Nullable public default V computeIfPresent(@libcore.util.NullFromTypeParam K key, @libcore.util.NonNull java.util.function.BiFunction<? super K,? super V,? extends V> remappingFunction) { throw new RuntimeException("Stub!"); }

@libcore.util.Nullable public default V compute(@libcore.util.NullFromTypeParam K key, @libcore.util.NonNull java.util.function.BiFunction<? super K,? super V,? extends V> remappingFunction) { throw new RuntimeException("Stub!"); }

@libcore.util.Nullable public default V merge(@libcore.util.NullFromTypeParam K key, @libcore.util.NonNull V value, @libcore.util.NonNull java.util.function.BiFunction<? super V,? super V,? extends V> remappingFunction) { throw new RuntimeException("Stub!"); }
@SuppressWarnings({"unchecked", "deprecation", "all"})
public static interface Entry<K, V> {

@libcore.util.NullFromTypeParam public K getKey();

@libcore.util.NullFromTypeParam public V getValue();

@libcore.util.NullFromTypeParam public V setValue(@libcore.util.NullFromTypeParam V value);

public boolean equals(@libcore.util.Nullable java.lang.Object o);

public int hashCode();

public static <K extends java.lang.Comparable<? super K>, V> java.util.Comparator<java.util.Map.Entry<K,V>> comparingByKey() { throw new RuntimeException("Stub!"); }

public static <K, V extends java.lang.Comparable<? super V>> java.util.Comparator<java.util.Map.Entry<K,V>> comparingByValue() { throw new RuntimeException("Stub!"); }

public static <K, V> java.util.Comparator<java.util.Map.Entry<K,V>> comparingByKey(@libcore.util.NonNull java.util.Comparator<? super K> cmp) { throw new RuntimeException("Stub!"); }

public static <K, V> java.util.Comparator<java.util.Map.Entry<K,V>> comparingByValue(@libcore.util.NonNull java.util.Comparator<? super V> cmp) { throw new RuntimeException("Stub!"); }
}

}

