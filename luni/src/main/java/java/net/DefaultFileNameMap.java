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

package java.net;

import libcore.net.MimeMap;

/**
 * Implements {@link FileNameMap} in terms of {@link libcore.net.MimeMap}.
 */
class DefaultFileNameMap implements FileNameMap {
    public String getContentTypeFor(String filename) {
        String ext = extensionOf(filename);
        return MimeMap.getDefault().guessMimeTypeFromExtension(ext);
    }

    private static String extensionOf(String filename) {
        int fragmentIndex = filename.indexOf('#');
        if (fragmentIndex >= 0) {
            filename = filename.substring(0, fragmentIndex);
        }
        if (filename.endsWith("/")) { // a directory
            return "html";
        }
        int slashIndex = filename.lastIndexOf('/');
        if (slashIndex >= 0) {
            filename = filename.substring(slashIndex);
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1) : "";
    }
}
