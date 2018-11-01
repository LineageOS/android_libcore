/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import dalvik.annotation.compat.UnsupportedAppUsage;

@SuppressWarnings({"unchecked", "deprecation", "all"})
public class ConcurrentHashMap<K, V> extends java.util.AbstractMap<K, V>
        implements java.util.concurrent.ConcurrentMap<K, V>, java.io.Serializable {

    public ConcurrentHashMap() {
        throw new RuntimeException("Stub!");
    }

    public ConcurrentHashMap(int initialCapacity) {
        throw new RuntimeException("Stub!");
    }

    public ConcurrentHashMap(java.util.Map<? extends K, ? extends V> m) {
        throw new RuntimeException("Stub!");
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        throw new RuntimeException("Stub!");
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        throw new RuntimeException("Stub!");
    }

    static final int spread(int h) {
        throw new RuntimeException("Stub!");
    }

    private static final int tableSizeFor(int c) {
        throw new RuntimeException("Stub!");
    }

    static java.lang.Class<?> comparableClassFor(java.lang.Object x) {
        throw new RuntimeException("Stub!");
    }

    static int compareComparables(java.lang.Class<?> kc, java.lang.Object k, java.lang.Object x) {
        throw new RuntimeException("Stub!");
    }

    static final <K, V> java.util.concurrent.ConcurrentHashMap.Node<K, V> tabAt(
            java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab, int i) {
        throw new RuntimeException("Stub!");
    }

    static final <K, V> boolean casTabAt(
            java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
            int i,
            java.util.concurrent.ConcurrentHashMap.Node<K, V> c,
            java.util.concurrent.ConcurrentHashMap.Node<K, V> v) {
        throw new RuntimeException("Stub!");
    }

    static final <K, V> void setTabAt(
            java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
            int i,
            java.util.concurrent.ConcurrentHashMap.Node<K, V> v) {
        throw new RuntimeException("Stub!");
    }

    public int size() {
        throw new RuntimeException("Stub!");
    }

    public boolean isEmpty() {
        throw new RuntimeException("Stub!");
    }

    public V get(java.lang.Object key) {
        throw new RuntimeException("Stub!");
    }

    public boolean containsKey(java.lang.Object key) {
        throw new RuntimeException("Stub!");
    }

    public boolean containsValue(java.lang.Object value) {
        throw new RuntimeException("Stub!");
    }

    public V put(K key, V value) {
        throw new RuntimeException("Stub!");
    }

    final V putVal(K key, V value, boolean onlyIfAbsent) {
        throw new RuntimeException("Stub!");
    }

    public void putAll(java.util.Map<? extends K, ? extends V> m) {
        throw new RuntimeException("Stub!");
    }

    public V remove(java.lang.Object key) {
        throw new RuntimeException("Stub!");
    }

    final V replaceNode(java.lang.Object key, V value, java.lang.Object cv) {
        throw new RuntimeException("Stub!");
    }

    public void clear() {
        throw new RuntimeException("Stub!");
    }

    public java.util.Set<K> keySet() {
        throw new RuntimeException("Stub!");
    }

    public java.util.Collection<V> values() {
        throw new RuntimeException("Stub!");
    }

    public java.util.Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String toString() {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object o) {
        throw new RuntimeException("Stub!");
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.lang.ClassNotFoundException, java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public V putIfAbsent(K key, V value) {
        throw new RuntimeException("Stub!");
    }

    public boolean remove(java.lang.Object key, java.lang.Object value) {
        throw new RuntimeException("Stub!");
    }

    public boolean replace(K key, V oldValue, V newValue) {
        throw new RuntimeException("Stub!");
    }

    public V replace(K key, V value) {
        throw new RuntimeException("Stub!");
    }

    public V getOrDefault(java.lang.Object key, V defaultValue) {
        throw new RuntimeException("Stub!");
    }

    public void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
        throw new RuntimeException("Stub!");
    }

    public void replaceAll(
            java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        throw new RuntimeException("Stub!");
    }

    boolean removeEntryIf(
            java.util.function.Predicate<? super java.util.Map.Entry<K, V>> function) {
        throw new RuntimeException("Stub!");
    }

    boolean removeValueIf(java.util.function.Predicate<? super V> function) {
        throw new RuntimeException("Stub!");
    }

    public V computeIfAbsent(
            K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        throw new RuntimeException("Stub!");
    }

    public V computeIfPresent(
            K key,
            java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("Stub!");
    }

    public V compute(
            K key,
            java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("Stub!");
    }

    public V merge(
            K key,
            V value,
            java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("Stub!");
    }

    public boolean contains(java.lang.Object value) {
        throw new RuntimeException("Stub!");
    }

    public java.util.Enumeration<K> keys() {
        throw new RuntimeException("Stub!");
    }

    public java.util.Enumeration<V> elements() {
        throw new RuntimeException("Stub!");
    }

    public long mappingCount() {
        throw new RuntimeException("Stub!");
    }

    public static <K>
            java.util.concurrent.ConcurrentHashMap.KeySetView<K, java.lang.Boolean> newKeySet() {
        throw new RuntimeException("Stub!");
    }

    public static <K>
            java.util.concurrent.ConcurrentHashMap.KeySetView<K, java.lang.Boolean> newKeySet(
                    int initialCapacity) {
        throw new RuntimeException("Stub!");
    }

    public java.util.concurrent.ConcurrentHashMap.KeySetView<K, V> keySet(V mappedValue) {
        throw new RuntimeException("Stub!");
    }

    static final int resizeStamp(int n) {
        throw new RuntimeException("Stub!");
    }

    private final java.util.concurrent.ConcurrentHashMap.Node<K, V>[] initTable() {
        throw new RuntimeException("Stub!");
    }

    private final void addCount(long x, int check) {
        throw new RuntimeException("Stub!");
    }

    final java.util.concurrent.ConcurrentHashMap.Node<K, V>[] helpTransfer(
            java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
            java.util.concurrent.ConcurrentHashMap.Node<K, V> f) {
        throw new RuntimeException("Stub!");
    }

    private final void tryPresize(int size) {
        throw new RuntimeException("Stub!");
    }

    private final void transfer(
            java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
            java.util.concurrent.ConcurrentHashMap.Node<K, V>[] nextTab) {
        throw new RuntimeException("Stub!");
    }

    final long sumCount() {
        throw new RuntimeException("Stub!");
    }

    private final void fullAddCount(long x, boolean wasUncontended) {
        throw new RuntimeException("Stub!");
    }

    private final void treeifyBin(
            java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab, int index) {
        throw new RuntimeException("Stub!");
    }

    static <K, V> java.util.concurrent.ConcurrentHashMap.Node<K, V> untreeify(
            java.util.concurrent.ConcurrentHashMap.Node<K, V> b) {
        throw new RuntimeException("Stub!");
    }

    final int batchFor(long b) {
        throw new RuntimeException("Stub!");
    }

    public void forEach(
            long parallelismThreshold, java.util.function.BiConsumer<? super K, ? super V> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> void forEach(
            long parallelismThreshold,
            java.util.function.BiFunction<? super K, ? super V, ? extends U> transformer,
            java.util.function.Consumer<? super U> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> U search(
            long parallelismThreshold,
            java.util.function.BiFunction<? super K, ? super V, ? extends U> searchFunction) {
        throw new RuntimeException("Stub!");
    }

    public <U> U reduce(
            long parallelismThreshold,
            java.util.function.BiFunction<? super K, ? super V, ? extends U> transformer,
            java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
        throw new RuntimeException("Stub!");
    }

    public double reduceToDouble(
            long parallelismThreshold,
            java.util.function.ToDoubleBiFunction<? super K, ? super V> transformer,
            double basis,
            java.util.function.DoubleBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public long reduceToLong(
            long parallelismThreshold,
            java.util.function.ToLongBiFunction<? super K, ? super V> transformer,
            long basis,
            java.util.function.LongBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public int reduceToInt(
            long parallelismThreshold,
            java.util.function.ToIntBiFunction<? super K, ? super V> transformer,
            int basis,
            java.util.function.IntBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public void forEachKey(
            long parallelismThreshold, java.util.function.Consumer<? super K> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> void forEachKey(
            long parallelismThreshold,
            java.util.function.Function<? super K, ? extends U> transformer,
            java.util.function.Consumer<? super U> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> U searchKeys(
            long parallelismThreshold,
            java.util.function.Function<? super K, ? extends U> searchFunction) {
        throw new RuntimeException("Stub!");
    }

    public K reduceKeys(
            long parallelismThreshold,
            java.util.function.BiFunction<? super K, ? super K, ? extends K> reducer) {
        throw new RuntimeException("Stub!");
    }

    public <U> U reduceKeys(
            long parallelismThreshold,
            java.util.function.Function<? super K, ? extends U> transformer,
            java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
        throw new RuntimeException("Stub!");
    }

    public double reduceKeysToDouble(
            long parallelismThreshold,
            java.util.function.ToDoubleFunction<? super K> transformer,
            double basis,
            java.util.function.DoubleBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public long reduceKeysToLong(
            long parallelismThreshold,
            java.util.function.ToLongFunction<? super K> transformer,
            long basis,
            java.util.function.LongBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public int reduceKeysToInt(
            long parallelismThreshold,
            java.util.function.ToIntFunction<? super K> transformer,
            int basis,
            java.util.function.IntBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public void forEachValue(
            long parallelismThreshold, java.util.function.Consumer<? super V> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> void forEachValue(
            long parallelismThreshold,
            java.util.function.Function<? super V, ? extends U> transformer,
            java.util.function.Consumer<? super U> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> U searchValues(
            long parallelismThreshold,
            java.util.function.Function<? super V, ? extends U> searchFunction) {
        throw new RuntimeException("Stub!");
    }

    public V reduceValues(
            long parallelismThreshold,
            java.util.function.BiFunction<? super V, ? super V, ? extends V> reducer) {
        throw new RuntimeException("Stub!");
    }

    public <U> U reduceValues(
            long parallelismThreshold,
            java.util.function.Function<? super V, ? extends U> transformer,
            java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
        throw new RuntimeException("Stub!");
    }

    public double reduceValuesToDouble(
            long parallelismThreshold,
            java.util.function.ToDoubleFunction<? super V> transformer,
            double basis,
            java.util.function.DoubleBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public long reduceValuesToLong(
            long parallelismThreshold,
            java.util.function.ToLongFunction<? super V> transformer,
            long basis,
            java.util.function.LongBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public int reduceValuesToInt(
            long parallelismThreshold,
            java.util.function.ToIntFunction<? super V> transformer,
            int basis,
            java.util.function.IntBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public void forEachEntry(
            long parallelismThreshold,
            java.util.function.Consumer<? super java.util.Map.Entry<K, V>> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> void forEachEntry(
            long parallelismThreshold,
            java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> transformer,
            java.util.function.Consumer<? super U> action) {
        throw new RuntimeException("Stub!");
    }

    public <U> U searchEntries(
            long parallelismThreshold,
            java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> searchFunction) {
        throw new RuntimeException("Stub!");
    }

    public java.util.Map.Entry<K, V> reduceEntries(
            long parallelismThreshold,
            java.util.function.BiFunction<
                            java.util.Map.Entry<K, V>,
                            java.util.Map.Entry<K, V>,
                            ? extends java.util.Map.Entry<K, V>>
                    reducer) {
        throw new RuntimeException("Stub!");
    }

    public <U> U reduceEntries(
            long parallelismThreshold,
            java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> transformer,
            java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
        throw new RuntimeException("Stub!");
    }

    public double reduceEntriesToDouble(
            long parallelismThreshold,
            java.util.function.ToDoubleFunction<java.util.Map.Entry<K, V>> transformer,
            double basis,
            java.util.function.DoubleBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public long reduceEntriesToLong(
            long parallelismThreshold,
            java.util.function.ToLongFunction<java.util.Map.Entry<K, V>> transformer,
            long basis,
            java.util.function.LongBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    public int reduceEntriesToInt(
            long parallelismThreshold,
            java.util.function.ToIntFunction<java.util.Map.Entry<K, V>> transformer,
            int basis,
            java.util.function.IntBinaryOperator reducer) {
        throw new RuntimeException("Stub!");
    }

    private static final int ABASE;

    static {
        ABASE = 0;
    }

    private static final int ASHIFT;

    static {
        ASHIFT = 0;
    }

    private static final long BASECOUNT;

    static {
        BASECOUNT = 0;
    }

    private static final long CELLSBUSY;

    static {
        CELLSBUSY = 0;
    }

    private static final long CELLVALUE;

    static {
        CELLVALUE = 0;
    }

    private static final int DEFAULT_CAPACITY = 16; // 0x10

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16; // 0x10

    static final int HASH_BITS = 2147483647; // 0x7fffffff

    private static final float LOAD_FACTOR = 0.75f;

    private static final int MAXIMUM_CAPACITY = 1073741824; // 0x40000000

    static final int MAX_ARRAY_SIZE = 2147483639; // 0x7ffffff7

    private static final int MAX_RESIZERS = 65535; // 0xffff

    private static final int MIN_TRANSFER_STRIDE = 16; // 0x10

    static final int MIN_TREEIFY_CAPACITY = 64; // 0x40

    static final int MOVED = -1; // 0xffffffff

    static final int NCPU;

    static {
        NCPU = 0;
    }

    static final int RESERVED = -3; // 0xfffffffd

    private static final int RESIZE_STAMP_BITS = 16; // 0x10

    private static final int RESIZE_STAMP_SHIFT = 16; // 0x10

    private static final long SIZECTL;

    static {
        SIZECTL = 0;
    }

    private static final long TRANSFERINDEX;

    static {
        TRANSFERINDEX = 0;
    }

    static final int TREEBIN = -2; // 0xfffffffe

    static final int TREEIFY_THRESHOLD = 8; // 0x8

    private static final sun.misc.Unsafe U;

    static {
        U = null;
    }

    static final int UNTREEIFY_THRESHOLD = 6; // 0x6

    private transient volatile long baseCount;

    private transient volatile int cellsBusy;

    private transient volatile java.util.concurrent.ConcurrentHashMap.CounterCell[] counterCells;

    private transient java.util.concurrent.ConcurrentHashMap.EntrySetView<K, V> entrySet;

    private transient java.util.concurrent.ConcurrentHashMap.KeySetView<K, V> keySet;

    private transient volatile java.util.concurrent.ConcurrentHashMap.Node<K, V>[] nextTable;

    private static final java.io.ObjectStreamField[] serialPersistentFields;

    static {
        serialPersistentFields = new java.io.ObjectStreamField[0];
    }

    private static final long serialVersionUID = 7249069246763182397L; // 0x6499de129d87293dL

    private transient volatile int sizeCtl;

    transient volatile java.util.concurrent.ConcurrentHashMap.Node<K, V>[] table;

    private transient volatile int transferIndex;

    private transient java.util.concurrent.ConcurrentHashMap.ValuesView<K, V> values;

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static class BaseIterator<K, V> extends java.util.concurrent.ConcurrentHashMap.Traverser<K, V> {

        BaseIterator(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int size,
                int index,
                int limit,
                java.util.concurrent.ConcurrentHashMap<K, V> map) {
            super(null, 0, 0, 0);
            throw new RuntimeException("Stub!");
        }

        public final boolean hasNext() {
            throw new RuntimeException("Stub!");
        }

        @UnsupportedAppUsage
        public final boolean hasMoreElements() {
            throw new RuntimeException("Stub!");
        }

        public final void remove() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.Node<K, V> lastReturned;

        final java.util.concurrent.ConcurrentHashMap<K, V> map;

        {
            map = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    abstract static class BulkTask<K, V, R> extends java.util.concurrent.CountedCompleter<R> {

        BulkTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> par,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t) {
            throw new RuntimeException("Stub!");
        }

        final java.util.concurrent.ConcurrentHashMap.Node<K, V> advance() {
            throw new RuntimeException("Stub!");
        }

        private void pushState(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t, int i, int n) {
            throw new RuntimeException("Stub!");
        }

        private void recoverState(int n) {
            throw new RuntimeException("Stub!");
        }

        int baseIndex;

        int baseLimit;

        final int baseSize;

        {
            baseSize = 0;
        }

        int batch;

        int index;

        java.util.concurrent.ConcurrentHashMap.Node<K, V> next;

        java.util.concurrent.ConcurrentHashMap.TableStack<K, V> spare;

        java.util.concurrent.ConcurrentHashMap.TableStack<K, V> stack;

        java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    abstract static class CollectionView<K, V, E>
            implements java.util.Collection<E>, java.io.Serializable {

        CollectionView(java.util.concurrent.ConcurrentHashMap<K, V> map) {
            throw new RuntimeException("Stub!");
        }

        public java.util.concurrent.ConcurrentHashMap<K, V> getMap() {
            throw new RuntimeException("Stub!");
        }

        public final void clear() {
            throw new RuntimeException("Stub!");
        }

        public final int size() {
            throw new RuntimeException("Stub!");
        }

        public final boolean isEmpty() {
            throw new RuntimeException("Stub!");
        }

        public abstract java.util.Iterator<E> iterator();

        public abstract boolean contains(java.lang.Object o);

        public abstract boolean remove(java.lang.Object o);

        public final java.lang.Object[] toArray() {
            throw new RuntimeException("Stub!");
        }

        public final <T> T[] toArray(T[] a) {
            throw new RuntimeException("Stub!");
        }

        public final java.lang.String toString() {
            throw new RuntimeException("Stub!");
        }

        public final boolean containsAll(java.util.Collection<?> c) {
            throw new RuntimeException("Stub!");
        }

        public final boolean removeAll(java.util.Collection<?> c) {
            throw new RuntimeException("Stub!");
        }

        public final boolean retainAll(java.util.Collection<?> c) {
            throw new RuntimeException("Stub!");
        }

        private static final java.lang.String OOME_MSG = "Required array size too large";

        final java.util.concurrent.ConcurrentHashMap<K, V> map;

        {
            map = null;
        }

        private static final long serialVersionUID = 7249069246763182397L; // 0x6499de129d87293dL
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class CounterCell {

        CounterCell(long x) {
            throw new RuntimeException("Stub!");
        }

        volatile long value;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class EntryIterator<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BaseIterator<K, V>
            implements java.util.Iterator<java.util.Map.Entry<K, V>> {

        EntryIterator(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int index,
                int size,
                int limit,
                java.util.concurrent.ConcurrentHashMap<K, V> map) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.util.Map.Entry<K, V> next() {
            throw new RuntimeException("Stub!");
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class EntrySetView<K, V>
            extends java.util.concurrent.ConcurrentHashMap.CollectionView<
                    K, V, java.util.Map.Entry<K, V>>
            implements java.util.Set<java.util.Map.Entry<K, V>>, java.io.Serializable {

        EntrySetView(java.util.concurrent.ConcurrentHashMap<K, V> map) {
            super(null);
            throw new RuntimeException("Stub!");
        }

        public boolean contains(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public boolean remove(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public java.util.Iterator<java.util.Map.Entry<K, V>> iterator() {
            throw new RuntimeException("Stub!");
        }

        public boolean add(java.util.Map.Entry<K, V> e) {
            throw new RuntimeException("Stub!");
        }

        public boolean addAll(java.util.Collection<? extends java.util.Map.Entry<K, V>> c) {
            throw new RuntimeException("Stub!");
        }

        public boolean removeIf(
                java.util.function.Predicate<? super java.util.Map.Entry<K, V>> filter) {
            throw new RuntimeException("Stub!");
        }

        public int hashCode() {
            throw new RuntimeException("Stub!");
        }

        public boolean equals(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public java.util.Spliterator<java.util.Map.Entry<K, V>> spliterator() {
            throw new RuntimeException("Stub!");
        }

        public void forEach(java.util.function.Consumer<? super java.util.Map.Entry<K, V>> action) {
            throw new RuntimeException("Stub!");
        }

        private static final long serialVersionUID = 2249069246763182397L; // 0x1f364c905893293dL
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class EntrySpliterator<K, V>
            extends java.util.concurrent.ConcurrentHashMap.Traverser<K, V>
            implements java.util.Spliterator<java.util.Map.Entry<K, V>> {

        EntrySpliterator(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int size,
                int index,
                int limit,
                long est,
                java.util.concurrent.ConcurrentHashMap<K, V> map) {
            super(null, 0, 0, 0);
            throw new RuntimeException("Stub!");
        }

        public java.util.concurrent.ConcurrentHashMap.EntrySpliterator<K, V> trySplit() {
            throw new RuntimeException("Stub!");
        }

        public void forEachRemaining(
                java.util.function.Consumer<? super java.util.Map.Entry<K, V>> action) {
            throw new RuntimeException("Stub!");
        }

        public boolean tryAdvance(
                java.util.function.Consumer<? super java.util.Map.Entry<K, V>> action) {
            throw new RuntimeException("Stub!");
        }

        public long estimateSize() {
            throw new RuntimeException("Stub!");
        }

        public int characteristics() {
            throw new RuntimeException("Stub!");
        }

        long est;

        final java.util.concurrent.ConcurrentHashMap<K, V> map;

        {
            map = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachEntryTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachEntryTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Consumer<? super java.util.Map.Entry<K, V>> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.Consumer<? super java.util.Map.Entry<K, V>> action;

        {
            action = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachKeyTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachKeyTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Consumer<? super K> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.Consumer<? super K> action;

        {
            action = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachMappingTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachMappingTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.BiConsumer<? super K, ? super V> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.BiConsumer<? super K, ? super V> action;

        {
            action = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachTransformedEntryTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachTransformedEntryTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> transformer,
                java.util.function.Consumer<? super U> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.Consumer<? super U> action;

        {
            action = null;
        }

        final java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachTransformedKeyTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachTransformedKeyTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Function<? super K, ? extends U> transformer,
                java.util.function.Consumer<? super U> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.Consumer<? super U> action;

        {
            action = null;
        }

        final java.util.function.Function<? super K, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachTransformedMappingTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachTransformedMappingTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.BiFunction<? super K, ? super V, ? extends U> transformer,
                java.util.function.Consumer<? super U> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.Consumer<? super U> action;

        {
            action = null;
        }

        final java.util.function.BiFunction<? super K, ? super V, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachTransformedValueTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachTransformedValueTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Function<? super V, ? extends U> transformer,
                java.util.function.Consumer<? super U> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.Consumer<? super U> action;

        {
            action = null;
        }

        final java.util.function.Function<? super V, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForEachValueTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Void> {

        ForEachValueTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Consumer<? super V> action) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.function.Consumer<? super V> action;

        {
            action = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ForwardingNode<K, V>
            extends java.util.concurrent.ConcurrentHashMap.Node<K, V> {

        ForwardingNode(java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab) {
            super(0, null, null, null);
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.Node<K, V> find(int h, java.lang.Object k) {
            throw new RuntimeException("Stub!");
        }

        final java.util.concurrent.ConcurrentHashMap.Node<K, V>[] nextTable;

        {
            nextTable = new java.util.concurrent.ConcurrentHashMap.Node[0];
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class KeyIterator<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BaseIterator<K, V>
            implements java.util.Iterator<K>, java.util.Enumeration<K> {

        KeyIterator(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int index,
                int size,
                int limit,
                java.util.concurrent.ConcurrentHashMap<K, V> map) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public K next() {
            throw new RuntimeException("Stub!");
        }

        public K nextElement() {
            throw new RuntimeException("Stub!");
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    public static class KeySetView<K, V>
            extends java.util.concurrent.ConcurrentHashMap.CollectionView<K, V, K>
            implements java.util.Set<K>, java.io.Serializable {

        KeySetView(java.util.concurrent.ConcurrentHashMap<K, V> map, V value) {
            super(null);
            throw new RuntimeException("Stub!");
        }

        public V getMappedValue() {
            throw new RuntimeException("Stub!");
        }

        public boolean contains(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public boolean remove(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public java.util.Iterator<K> iterator() {
            throw new RuntimeException("Stub!");
        }

        public boolean add(K e) {
            throw new RuntimeException("Stub!");
        }

        public boolean addAll(java.util.Collection<? extends K> c) {
            throw new RuntimeException("Stub!");
        }

        public int hashCode() {
            throw new RuntimeException("Stub!");
        }

        public boolean equals(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public java.util.Spliterator<K> spliterator() {
            throw new RuntimeException("Stub!");
        }

        public void forEach(java.util.function.Consumer<? super K> action) {
            throw new RuntimeException("Stub!");
        }

        private static final long serialVersionUID = 7249069246763182397L; // 0x6499de129d87293dL

        private final V value;

        {
            value = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class KeySpliterator<K, V>
            extends java.util.concurrent.ConcurrentHashMap.Traverser<K, V>
            implements java.util.Spliterator<K> {

        KeySpliterator(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int size,
                int index,
                int limit,
                long est) {
            super(null, 0, 0, 0);
            throw new RuntimeException("Stub!");
        }

        public java.util.concurrent.ConcurrentHashMap.KeySpliterator<K, V> trySplit() {
            throw new RuntimeException("Stub!");
        }

        public void forEachRemaining(java.util.function.Consumer<? super K> action) {
            throw new RuntimeException("Stub!");
        }

        public boolean tryAdvance(java.util.function.Consumer<? super K> action) {
            throw new RuntimeException("Stub!");
        }

        public long estimateSize() {
            throw new RuntimeException("Stub!");
        }

        public int characteristics() {
            throw new RuntimeException("Stub!");
        }

        long est;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapEntry<K, V> implements java.util.Map.Entry<K, V> {

        MapEntry(K key, V val, java.util.concurrent.ConcurrentHashMap<K, V> map) {
            throw new RuntimeException("Stub!");
        }

        public K getKey() {
            throw new RuntimeException("Stub!");
        }

        public V getValue() {
            throw new RuntimeException("Stub!");
        }

        public int hashCode() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String toString() {
            throw new RuntimeException("Stub!");
        }

        public boolean equals(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public V setValue(V value) {
            throw new RuntimeException("Stub!");
        }

        final K key;

        {
            key = null;
        }

        final java.util.concurrent.ConcurrentHashMap<K, V> map;

        {
            map = null;
        }

        V val;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceEntriesTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        MapReduceEntriesTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceEntriesTask<K, V, U> nextRight,
                java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> transformer,
                java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesTask<K, V, U> nextRight;

        final java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer;

        {
            reducer = null;
        }

        U result;

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesTask<K, V, U> rights;

        final java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceEntriesToDoubleTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Double> {

        MapReduceEntriesToDoubleTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToDoubleTask<K, V> nextRight,
                java.util.function.ToDoubleFunction<java.util.Map.Entry<K, V>> transformer,
                double basis,
                java.util.function.DoubleBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Double getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final double basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToDoubleTask<K, V> nextRight;

        final java.util.function.DoubleBinaryOperator reducer;

        {
            reducer = null;
        }

        double result;

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToDoubleTask<K, V> rights;

        final java.util.function.ToDoubleFunction<java.util.Map.Entry<K, V>> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceEntriesToIntTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Integer> {

        MapReduceEntriesToIntTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToIntTask<K, V> nextRight,
                java.util.function.ToIntFunction<java.util.Map.Entry<K, V>> transformer,
                int basis,
                java.util.function.IntBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Integer getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final int basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToIntTask<K, V> nextRight;

        final java.util.function.IntBinaryOperator reducer;

        {
            reducer = null;
        }

        int result;

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToIntTask<K, V> rights;

        final java.util.function.ToIntFunction<java.util.Map.Entry<K, V>> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceEntriesToLongTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Long> {

        MapReduceEntriesToLongTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToLongTask<K, V> nextRight,
                java.util.function.ToLongFunction<java.util.Map.Entry<K, V>> transformer,
                long basis,
                java.util.function.LongBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Long getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final long basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToLongTask<K, V> nextRight;

        final java.util.function.LongBinaryOperator reducer;

        {
            reducer = null;
        }

        long result;

        java.util.concurrent.ConcurrentHashMap.MapReduceEntriesToLongTask<K, V> rights;

        final java.util.function.ToLongFunction<java.util.Map.Entry<K, V>> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceKeysTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        MapReduceKeysTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceKeysTask<K, V, U> nextRight,
                java.util.function.Function<? super K, ? extends U> transformer,
                java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysTask<K, V, U> nextRight;

        final java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer;

        {
            reducer = null;
        }

        U result;

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysTask<K, V, U> rights;

        final java.util.function.Function<? super K, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceKeysToDoubleTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Double> {

        MapReduceKeysToDoubleTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceKeysToDoubleTask<K, V> nextRight,
                java.util.function.ToDoubleFunction<? super K> transformer,
                double basis,
                java.util.function.DoubleBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Double getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final double basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysToDoubleTask<K, V> nextRight;

        final java.util.function.DoubleBinaryOperator reducer;

        {
            reducer = null;
        }

        double result;

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysToDoubleTask<K, V> rights;

        final java.util.function.ToDoubleFunction<? super K> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceKeysToIntTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Integer> {

        MapReduceKeysToIntTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceKeysToIntTask<K, V> nextRight,
                java.util.function.ToIntFunction<? super K> transformer,
                int basis,
                java.util.function.IntBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Integer getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final int basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysToIntTask<K, V> nextRight;

        final java.util.function.IntBinaryOperator reducer;

        {
            reducer = null;
        }

        int result;

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysToIntTask<K, V> rights;

        final java.util.function.ToIntFunction<? super K> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceKeysToLongTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Long> {

        MapReduceKeysToLongTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceKeysToLongTask<K, V> nextRight,
                java.util.function.ToLongFunction<? super K> transformer,
                long basis,
                java.util.function.LongBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Long getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final long basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysToLongTask<K, V> nextRight;

        final java.util.function.LongBinaryOperator reducer;

        {
            reducer = null;
        }

        long result;

        java.util.concurrent.ConcurrentHashMap.MapReduceKeysToLongTask<K, V> rights;

        final java.util.function.ToLongFunction<? super K> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceMappingsTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        MapReduceMappingsTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceMappingsTask<K, V, U> nextRight,
                java.util.function.BiFunction<? super K, ? super V, ? extends U> transformer,
                java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsTask<K, V, U> nextRight;

        final java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer;

        {
            reducer = null;
        }

        U result;

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsTask<K, V, U> rights;

        final java.util.function.BiFunction<? super K, ? super V, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceMappingsToDoubleTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Double> {

        MapReduceMappingsToDoubleTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToDoubleTask<K, V>
                        nextRight,
                java.util.function.ToDoubleBiFunction<? super K, ? super V> transformer,
                double basis,
                java.util.function.DoubleBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Double getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final double basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToDoubleTask<K, V> nextRight;

        final java.util.function.DoubleBinaryOperator reducer;

        {
            reducer = null;
        }

        double result;

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToDoubleTask<K, V> rights;

        final java.util.function.ToDoubleBiFunction<? super K, ? super V> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceMappingsToIntTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Integer> {

        MapReduceMappingsToIntTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToIntTask<K, V> nextRight,
                java.util.function.ToIntBiFunction<? super K, ? super V> transformer,
                int basis,
                java.util.function.IntBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Integer getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final int basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToIntTask<K, V> nextRight;

        final java.util.function.IntBinaryOperator reducer;

        {
            reducer = null;
        }

        int result;

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToIntTask<K, V> rights;

        final java.util.function.ToIntBiFunction<? super K, ? super V> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceMappingsToLongTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Long> {

        MapReduceMappingsToLongTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToLongTask<K, V> nextRight,
                java.util.function.ToLongBiFunction<? super K, ? super V> transformer,
                long basis,
                java.util.function.LongBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Long getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final long basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToLongTask<K, V> nextRight;

        final java.util.function.LongBinaryOperator reducer;

        {
            reducer = null;
        }

        long result;

        java.util.concurrent.ConcurrentHashMap.MapReduceMappingsToLongTask<K, V> rights;

        final java.util.function.ToLongBiFunction<? super K, ? super V> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceValuesTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        MapReduceValuesTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceValuesTask<K, V, U> nextRight,
                java.util.function.Function<? super V, ? extends U> transformer,
                java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesTask<K, V, U> nextRight;

        final java.util.function.BiFunction<? super U, ? super U, ? extends U> reducer;

        {
            reducer = null;
        }

        U result;

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesTask<K, V, U> rights;

        final java.util.function.Function<? super V, ? extends U> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceValuesToDoubleTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Double> {

        MapReduceValuesToDoubleTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceValuesToDoubleTask<K, V> nextRight,
                java.util.function.ToDoubleFunction<? super V> transformer,
                double basis,
                java.util.function.DoubleBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Double getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final double basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesToDoubleTask<K, V> nextRight;

        final java.util.function.DoubleBinaryOperator reducer;

        {
            reducer = null;
        }

        double result;

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesToDoubleTask<K, V> rights;

        final java.util.function.ToDoubleFunction<? super V> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceValuesToIntTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Integer> {

        MapReduceValuesToIntTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceValuesToIntTask<K, V> nextRight,
                java.util.function.ToIntFunction<? super V> transformer,
                int basis,
                java.util.function.IntBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Integer getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final int basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesToIntTask<K, V> nextRight;

        final java.util.function.IntBinaryOperator reducer;

        {
            reducer = null;
        }

        int result;

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesToIntTask<K, V> rights;

        final java.util.function.ToIntFunction<? super V> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class MapReduceValuesToLongTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, java.lang.Long> {

        MapReduceValuesToLongTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.MapReduceValuesToLongTask<K, V> nextRight,
                java.util.function.ToLongFunction<? super V> transformer,
                long basis,
                java.util.function.LongBinaryOperator reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.lang.Long getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final long basis;

        {
            basis = 0;
        }

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesToLongTask<K, V> nextRight;

        final java.util.function.LongBinaryOperator reducer;

        {
            reducer = null;
        }

        long result;

        java.util.concurrent.ConcurrentHashMap.MapReduceValuesToLongTask<K, V> rights;

        final java.util.function.ToLongFunction<? super V> transformer;

        {
            transformer = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static class Node<K, V> implements java.util.Map.Entry<K, V> {

        Node(int hash, K key, V val, java.util.concurrent.ConcurrentHashMap.Node<K, V> next) {
            throw new RuntimeException("Stub!");
        }

        public final K getKey() {
            throw new RuntimeException("Stub!");
        }

        public final V getValue() {
            throw new RuntimeException("Stub!");
        }

        public final int hashCode() {
            throw new RuntimeException("Stub!");
        }

        public final java.lang.String toString() {
            throw new RuntimeException("Stub!");
        }

        public final V setValue(V value) {
            throw new RuntimeException("Stub!");
        }

        public final boolean equals(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.Node<K, V> find(int h, java.lang.Object k) {
            throw new RuntimeException("Stub!");
        }

        final int hash;

        {
            hash = 0;
        }

        final K key;

        {
            key = null;
        }

        volatile java.util.concurrent.ConcurrentHashMap.Node<K, V> next;

        volatile V val;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ReduceEntriesTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<
                    K, V, java.util.Map.Entry<K, V>> {

        ReduceEntriesTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.ReduceEntriesTask<K, V> nextRight,
                java.util.function.BiFunction<
                                java.util.Map.Entry<K, V>,
                                java.util.Map.Entry<K, V>,
                                ? extends java.util.Map.Entry<K, V>>
                        reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public java.util.Map.Entry<K, V> getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.ReduceEntriesTask<K, V> nextRight;

        final java.util.function.BiFunction<
                        java.util.Map.Entry<K, V>,
                        java.util.Map.Entry<K, V>,
                        ? extends java.util.Map.Entry<K, V>>
                reducer;

        {
            reducer = null;
        }

        java.util.Map.Entry<K, V> result;

        java.util.concurrent.ConcurrentHashMap.ReduceEntriesTask<K, V> rights;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ReduceKeysTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, K> {

        ReduceKeysTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.ReduceKeysTask<K, V> nextRight,
                java.util.function.BiFunction<? super K, ? super K, ? extends K> reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public K getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.ReduceKeysTask<K, V> nextRight;

        final java.util.function.BiFunction<? super K, ? super K, ? extends K> reducer;

        {
            reducer = null;
        }

        K result;

        java.util.concurrent.ConcurrentHashMap.ReduceKeysTask<K, V> rights;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ReduceValuesTask<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, V> {

        ReduceValuesTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.concurrent.ConcurrentHashMap.ReduceValuesTask<K, V> nextRight,
                java.util.function.BiFunction<? super V, ? super V, ? extends V> reducer) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public V getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.ReduceValuesTask<K, V> nextRight;

        final java.util.function.BiFunction<? super V, ? super V, ? extends V> reducer;

        {
            reducer = null;
        }

        V result;

        java.util.concurrent.ConcurrentHashMap.ReduceValuesTask<K, V> rights;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ReservationNode<K, V>
            extends java.util.concurrent.ConcurrentHashMap.Node<K, V> {

        ReservationNode() {
            super(0, null, null, null);
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.Node<K, V> find(int h, java.lang.Object k) {
            throw new RuntimeException("Stub!");
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class SearchEntriesTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        SearchEntriesTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> searchFunction,
                java.util.concurrent.atomic.AtomicReference<U> result) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.concurrent.atomic.AtomicReference<U> result;

        {
            result = null;
        }

        final java.util.function.Function<java.util.Map.Entry<K, V>, ? extends U> searchFunction;

        {
            searchFunction = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class SearchKeysTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        SearchKeysTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Function<? super K, ? extends U> searchFunction,
                java.util.concurrent.atomic.AtomicReference<U> result) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.concurrent.atomic.AtomicReference<U> result;

        {
            result = null;
        }

        final java.util.function.Function<? super K, ? extends U> searchFunction;

        {
            searchFunction = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class SearchMappingsTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        SearchMappingsTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.BiFunction<? super K, ? super V, ? extends U> searchFunction,
                java.util.concurrent.atomic.AtomicReference<U> result) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.concurrent.atomic.AtomicReference<U> result;

        {
            result = null;
        }

        final java.util.function.BiFunction<? super K, ? super V, ? extends U> searchFunction;

        {
            searchFunction = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class SearchValuesTask<K, V, U>
            extends java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, U> {

        SearchValuesTask(
                java.util.concurrent.ConcurrentHashMap.BulkTask<K, V, ?> p,
                int b,
                int i,
                int f,
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t,
                java.util.function.Function<? super V, ? extends U> searchFunction,
                java.util.concurrent.atomic.AtomicReference<U> result) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public U getRawResult() {
            throw new RuntimeException("Stub!");
        }

        public void compute() {
            throw new RuntimeException("Stub!");
        }

        final java.util.concurrent.atomic.AtomicReference<U> result;

        {
            result = null;
        }

        final java.util.function.Function<? super V, ? extends U> searchFunction;

        {
            searchFunction = null;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static class Segment<K, V> extends java.util.concurrent.locks.ReentrantLock
            implements java.io.Serializable {

        Segment(float lf) {
            throw new RuntimeException("Stub!");
        }

        final float loadFactor;

        {
            loadFactor = 0;
        }

        private static final long serialVersionUID = 2249069246763182397L; // 0x1f364c905893293dL
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class TableStack<K, V> {

        TableStack() {
            throw new RuntimeException("Stub!");
        }

        int index;

        int length;

        java.util.concurrent.ConcurrentHashMap.TableStack<K, V> next;

        java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static class Traverser<K, V> {

        Traverser(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int size,
                int index,
                int limit) {
            throw new RuntimeException("Stub!");
        }

        final java.util.concurrent.ConcurrentHashMap.Node<K, V> advance() {
            throw new RuntimeException("Stub!");
        }

        private void pushState(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] t, int i, int n) {
            throw new RuntimeException("Stub!");
        }

        private void recoverState(int n) {
            throw new RuntimeException("Stub!");
        }

        int baseIndex;

        int baseLimit;

        final int baseSize;

        {
            baseSize = 0;
        }

        int index;

        java.util.concurrent.ConcurrentHashMap.Node<K, V> next;

        java.util.concurrent.ConcurrentHashMap.TableStack<K, V> spare;

        java.util.concurrent.ConcurrentHashMap.TableStack<K, V> stack;

        java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class TreeBin<K, V> extends java.util.concurrent.ConcurrentHashMap.Node<K, V> {

        TreeBin(java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> b) {
            super(0, null, null, null);
            throw new RuntimeException("Stub!");
        }

        static int tieBreakOrder(java.lang.Object a, java.lang.Object b) {
            throw new RuntimeException("Stub!");
        }

        private void lockRoot() {
            throw new RuntimeException("Stub!");
        }

        private void unlockRoot() {
            throw new RuntimeException("Stub!");
        }

        private void contendedLock() {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.Node<K, V> find(int h, java.lang.Object k) {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> putTreeVal(int h, K k, V v) {
            throw new RuntimeException("Stub!");
        }

        boolean removeTreeNode(java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> p) {
            throw new RuntimeException("Stub!");
        }

        static <K, V> java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> rotateLeft(
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> root,
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> p) {
            throw new RuntimeException("Stub!");
        }

        static <K, V> java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> rotateRight(
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> root,
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> p) {
            throw new RuntimeException("Stub!");
        }

        static <K, V> java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> balanceInsertion(
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> root,
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> x) {
            throw new RuntimeException("Stub!");
        }

        static <K, V> java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> balanceDeletion(
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> root,
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> x) {
            throw new RuntimeException("Stub!");
        }

        static <K, V> boolean checkInvariants(
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> t) {
            throw new RuntimeException("Stub!");
        }

        private static final long LOCKSTATE;

        static {
            LOCKSTATE = 0;
        }

        static final int READER = 4; // 0x4

        private static final sun.misc.Unsafe U;

        static {
            U = null;
        }

        static final int WAITER = 2; // 0x2

        static final int WRITER = 1; // 0x1

        volatile java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> first;

        volatile int lockState;

        java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> root;

        volatile java.lang.Thread waiter;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class TreeNode<K, V> extends java.util.concurrent.ConcurrentHashMap.Node<K, V> {

        TreeNode(
                int hash,
                K key,
                V val,
                java.util.concurrent.ConcurrentHashMap.Node<K, V> next,
                java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> parent) {
            super(0, null, null, null);
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.Node<K, V> find(int h, java.lang.Object k) {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> findTreeNode(
                int h, java.lang.Object k, java.lang.Class<?> kc) {
            throw new RuntimeException("Stub!");
        }

        java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> left;

        java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> parent;

        java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> prev;

        boolean red;

        java.util.concurrent.ConcurrentHashMap.TreeNode<K, V> right;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ValueIterator<K, V>
            extends java.util.concurrent.ConcurrentHashMap.BaseIterator<K, V>
            implements java.util.Iterator<V>, java.util.Enumeration<V> {

        ValueIterator(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int index,
                int size,
                int limit,
                java.util.concurrent.ConcurrentHashMap<K, V> map) {
            super(null, 0, 0, 0, null);
            throw new RuntimeException("Stub!");
        }

        public V next() {
            throw new RuntimeException("Stub!");
        }

        public V nextElement() {
            throw new RuntimeException("Stub!");
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ValueSpliterator<K, V>
            extends java.util.concurrent.ConcurrentHashMap.Traverser<K, V>
            implements java.util.Spliterator<V> {

        ValueSpliterator(
                java.util.concurrent.ConcurrentHashMap.Node<K, V>[] tab,
                int size,
                int index,
                int limit,
                long est) {
            super(null, 0, 0, 0);
            throw new RuntimeException("Stub!");
        }

        public java.util.concurrent.ConcurrentHashMap.ValueSpliterator<K, V> trySplit() {
            throw new RuntimeException("Stub!");
        }

        public void forEachRemaining(java.util.function.Consumer<? super V> action) {
            throw new RuntimeException("Stub!");
        }

        public boolean tryAdvance(java.util.function.Consumer<? super V> action) {
            throw new RuntimeException("Stub!");
        }

        public long estimateSize() {
            throw new RuntimeException("Stub!");
        }

        public int characteristics() {
            throw new RuntimeException("Stub!");
        }

        long est;
    }

    @SuppressWarnings({"unchecked", "deprecation", "all"})
    static final class ValuesView<K, V>
            extends java.util.concurrent.ConcurrentHashMap.CollectionView<K, V, V>
            implements java.util.Collection<V>, java.io.Serializable {

        ValuesView(java.util.concurrent.ConcurrentHashMap<K, V> map) {
            super(null);
            throw new RuntimeException("Stub!");
        }

        public boolean contains(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public boolean remove(java.lang.Object o) {
            throw new RuntimeException("Stub!");
        }

        public java.util.Iterator<V> iterator() {
            throw new RuntimeException("Stub!");
        }

        public boolean add(V e) {
            throw new RuntimeException("Stub!");
        }

        public boolean addAll(java.util.Collection<? extends V> c) {
            throw new RuntimeException("Stub!");
        }

        public boolean removeIf(java.util.function.Predicate<? super V> filter) {
            throw new RuntimeException("Stub!");
        }

        public java.util.Spliterator<V> spliterator() {
            throw new RuntimeException("Stub!");
        }

        public void forEach(java.util.function.Consumer<? super V> action) {
            throw new RuntimeException("Stub!");
        }

        private static final long serialVersionUID = 2249069246763182397L; // 0x1f364c905893293dL
    }
}
