/*
 * Copyright (C) 2016 The Android Open Source Project
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
package libcore.junit.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Provides support for testing classes that own resources which must not leak.
 *
 * <p><strong>This will not detect any resource leakages in OpenJDK</strong></p>
 */
public class ResourceLeakageDetector {
    private static final LeakageDetectorRule LEAKAGE_DETECTOR_RULE;
    private static final BiConsumer<Object, Integer> FINALIZER_CHECKER;

    static {
        LeakageDetectorRule leakageDetectorRule;
        BiConsumer<Object, Integer> finalizerChecker;
        try {
            // Make sure that the CloseGuard class exists; this ensures that this is not
            // running on a RI JVM.
            Class.forName("dalvik.system.CloseGuard");

            // Access the underlying support class using reflection in order to prevent any compile
            // time dependencies on it so as to allow this to compile on OpenJDK.
            Class<?> closeGuardSupportClass = Class.forName("dalvik.system.CloseGuardSupport");
            Method method = closeGuardSupportClass.getMethod("getRule");
            leakageDetectorRule = new LeakageDetectorRule((TestRule) method.invoke(null));

            finalizerChecker = getFinalizerChecker(closeGuardSupportClass);

        } catch (ReflectiveOperationException e) {
            System.err.println("Resource leakage will not be detected; "
                    + "this is expected in the reference implementation");
            e.printStackTrace(System.err);

            // Could not access the class for some reason so have a rule that does nothing and a
            // finalizer checker that checks nothing. This should ensure that tests work properly
            // on OpenJDK even though it does not support CloseGuard.
            leakageDetectorRule = new LeakageDetectorRule(RuleChain.emptyRuleChain());
            finalizerChecker = new BiConsumer<Object, Integer>() {
                @Override
                public void accept(Object o, Integer integer) {
                    // Do nothing.
                }
            };
        }

        LEAKAGE_DETECTOR_RULE = leakageDetectorRule;
        FINALIZER_CHECKER = finalizerChecker;
    }

    @SuppressWarnings("unchecked")
    private static BiConsumer<Object, Integer> getFinalizerChecker(Class<?> closeGuardSupportClass)
            throws ReflectiveOperationException {
        Method method = closeGuardSupportClass.getMethod("getFinalizerChecker");
        return (BiConsumer<Object, Integer>) method.invoke(null);
    }

    /**
     * @return the {@link LeakageDetectorRule}
     */
    public static LeakageDetectorRule getRule() {
       return LEAKAGE_DETECTOR_RULE;
    }

    /**
     * A {@link TestRule} that will fail a test if it detects any resources that were allocated
     * during the test but were not released.
     *
     * <p>This only tracks resources that were allocated on the test thread, although it does not
     * care what thread they were released on. This avoids flaky false positives where a background
     * thread allocates a resource during a test but releases it after the test.
     *
     * <p>It is still possible to have a false positive in the case where the test causes a caching
     * mechanism to open a resource and hold it open past the end of the test. In that case if there
     * is no way to clear the cached data then it should be relatively simple to move the code that
     * invokes the caching mechanism to outside the scope of this rule. i.e.
     *
     * <pre>{@code
     *     @Rule
     *     public final TestRule ruleChain = org.junit.rules.RuleChain
     *         .outerRule(new ...invoke caching mechanism...)
     *         .around(CloseGuardSupport.getRule());
     * }</pre>
     *
     * @return a {@link TestRule} that detects resource leakages, or one that does nothing if
     * resource leakage detection is not supported.
     */
    public static class LeakageDetectorRule implements TestRule {

        private final TestRule leakageDetectorRule;

        private LeakageDetectorRule(TestRule leakageDetectorRule) {
            this.leakageDetectorRule = leakageDetectorRule;
        }

        @Override
        public Statement apply(Statement base, Description description) {
            // Make the resource leakage detector rule optional based on the presence of an
            // annotation.
            if (description.getAnnotation(DisableResourceLeakageDetection.class) != null) {
                return base;
            } else {
                return leakageDetectorRule.apply(base, description);
            }
        }

        /**
         * Ensure that when the supplied object is finalized that it detects the expected number of
         * unreleased resources.
         *
         * <p>This helps ensure that classes which own resources protected using {@code CloseGuard}
         * support leakage detection.
         *
         * <p>This must only be called from within the test currently being run that is not
         * annotated with {@link DisableResourceLeakageDetection} otherwise it will fail if the
         * resource leakage detected mechanism is disabled, e.g. in CTS.
         *
         * <p>Use as follows:
         * <pre>
         *     Object object = ...create and 'open' an object encapsulating a protected resource...;
         *     // Check to make sure that the object reports a resource leak when it is finalized.
         *     assertUnreleasedResourceCount(object, 1);
         *
         *     object = ... create, 'open' and then 'close' another object ...;
         *     // Check to make sure that the object does not have any unreleased resources.
         *     assertUnreleasedResourceCount(object, 0);
         * </pre>
         *
         * @param owner the object that owns the resource and uses {@code CloseGuard} object to detect
         * when the resource is not released.
         * @param expectedCount the expected number of unreleased resources.
         */
        public void assertUnreleasedResourceCount(Object owner, int expectedCount) {
            FINALIZER_CHECKER.accept(owner, expectedCount);
        }
    }

    /**
     * An annotation that indicates that the test should not be run with resource leakage detection
     * enabled.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface DisableResourceLeakageDetection {

        /**
         * The explanation as to why resource leakage detection is disabled for this test.
         */
        String why();

        /**
         * The bug reference to the bug that was opened to fix the issue.
         */
        String bug();
    }
}
