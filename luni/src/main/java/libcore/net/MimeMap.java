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

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import libcore.util.NonNull;
import libcore.util.Nullable;

/**
 * Maps from MIME types to file extensions and back.
 * @hide
 */
public abstract class MimeMap {
    private static AtomicReference<MimeMap> defaultHolder = new AtomicReference<>(
            MimeMapImpl.parseFromResources("mime.types", "android.mime.types"));

    /**
     * @return The system's current default {@link MimeMap}.
     */
    public static @NonNull MimeMap getDefault() {
        return defaultHolder.get();
    }

    /**
     * Atomically sets the system's default {@link MimeMap} to be {@code update} if the
     * current value {@code == expect}.
     *
     * @param expect the expected current default {@link MimeMap}; must not be null.
     * @param update the new default {@link MimeMap} to set; must not be null.
     * @return whether the update was successful.
     */
    public static boolean compareAndSetDefault(@NonNull MimeMap expect, @NonNull MimeMap update) {
        Objects.requireNonNull(expect);
        Objects.requireNonNull(update);
        return defaultHolder.compareAndSet(expect, update);
    }

    /**
     * Returns whether the given case insensitive extension has a registered MIME type.
     *
     * @param extension A file extension without the leading '.'
     * @return Whether a MIME type has been registered for the given case insensitive file
     *         extension.
     */
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
    protected abstract @Nullable String guessMimeTypeFromLowerCaseExtension(
            @NonNull String extension);

    /**
     * @param mimeType A MIME type (i.e. {@code "text/plain")
     * @return Whether the given case insensitive MIME type is
     *         {@link #guessMimeTypeFromExtension(String) mapped} to a file extension.
     */
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
    protected abstract @Nullable String guessExtensionFromLowerCaseMimeType(
            @NonNull String mimeType);

    /**
     * Returns the canonical (lowercase) form of the given extension or MIME type.
     */
    static @NonNull String toLowerCase(@NonNull String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    static boolean isNullOrEmpty(@Nullable String s) {
        return s == null || s.isEmpty();
    }

}
