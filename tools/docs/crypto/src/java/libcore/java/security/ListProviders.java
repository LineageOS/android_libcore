/*
 * Copyright (C) 2017 The Android Open Source Project
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

package libcore.java.security;

import java.security.Provider;
import java.security.Security;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Prints a list of all algorithms provided by security providers.  Intended to be run
 * via vogar as part of the algorithm documentation update process.
 * <p>
 * {@code vogar libcore/tools/src/java/libcore/java/security/ListProviders.java}
 */
public class ListProviders {
    public static void main(String[] argv) {
        System.out.println("BEGIN ALGORITHM LIST");
        for (Provider p : Security.getProviders()) {
            Set<Provider.Service> services = new TreeSet<Provider.Service>(
                    new Comparator<Provider.Service>() {
                        public int compare(Provider.Service a, Provider.Service b) {
                            int typeCompare = a.getType().compareTo(b.getType());
                            if (typeCompare != 0) {
                                return typeCompare;
                            }
                            return a.getAlgorithm().compareTo(b.getAlgorithm());
                        }
                    });
            services.addAll(p.getServices());
            for (Provider.Service s : services) {
                System.out.println(s.getType() + " " + s.getAlgorithm());
            }
        }
        System.out.println("END ALGORITHM LIST");
    }
}