/*
 * Copyright (C) 2019 The Android Open Source Project
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
package libcore.java.io;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import libcore.junit.util.SwitchTargetSdkVersionRule;
import libcore.junit.util.SwitchTargetSdkVersionRule.TargetSdkVersion;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ObjectStreamClassTest {

  @Rule
  public TestRule switchTargetSdkVersionRule = SwitchTargetSdkVersionRule.getInstance();

  /**
   * The default SUID for this should not be affected by the b/29064453 patch.
   */
  private static class BaseWithStaticInitializer implements Serializable {
    static {
      System.out.println(
          "Static initializer for " + BaseWithoutStaticInitializer.class.getCanonicalName());
    }
  }

  /**
   * The default SUID for this should not be affected by the b/29064453 patch.
   */
  private static class BaseWithoutStaticInitializer implements Serializable {
  }

  /**
   * The default SUID for this should not be affected by the b/29064453 patch.
   */
  private static class WithStaticInitializer extends BaseWithoutStaticInitializer {
    static {
      System.out.println(
          "Static initializer for " + WithStaticInitializer.class.getCanonicalName());
    }
  }

  /**
   * The default SUID for this should not be affected by the b/29064453 patch.
   */
  private static class WithoutStaticInitializer extends BaseWithoutStaticInitializer {
  }

  /**
   * The default SUID for this should be affected by the b/29064453 patch and so should differ
   * between version <= 23 and version > 23.
   */
  private static class InheritStaticInitializer extends BaseWithStaticInitializer {
  }

  public static Object[][] defaultSUIDs() {
    return new Object[][] {
        // The default SUID for BaseWithStaticInitializer should not be affected by the b/29064453
        // patch.
        { BaseWithStaticInitializer.class, -4971959491244110788L, -4971959491244110788L },

        // The default SUID for BaseWithoutStaticInitializer should not be affected by the
        // b/29064453 patch.
        { BaseWithoutStaticInitializer.class, -245652310465925293L, -245652310465925293L },

        // The default SUID for WithStaticInitializer should not be affected by the b/29064453
        // patch.
        { WithStaticInitializer.class, -3581047773254885060L, -3581047773254885060L },

        // The default SUID for WithStaticInitializer should not be affected by the
        // b/29064453 patch.
        { WithoutStaticInitializer.class, -975889567651927545L, -975889567651927545L },

        // The default SUID for the InheritStaticInitializer should be affected by the b/29064453
        // patch and so should differ between version <= 23 and version > 23.
        { InheritStaticInitializer.class, 4188245044387716731L, 992629205079295334L },


    };
  }

  @Parameters(method = "defaultSUIDs")
  @Test
  public void computeDefaultSUID_current(Class<?> clazz, long suid,
      @SuppressWarnings("unused") long suid23) {
    checkSerialVersionUID(suid, clazz);
  }

  @Parameters(method = "defaultSUIDs")
  @Test
  @TargetSdkVersion(23)
  public void computeDefaultSUID_targetSdkVersion_23(Class<?> clazz,
      @SuppressWarnings("unused") long suid, long suid23) {
    checkSerialVersionUID(suid23, clazz);
  }

  private static void checkSerialVersionUID(long expectedSUID, Class<?> clazz) {
    // Use reflection to access the private static computeDefaultSUID method.
    long defaultSUID;
    try {
      Method computeDefaultSUIDMethod =
          ObjectStreamClass.class.getDeclaredMethod("computeDefaultSUID", Class.class);
      computeDefaultSUIDMethod.setAccessible(true);
      defaultSUID = (Long) computeDefaultSUIDMethod.invoke(null, clazz);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
    assertEquals(expectedSUID, defaultSUID);
  }
}
