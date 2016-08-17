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

package libcore.java.lang.reflect.annotations;

import junit.framework.TestCase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationB;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationC;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationD;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Container;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Repeated;

import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.EXPECT_EMPTY;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.annotationsToTypes;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertAnnotationsMatch;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.set;

public class ParameterTest extends TestCase {
    private static class Type {
        @AnnotationB
        @AnnotationC
        public void method(String parameter1, String parameter2) {}

        @AnnotationB
        @AnnotationC
        public void parameters(@AnnotationB @AnnotationD String parameter1,
                @AnnotationC @AnnotationD String parameter2) {}
    }

    public void testParameterAnnotations() throws Exception {
        Method method = Type.class.getMethod("method", String.class, String.class);
        Annotation[][] noParameterAnnotations = method.getParameterAnnotations();
        assertEquals(2, noParameterAnnotations.length);
        assertEquals(set(), annotationsToTypes(noParameterAnnotations[0]));
        assertEquals(set(), annotationsToTypes(noParameterAnnotations[1]));

        Method parameters = Type.class.getMethod("parameters", String.class, String.class);
        Annotation[][] parameterAnnotations = parameters.getParameterAnnotations();
        assertEquals(2, parameterAnnotations.length);
        assertEquals(set(AnnotationB.class, AnnotationD.class),
                annotationsToTypes(parameterAnnotations[0]));
        assertEquals(set(AnnotationC.class, AnnotationD.class),
                annotationsToTypes(parameterAnnotations[1]));
    }

    private static class AnnotatedClass {
        public void singleAnnotation(@Repeated(1) String p0) {}

        public void multipleAnnotation(@Repeated(1) @Repeated(2) String p0) {}

        public void multipleAnnotationExplicitSingle(@Container({@Repeated(1)}) String p0) {}

        public void multipleAnnotationOddity(
                @Repeated(1) @Container({@Repeated(2), @Repeated(3)}) String p0) {}

        public void noAnnotation(String p0) {}
    }

    public void testGetParameterAnnotations() throws Exception {
        Class<?> c = AnnotatedClass.class;

        assertParameter0Annotations(c, "noAnnotation", EXPECT_EMPTY);
        assertParameter0Annotations(c, "multipleAnnotationOddity",
                "@Repeated(1)", "@Container({@Repeated(2), @Repeated(3)})");
        assertParameter0Annotations(c, "multipleAnnotationExplicitSingle",
                "@Container({@Repeated(1)})");
        assertParameter0Annotations(c, "multipleAnnotation",
                "@Container({@Repeated(1), @Repeated(2)})");
        assertParameter0Annotations(c, "singleAnnotation",
                "@Repeated(1)");
    }

    private static void assertParameter0Annotations(
            Class<?> c, String methodName, String... expectedAnnotationStrings) throws Exception {
        Annotation[][] allAnnotations =
                c.getDeclaredMethod(methodName, String.class).getParameterAnnotations();
        final int expectedParameterCount = 1;
        assertEquals(expectedParameterCount, allAnnotations.length);

        Annotation[] p0Annotations = allAnnotations[0];
        assertAnnotationsMatch(p0Annotations, expectedAnnotationStrings);
    }
}
