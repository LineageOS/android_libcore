/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import dalvik.system.VMRuntime;

public final class CollectionsTest extends TestCase {

    private static final Object NOT_A_STRING = new Object();
    private static final Object A_STRING = "string";

    public void testEmptyEnumeration() {
        Enumeration<Object> e = Collections.emptyEnumeration();
        assertFalse(e instanceof Serializable);
        assertFalse(e.hasMoreElements());
        try {
            e.nextElement();
            fail();
        } catch (NoSuchElementException expected) {
        }
    }

    public void testEmptyIterator() {
        testEmptyIterator(Collections.emptyIterator());
        testEmptyIterator(Collections.emptyList().iterator());
        testEmptyIterator(Collections.emptySet().iterator());
        testEmptyIterator(Collections.emptyMap().keySet().iterator());
        testEmptyIterator(Collections.emptyMap().entrySet().iterator());
        testEmptyIterator(Collections.emptyMap().values().iterator());
    }

    private void testEmptyIterator(Iterator<?> i) {
        assertFalse(i instanceof Serializable);
        assertFalse(i.hasNext());
        try {
            i.next();
            fail();
        } catch (NoSuchElementException expected) {
        }
        try {
            i.remove();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    public void testEmptyListIterator() {
        testEmptyListIterator(Collections.emptyListIterator());
        testEmptyListIterator(Collections.emptyList().listIterator());
        testEmptyListIterator(Collections.emptyList().listIterator(0));
    }

    private void testEmptyListIterator(ListIterator<?> i) {
        assertFalse(i instanceof Serializable);
        assertFalse(i.hasNext());
        assertFalse(i.hasPrevious());
        assertEquals(0, i.nextIndex());
        try {
            i.next();
            fail();
        } catch (NoSuchElementException expected) {
        }
        assertEquals(-1, i.previousIndex());
        try {
            i.previous();
            fail();
        } catch (NoSuchElementException expected) {
        }
        try {
            i.add(null);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
        try {
            i.remove();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    static final class ArrayListInheritor<T> extends ArrayList<T> {
        private int numSortCalls = 0;
        public ArrayListInheritor(Collection<T> initialElements) {
            super(initialElements);
        }

        @Override
        public void sort(Comparator<? super T> c) {
            super.sort(c);
            numSortCalls++;
        }

        public int numSortCalls() {
            return numSortCalls;
        }
    }

    /**
     * Tests that when targetSdk {@code <= 25}, Collections.sort() does not delegate
     * to List.sort().
     */
    public void testSort_nougatOrEarlier_doesNotDelegateToListSort() {
        runOnTargetSdk(25, () -> { // Nougat MR1 / MR2
            ArrayListInheritor<String> list = new ArrayListInheritor<>(
                    Arrays.asList("a", "c", "b"));
            assertEquals(0, list.numSortCalls());
            Collections.sort(list);
            assertEquals(0, list.numSortCalls());
        });
    }

    public void testSort_postNougat_delegatesToListSort() {
        runOnTargetSdkAtLeast(26, () -> {
            ArrayListInheritor<String> list = new ArrayListInheritor<>(
                    Arrays.asList("a", "c", "b"));
            assertEquals(0, list.numSortCalls());
            Collections.sort(list);
            assertEquals(1, list.numSortCalls());
        });
    }

    public void testSort_modcountUnmodifiedForLinkedList() {
        runOnTargetSdkAtLeast(26, () -> {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(
                    "red", "green", "blue", "violet"));
            Iterator<String> it = list.iterator();
            it.next();
            Collections.sort(list);
            it.next(); // does not throw ConcurrentModificationException
        });
    }

    public void testSort_modcountModifiedForArrayListAndSubclasses() {
        runOnTargetSdkAtLeast(26, () -> {
            List<String> testData = Arrays.asList("red", "green", "blue", "violet");

            ArrayList<String> list = new ArrayList<>(testData);
            Iterator<String> it = list.iterator();
            it.next();
            Collections.sort(list);
            try {
                it.next();
                fail();
            } catch (ConcurrentModificationException expected) {
            }

            list = new ArrayListInheritor<>(testData);
            it = list.iterator();
            it.next();
            Collections.sort(list);
            try {
                it.next();
                fail();
            } catch (ConcurrentModificationException expected) {
            }
        });
    }

    /**
     * Runs the given runnable on this thread with the targetSdkVersion temporarily set
     * to the specified value, unless the current value is already higher.
     */
    private static void runOnTargetSdkAtLeast(int minimumTargetSdkForTest, Runnable runnable) {
        int targetSdkForTest = Math.max(minimumTargetSdkForTest,
                VMRuntime.getRuntime().getTargetSdkVersion());
        runOnTargetSdk(targetSdkForTest, runnable);
    }

    /**
     * Runs the given runnable on this thread with the targetSdkVersion temporarily set
     * to the specified value. This helps test behavior that depends on an API level
     * other than the current one (e.g. between releases).
     */
    private static void runOnTargetSdk(int targetSdkForTest, Runnable runnable) {
        VMRuntime runtime = VMRuntime.getRuntime();
        int targetSdk = runtime.getTargetSdkVersion();
        try {
            runtime.setTargetSdkVersion(targetSdkForTest);
            runnable.run();
        } finally {
            runtime.setTargetSdkVersion(targetSdk);
        }
    }

    /**
     * A value type whose {@code compareTo} method returns one of {@code 0},
     * {@code Integer.MIN_VALUE} and {@code Integer.MAX_VALUE}.
     */
    static final class IntegerWithExtremeComparator
            implements Comparable<IntegerWithExtremeComparator> {
        private final int value;

        public IntegerWithExtremeComparator(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(IntegerWithExtremeComparator another) {
            if (another.value == this.value) {
                return 0;
            } else if (another.value > this.value) {
                return Integer.MIN_VALUE;
            } else {
                return Integer.MAX_VALUE;
            }
        }
    }

    // http://b/19749094
    public void testBinarySearch_comparatorThatReturnsMinAndMaxValue() {
        ArrayList<Integer> list = new ArrayList<Integer>(16);
        list.add(4);
        list.add(9);
        list.add(11);
        list.add(14);
        list.add(16);

        int index = Collections.binarySearch(list, 9, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                final int compare = lhs.compareTo(rhs);
                if (compare == 0) {
                    return 0;
                } else if (compare < 0) {
                    return Integer.MIN_VALUE;
                } else {
                    return Integer.MAX_VALUE;
                }
            }
        });
        assertEquals(1, index);

        ArrayList<IntegerWithExtremeComparator> list2 =
                new ArrayList<IntegerWithExtremeComparator>();
        list2.add(new IntegerWithExtremeComparator(4));
        list2.add(new IntegerWithExtremeComparator(9));
        list2.add(new IntegerWithExtremeComparator(11));
        list2.add(new IntegerWithExtremeComparator(14));
        list2.add(new IntegerWithExtremeComparator(16));

        assertEquals(1, Collections.binarySearch(list2, new IntegerWithExtremeComparator(9)));
    }

    public void testBinarySearch_emptyCollection() {
        assertEquals(-1, Collections.binarySearch(new ArrayList<Integer>(), 9));

        assertEquals(-1, Collections.binarySearch(new ArrayList<>(), 9, Integer::compareTo));
    }

    public void testSingletonSpliterator() {
        Spliterator<String> sp = Collections.singletonList("spiff").spliterator();

        assertEquals(1, sp.estimateSize());
        assertEquals(1, sp.getExactSizeIfKnown());
        assertNull(sp.trySplit());
        assertEquals(true, sp.tryAdvance(value -> assertEquals("spiff", value)));
        assertEquals(false, sp.tryAdvance(value -> fail()));
    }

    public void test_unmodifiableMap_getOrDefault() {
        HashMap<Integer, Double> hashMap = new HashMap<>();
        hashMap.put(2, 12.0);
        hashMap.put(3, null);
        Map<Integer, Double> m = Collections.unmodifiableMap(hashMap);
        assertEquals(-1.0, m.getOrDefault(1, -1.0));
        assertEquals(12.0, m.getOrDefault(2, -1.0));
        assertEquals(null, m.getOrDefault(3, -1.0));
    }

    public void test_unmodifiableMap_forEach() {
        Map<Integer, Double> hashMap = new HashMap<>();
        Map<Integer, Double> replica = new HashMap<>();
        hashMap.put(1, 10.0);
        hashMap.put(2, 20.0);
        Collections.unmodifiableMap(hashMap).forEach(replica::put);
        assertEquals(10.0, replica.get(1));
        assertEquals(20.0, replica.get(2));
        assertEquals(2, replica.size());
    }

    public void test_unmodifiableMap_putIfAbsent() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_remove() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_replace$K$V$V() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).replace(1, 5.0, 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).replace(1, 5.0, 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_replace$K$V() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_computeIfAbsent() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).computeIfAbsent(1, k -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).computeIfAbsent(1, k -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_computeIfPresent() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).computeIfPresent(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).computeIfPresent(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_compute() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).compute(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).compute(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_merge() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).merge(1, 2.0, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        HashMap<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).merge(1, 2.0, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_getOrDefault() {
        Map<Integer, Double> m = Collections.emptyMap();
        assertEquals(-1.0, m.getOrDefault(1, -1.0));
        assertEquals(-1.0, m.getOrDefault(2, -1.0));
    }

    public void test_EmptyMap_forEach() {
        try {
            Collections.emptyMap().forEach(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_EmptyMap_putIfAbsent() {
        try {
            Collections.emptyMap().putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_remove() {
        try {
            Collections.emptyMap().remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_replace$K$V$V() {
        try {
            Collections.emptyMap().replace(1, 5.0, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_replace$K$V() {
        try {
            Collections.emptyMap().replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_computeIfAbsent() {
        try {
            Collections.emptyMap().computeIfAbsent(1, k -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_computeIfPresent() {
        try {
            Collections.emptyMap().computeIfPresent(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_compute() {
        try {
            Collections.emptyMap().compute(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_merge() {
        try {
            Collections.emptyMap().merge(1, 5.0, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_getOrDefault() {
        Map<Integer, Double> m = Collections.singletonMap(1, 11.0);
        assertEquals(11.0, m.getOrDefault(1, -1.0));
        assertEquals(-1.0, m.getOrDefault(2, -1.0));
    }

    public void test_SingletonMap_forEach() {
        Map<Integer, Double> m = new HashMap<>();
        Collections.singletonMap(1, 11.0).forEach(m::put);
        assertEquals(11.0, m.getOrDefault(1, -1.0));
        assertEquals(1, m.size());
    }

    public void test_SingletonMap_putIfAbsent() {
        try {
            Collections.singletonMap(1, 11.0).putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_remove() {
        try {
            Collections.singletonMap(1, 11.0).remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_replace$K$V$V() {
        try {
            Collections.singletonMap(1, 11.0).replace(1, 5.0, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_replace$K$V() {
        try {
            Collections.singletonMap(1, 11.0).replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_computeIfAbsent() {
        try {
            Collections.singletonMap(1, 11.0).computeIfAbsent(1, k -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_computeIfPresent() {
        try {
            Collections.singletonMap(1, 11.0).computeIfPresent(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_compute() {
        try {
            Collections.singletonMap(1, 11.0).compute(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_merge() {
        try {
            Collections.singletonMap(1, 11.0).merge(1, 5.0, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SynchronizedList_replaceAll() {
        ListDefaultMethodTester.test_replaceAll(Collections.synchronizedList(new ArrayList<>()));
    }

    public void test_SynchronizedList_sort() {
        ListDefaultMethodTester.test_sort(Collections.synchronizedList(new ArrayList<>()));
    }

    public void test_CheckedList_replaceAll() {
        ListDefaultMethodTester.test_replaceAll(Collections.checkedList(new ArrayList<>(), Integer.class));
    }

    public void test_CheckedList_sort() {
        ListDefaultMethodTester.test_sort(Collections.checkedList(new ArrayList<>(), Double.class));
    }

    public void test_EmptyList_replaceAll() {
        Collections.emptyList().replaceAll(k -> 1);

        try {
            Collections.emptyList().replaceAll(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_EmptyList_sort() {
        Collections.emptyList().sort((k1, k2) -> 1);
    }

    public void test_unmodifiableList_replaceAll() {
        try {
            Collections.unmodifiableList(new ArrayList<>()).replaceAll(k -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // with non empty list

        try {
            ArrayList l = new ArrayList();
            l.add(1);
            l.add(2);
            Collections.unmodifiableList(l).replaceAll(k -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableList_sort() {
        try {
            Collections.unmodifiableList(new ArrayList<>()).sort((k1, k2) -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // with non empty list

        try {
            ArrayList l = new ArrayList();
            l.add(1);
            l.add(2);
            Collections.unmodifiableList(l).sort((k1, k2) -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonList_replaceAll() {
        try {
            Collections.singletonList(1).replaceAll(k -> 2);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonList_sort() {
        Collections.singletonList(1).sort((k1, k2) -> 2);
    }

    public void test_CheckedMap_replaceAll() {
        Map<Integer, Integer> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Integer.class);
        checkedMap.put(1, 10);
        checkedMap.put(2, 20);
        checkedMap.put(3, 30);
        checkedMap.replaceAll((k, v) -> (Integer)k + (Integer)v);
        assertEquals(11, checkedMap.get(1));
        assertEquals(22, checkedMap.get(2));
        assertEquals(33, checkedMap.get(3));
        assertEquals(3, checkedMap.size());
    }

    public void test_CheckedMap_putIfAbsent() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_putIfAbsent(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);

        // When key is present
        checkedMap2.putIfAbsent(1, A_STRING);
        try {
            checkedMap2.putIfAbsent(1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}

        // When key is absent
        checkedMap2.clear();
        try {
            checkedMap2.putIfAbsent(1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_remove() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_remove(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);
    }

    public void test_CheckedMap_replace$K$V$V() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_replace$K$V$V(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.replace(1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_replace$K$V() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_replace$K$V(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.replace(1, 1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_computeIfAbsent() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_computeIfAbsent(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        // When key is present, function should not be invoked
        assertSame(A_STRING, checkedMap2.computeIfAbsent(1, k -> {
            throw new AssertionFailedError("key present: function should not be invoked");
        }));

        // When key is absent, computed value's type should be checked
        checkedMap2.clear();
        try {
            checkedMap2.computeIfAbsent(1, k -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_computeIfPresent() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_computeIfPresent(checkedMap, true /* acceptsNullKey */);

        // Without generics to check the typeCheck implementation
        Map m = new HashMap();
        Map checkedMap2 = Collections.checkedMap(m, Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.computeIfPresent(1, (k, v) -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_compute() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_compute(checkedMap, true /* acceptsNullKey */);

        Map checkedMap2 = Collections.checkedMap(new HashMap(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);
        try {
            checkedMap2.compute(1, (k, v) -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_merge() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_merge(checkedMap, true /* acceptsNullKey */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 =
                Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.merge(1, A_STRING, (v1, v2) -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }
}
