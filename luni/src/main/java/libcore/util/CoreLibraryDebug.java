/*
 * Copyright (C) 2018 The Android Open Source Project
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

package libcore.util;

import libcore.timezone.TimeZoneDataFiles;
import libcore.timezone.TzDataSetVersion;
import libcore.timezone.TzDataSetVersion.TzDataSetException;

import java.io.File;
import java.io.IOException;

/**
 * Provides APIs for obtaining metadata for the managed core library and lower-level
 * components like bionic and the runtime.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public class CoreLibraryDebug {
    private CoreLibraryDebug() {}

    /**
     * Returns information about the Core Library for debugging.
     */
    @libcore.api.CorePlatformApi
    public static DebugInfo getDebugInfo() {
        DebugInfo debugInfo = new DebugInfo();
        populateTimeZoneInfo(debugInfo);
        return debugInfo;
    }

    /** Adds time zone data information to the supplied {@link DebugInfo}. */
    private static void populateTimeZoneInfo(DebugInfo debugInfo) {
        // Time zone module tz data set.
        {
            String debugKeyPrefix = "core_library.timezone.tzdata_module_";
            String versionFileName =
                    TimeZoneDataFiles.getTimeZoneModuleFile(TzDataSetVersion.DEFAULT_FILE_NAME);
            addTzDataSetVersionDebugInfo(versionFileName, debugKeyPrefix, debugInfo);
        }
        // /system tz data set.
        {
            String debugKeyPrefix = "core_library.timezone.system_";
            String versionFileName =
                    TimeZoneDataFiles.getSystemTimeZoneFile(TzDataSetVersion.DEFAULT_FILE_NAME);
            addTzDataSetVersionDebugInfo(versionFileName, debugKeyPrefix, debugInfo);
        }
    }

    private static void addTzDataSetVersionDebugInfo(String tzDataSetVersionFileName,
            String debugKeyPrefix, DebugInfo debugInfo) {
        File file = new File(tzDataSetVersionFileName);
        String statusKey = debugKeyPrefix + "status";
        if (file.exists()) {
            try {
                TzDataSetVersion tzDataSetVersion =
                        TzDataSetVersion.readFromFile(file);
                String formatVersionString = tzDataSetVersion.formatMajorVersion + "."
                        + tzDataSetVersion.formatMinorVersion;
                debugInfo.addStringEntry(statusKey, "OK")
                        .addStringEntry(debugKeyPrefix + "formatVersion", formatVersionString)
                        .addStringEntry(debugKeyPrefix + "rulesVersion",
                                tzDataSetVersion.rulesVersion)
                        .addStringEntry(debugKeyPrefix + "revision",
                                tzDataSetVersion.revision);
            } catch (IOException | TzDataSetException e) {
                debugInfo.addStringEntry(statusKey, "ERROR");
                debugInfo.addStringEntry(debugKeyPrefix + "exception_class", e.getClass().getName());
                debugInfo.addStringEntry(debugKeyPrefix + "exception_msg", e.getMessage());
                System.logE("Error reading " + file, e);
            }
        } else {
            debugInfo.addStringEntry(statusKey, "NOT_FOUND");
        }
    }

}
