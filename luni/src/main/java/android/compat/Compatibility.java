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
 * limitations under the License
 */

package android.compat;

import android.compat.annotation.ChangeId;

import libcore.api.CorePlatformApi;
import libcore.api.IntraCoreApi;

/**
 * Internal APIs for logging and gating compatibility changes.
 *
 * @see ChangeId
 *
 * @hide
 */
@CorePlatformApi
@IntraCoreApi
public final class Compatibility {

    private Compatibility() {}

    /**
     * Reports that a compatibility change is affecting the current process now.
     *
     * <p>Calls to this method from a non-app process are ignored. This allows code implementing
     * APIs that are used by apps and by other code (e.g. the system server) to report changes
     * regardless of the process it's running in. When called in a non-app process, this method is
     * a no-op.
     *
     * <p>Note: for changes that are gated using {@link #isChangeEnabled(long)}, you do not need to
     * call this API directly. The change will be reported for you in the case that
     * {@link #isChangeEnabled(long)} returns {@code true}.
     *
     * @param changeId The ID of the compatibility change taking effect.
     */
    @CorePlatformApi
    @IntraCoreApi
    public static void reportChange(@ChangeId long changeId) {
        sCallbacks.reportChange(changeId);
    }

    /**
     * Query if a given compatibility change is enabled for the current process. This method should
     * only be called by code running inside a process of the affected app.
     *
     * <p>If this method returns {@code true}, the calling code should implement the compatibility
     * change, resulting in differing behaviour compared to earlier releases. If this method returns
     * {@code false}, the calling code should behave as it did in earlier releases.
     *
     * <p>When this method returns {@code true}, it will also report the change as
     * {@link #reportChange(long)} would, so there is no need to call that method directly.
     *
     * @param changeId The ID of the compatibility change in question.
     * @return {@code true} if the change is enabled for the current app.
     */
    @CorePlatformApi
    @IntraCoreApi
    public static boolean isChangeEnabled(@ChangeId long changeId) {
        return sCallbacks.isChangeEnabled(changeId);
    }

    private volatile static Callbacks sCallbacks = new Callbacks();

    @CorePlatformApi
    public static void setCallbacks(Callbacks callbacks) {
        sCallbacks = callbacks;
    }

    /**
     * Base class for compatibility API implementations. The default implementation logs a warning
     * to logcat.
     *
     * This is provided as a class rather than an interface to allow new methods to be added without
     * breaking @CorePlatformApi binary compatibility.
     */
    @CorePlatformApi
    public static class Callbacks {
        @CorePlatformApi
        protected Callbacks() {
        }
        @CorePlatformApi
        protected void reportChange(long changeId) {
            System.logW(String.format(
                    "No Compatibility callbacks set! Reporting change %d", changeId));
        }
        @CorePlatformApi
        protected boolean isChangeEnabled(long changeId) {
            System.logW(String.format(
                    "No Compatibility callbacks set! Querying change %d", changeId));
            return true;
        }
    }

}
