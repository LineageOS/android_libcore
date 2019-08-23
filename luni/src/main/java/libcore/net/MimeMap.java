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

package libcore.net;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import libcore.util.NonNull;
import libcore.util.Nullable;

/**
 * Maps from MIME types to file extensions and back.
 * @hide
 */
@libcore.api.CorePlatformApi
public abstract class MimeMap {
    private static volatile MimeMap defaultInstance = new DefaultImpl();

    /**
     * A basic implementation of MimeMap used if a new default isn't explicitly
     * {@link MimeMap#setDefault(MimeMap) installed}. Hard-codes enough mappings
     * to satisfy libcore tests. Android framework code is expected to replace
     * this implementation during runtime initialization.
     */
    private static class DefaultImpl extends MimeMap {
        private final Map<String, String> mimeToExt = new HashMap<>();
        private final Map<String, String> extToMime = new HashMap<>();

        private DefaultImpl() {
            put("application/pdf", "pdf");
            put("image/jpeg", "jpg");
            put("image/x-ms-bmp", "bmp");
            put("text/html", "htm", "html");
            put("text/plain", "text", "txt");
            put("text/x-java", "java");
        }

        private void put(String mime, String... exts) {
            mimeToExt.put(mime, exts[0]);
            for (String ext : exts) {
                extToMime.put(ext, mime);
            }
        }

        @Override
        protected @Nullable String guessMimeTypeFromLowerCaseExtension(@NonNull String extension) {
            return extToMime.get(extension);
        }

        @Override
        protected @Nullable String guessExtensionFromLowerCaseMimeType(@NonNull String mimeType) {
            return mimeToExt.get(mimeType);
        }
    }

    @libcore.api.CorePlatformApi
    protected MimeMap() {
    }

    /**
     * @return The system's current default {@link MimeMap}.
     */
    @libcore.api.CorePlatformApi
    public static @NonNull MimeMap getDefault() {
        return defaultInstance;
    }

    /**
     * Sets the system's default {@link MimeMap} to be {@code mimeMap}.
     */
    @libcore.api.CorePlatformApi
    public static void setDefault(@NonNull MimeMap mimeMap) {
        defaultInstance = Objects.requireNonNull(mimeMap);
    }

    /**
     * Returns whether the given case insensitive extension has a registered MIME type.
     *
     * @param extension A file extension without the leading '.'
     * @return Whether a MIME type has been registered for the given case insensitive file
     *         extension.
     */
    @libcore.api.CorePlatformApi
    public final boolean hasExtension(@Nullable String extension) {
        return guessMimeTypeFromExtension(extension) != null;
    }

    /**
     * Returns the MIME type for the given case insensitive file extension.
     * If {@code extension} is {@code null} or {@code ""}, then this method always returns
     * {@code null}. Otherwise, it delegates to
     * {@link #guessMimeTypeFromLowerCaseExtension(String)}.
     *
     * @param extension A file extension without the leading '.'
     * @return The lower-case MIME type registered for the given case insensitive file extension,
     *         or null if there is none.
     */
    @libcore.api.CorePlatformApi
    public final @Nullable String guessMimeTypeFromExtension(@Nullable String extension) {
        if (isNullOrEmpty(extension)) {
            return null;
        }
        extension = toLowerCase(extension);
        String result = guessMimeTypeFromLowerCaseExtension(extension);
        if (result != null) {
            result = toLowerCase(result);
        }
        return result;
    }

    /**
     * @param extension A non-null, non-empty, lowercase file extension.
     * @return The MIME type registered for the given file extension, or null if there is none.
     */
    @libcore.api.CorePlatformApi
    protected abstract @Nullable String guessMimeTypeFromLowerCaseExtension(
            @NonNull String extension);

    /**
     * @param mimeType A MIME type (i.e. {@code "text/plain")
     * @return Whether the given case insensitive MIME type is
     *         {@link #guessMimeTypeFromExtension(String) mapped} to a file extension.
     */
    @libcore.api.CorePlatformApi
    public final boolean hasMimeType(@Nullable String mimeType) {
        return guessExtensionFromMimeType(mimeType) != null;
    }

    /**
     * Returns the registered extension for the given case insensitive MIME type. Note that some
     * MIME types map to multiple extensions. This call will return the most
     * common extension for the given MIME type.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return The lower-case file extension (without the leading "." that has been registered for
     *         the given case insensitive MIME type, or null if there is none.
     */
    @libcore.api.CorePlatformApi
    public final @Nullable String guessExtensionFromMimeType(@Nullable String mimeType) {
        if (isNullOrEmpty(mimeType)) {
            return null;
        }
        mimeType = toLowerCase(mimeType);
        String result = guessExtensionFromLowerCaseMimeType(mimeType);
        if (result != null) {
            result = toLowerCase(result);
        }
        return result;
    }

    /**
     * @param mimeType A non-null, non-empty, lowercase file extension.
     * @return The file extension (without the leading ".") for the given mimeType, or null if
     *         there is none.
     */
    @libcore.api.CorePlatformApi
    protected abstract @Nullable String guessExtensionFromLowerCaseMimeType(
            @NonNull String mimeType);

    /**
     * Returns the canonical (lowercase) form of the given extension or MIME type.
     */
    @libcore.api.CorePlatformApi
    public static @NonNull String toLowerCase(@NonNull String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    @libcore.api.CorePlatformApi
    public static boolean isNullOrEmpty(@Nullable String s) {
        return s == null || s.isEmpty();
    }

}
