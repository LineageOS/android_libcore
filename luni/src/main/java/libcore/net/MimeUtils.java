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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilities for dealing with MIME types.
 * Used to implement java.net.URLConnection and android.webkit.MimeTypeMap.
 */
public final class MimeUtils {
    private static final Pattern splitPattern = Pattern.compile("\\s+");

    private static final Map<String, String> mimeTypeToExtensionMap = new HashMap<String, String>();

    private static final Map<String, String> extensionToMimeTypeMap = new HashMap<String, String>();

    static {
        parseTypes("mime.types");
        parseTypes("android.mime.types");
    }

    private static void parseTypes(String resource) {
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(MimeUtils.class.getResourceAsStream(resource)))) {
            String line;
            while ((line = r.readLine()) != null) {
                int commentPos = line.indexOf('#');
                if (commentPos >= 0) {
                    line = line.substring(0, commentPos);
                }
                line = line.trim();
                if (line.equals("")) {
                    continue;
                }

                final String[] split = splitPattern.split(line);
                final String mimeType = split[0];
                for (int i = 1; i < split.length; i++) {
                    String extension = split[i].toLowerCase(Locale.US);

                    // Normally the first MIME type definition wins, and the
                    // last extension definition wins. However, a file can
                    // override a MIME type definition by adding the "!" suffix
                    // to an extension.

                    if (extension.endsWith("!")) {
                        extension = extension.substring(0, extension.length() - 1);

                        // Overriding MIME definition wins
                        mimeTypeToExtensionMap.put(mimeType, extension);
                    } else {
                        // First MIME definition wins
                        if (!mimeTypeToExtensionMap.containsKey(mimeType)) {
                            mimeTypeToExtensionMap.put(mimeType, extension);
                        }
                    }

                    // Last extension definition wins
                    extensionToMimeTypeMap.put(extension, mimeType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse " + resource, e);
        }
    }

    /**
     * Returns true if the given case insensitive MIME type has an entry in the map.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return True if a extension has been registered for
     * the given case insensitive MIME type.
     */
    public static boolean hasMimeType(String mimeType) {
        return (guessExtensionFromMimeType(mimeType) != null);
    }

    /**
     * Returns the MIME type for the given case insensitive file extension.
     * @param extension A file extension without the leading '.'
     * @return The MIME type has been registered for
     * the given case insensitive file extension or null if there is none.
     */
    public static String guessMimeTypeFromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }
        extension = extension.toLowerCase(Locale.US);
        return extensionToMimeTypeMap.get(extension);
    }

    /**
     * Returns true if the given case insensitive extension has a registered MIME type.
     * @param extension A file extension without the leading '.'
     * @return True if a MIME type has been registered for
     * the given case insensitive file extension.
     */
    public static boolean hasExtension(String extension) {
        return (guessMimeTypeFromExtension(extension) != null);
    }

    /**
     * Returns the registered extension for the given case insensitive MIME type. Note that some
     * MIME types map to multiple extensions. This call will return the most
     * common extension for the given MIME type.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return The extension has been registered for
     * the given case insensitive MIME type or null if there is none.
     */
    public static String guessExtensionFromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return null;
        }
        mimeType = mimeType.toLowerCase(Locale.US);
        return mimeTypeToExtensionMap.get(mimeType);
    }
}
