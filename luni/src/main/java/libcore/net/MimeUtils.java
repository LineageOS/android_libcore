/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.net;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilities for dealing with MIME types.
 * Used to implement java.net.URLConnection and android.webkit.MimeTypeMap.
 * @hide
 */
@libcore.api.CorePlatformApi
public final class MimeUtils {

    private MimeUtils() {
    }

    @libcore.api.CorePlatformApi
    public static boolean hasMimeType(String mimeType) {
        return MimeMap.getDefault().hasMimeType(mimeType);
    }

    /**
     * Returns the MIME type for the given case insensitive file extension.
     * @param extension A file extension without the leading '.'
     * @return The MIME type has been registered for
     * the given case insensitive file extension or null if there is none.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static String guessMimeTypeFromExtension(String extension) {
        return MimeMap.getDefault().guessMimeTypeFromExtension(extension);
    }

    /**
     * Returns true if the given case insensitive extension has a registered MIME type.
     * @param extension A file extension without the leading '.'
     * @return True if a MIME type has been registered for
     * the given case insensitive file extension.
     */
    @libcore.api.CorePlatformApi
    public static boolean hasExtension(String extension) {
        return MimeMap.getDefault().hasExtension(extension);
    }

    /**
     * Returns the registered extension for the given case insensitive MIME type. Note that some
     * MIME types map to multiple extensions. This call will return the most
     * common extension for the given MIME type.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return The extension has been registered for
     * the given case insensitive MIME type or null if there is none.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static String guessExtensionFromMimeType(String mimeType) {
        return MimeMap.getDefault().guessExtensionFromMimeType(mimeType);
    }
}
