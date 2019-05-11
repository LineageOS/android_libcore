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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class MimeMapImpl extends MimeMap {

    private static final Pattern splitPattern = Pattern.compile("\\s+");

    /**
     * Note: These maps only contain lowercase keys/values, regarded as the
     * {@link #toLowerCase(String) canonical form}.
     *
     * <p>This is the case for both extensions and MIME types. The mime.types
     * data file contains examples of mixed-case MIME types, but some applications
     * use the lowercase version of these same types. RFC 2045 section 2 states
     * that MIME types are case insensitive.
     */
    private final Map<String, String> mimeTypeToExtension;
    private final Map<String, String> extensionToMimeType;

    public MimeMapImpl(Map<String, String> mimeTypeToExtension,
            Map<String, String> extensionToMimeType) {
        this.mimeTypeToExtension = new HashMap<>(mimeTypeToExtension);
        for (Map.Entry<String, String> entry : mimeTypeToExtension.entrySet()) {
            checkValidMimeType(entry.getKey());
            checkValidExtension(entry.getValue());
        }
        this.extensionToMimeType = new HashMap<>(extensionToMimeType);
        for (Map.Entry<String, String> entry : extensionToMimeType.entrySet()) {
            checkValidExtension(entry.getKey());
            checkValidMimeType(entry.getValue());
        }
    }

    private static void checkValidMimeType(String s) {
        if (MimeMap.isNullOrEmpty(s) || !s.equals(MimeMap.toLowerCase(s))) {
            throw new IllegalArgumentException("Invalid MIME type: " + s);
        }
    }

    private static void checkValidExtension(String s) {
        if (isNullOrEmpty(s) || !s.equals(toLowerCase(s))) {
            throw new IllegalArgumentException("Invalid extension: " + s);
        }
    }

    static MimeMapImpl parseFromResources(String... resourceNames) {
        Map<String, String> mimeTypeToExtension = new HashMap<>();
        Map<String, String> extensionToMimeType = new HashMap<>();
        for (String resourceName : resourceNames) {
            parseTypes(mimeTypeToExtension, extensionToMimeType, resourceName);
        }
        return new MimeMapImpl(mimeTypeToExtension, extensionToMimeType);
    }

    private static void parseTypes(Map<String, String> mimeTypeToExtension,
            Map<String, String> extensionToMimeType, String resource) {
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(MimeMap.class.getResourceAsStream(resource)))) {
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
                final String mimeType = toLowerCase(split[0]);
                if (isNullOrEmpty(mimeType)) {
                    throw new IllegalArgumentException(
                            "Invalid mimeType " + mimeType + " in: " + line);
                }
                for (int i = 1; i < split.length; i++) {
                    String extension = toLowerCase(split[i]);
                    if (isNullOrEmpty(extension)) {
                        throw new IllegalArgumentException(
                                "Invalid extension " + extension + " in: " + line);
                    }

                    // Normally the first MIME type definition wins, and the
                    // last extension definition wins. However, a file can
                    // override a MIME type definition by adding the "!" suffix
                    // to an extension.

                    if (extension.endsWith("!")) {
                        if (i != 1) {
                            throw new IllegalArgumentException(mimeType + ": override " +
                                    extension + " must be listed first.");
                        }
                        extension = extension.substring(0, extension.length() - 1);

                        // Overriding MIME definition wins
                        mimeTypeToExtension.put(mimeType, extension);
                    } else {
                        // First MIME definition wins
                        if (!mimeTypeToExtension.containsKey(mimeType)) {
                            mimeTypeToExtension.put(mimeType, extension);
                        }
                    }

                    // Last extension definition wins
                    extensionToMimeType.put(extension, mimeType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse " + resource, e);
        }
    }

    @Override
    protected String guessExtensionFromLowerCaseMimeType(String mimeType) {
        return mimeTypeToExtension.get(mimeType);
    }

    @Override
    protected String guessMimeTypeFromLowerCaseExtension(String extension) {
        return extensionToMimeType.get(extension);
    }
}
