/*
 * Copyright (C) 2015 The Android Open Source Project
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
package libcore.tzdata.update2.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Properties;
import libcore.tzdata.update2.TimeZoneBundle;

/**
 * A command-line tool for creating a timezone update bundle.
 *
 * Args:
 * tzdata.properties file - the file describing the bundle (see template file in tzdata/tools)
 * output file - the name of the file to be generated
 */
public class CreateTimeZoneBundle {

    private CreateTimeZoneBundle() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }
        File f = new File(args[0]);
        if (!f.exists()) {
            System.err.println("Properties file " + f + " not found");
            printUsage();
            System.exit(2);
        }
        Properties p = loadProperties(f);
        TimeZoneBundleBuilder builder = new TimeZoneBundleBuilder()
                .setRulesVersion(getMandatoryProperty(p, "rules.version"))
                .setAndroidRevision(getMandatoryProperty(p, "android.revision"))
                .setTzData(getMandatoryPropertyFile(p, "bionic.file"))
                .setIcuData(getMandatoryPropertyFile(p, "icu.file"));

        TimeZoneBundle bundle = builder.build();
        File outputFile = new File(args[1]);
        try (OutputStream os = new FileOutputStream(outputFile)) {
            os.write(bundle.getBytes());
        }
        System.out.println("Wrote: " + outputFile);
    }

    private static File getMandatoryPropertyFile(Properties p, String propertyName) {
        String fileName = getMandatoryProperty(p, propertyName);
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println(
                    "Missing file: " + file + " for property " + propertyName + " does not exist.");
            printUsage();
            System.exit(4);
        }
        return file;
    }

    private static String getMandatoryProperty(Properties p, String propertyName) {
        String value = p.getProperty(propertyName);
        if (value == null) {
            System.out.println("Missing property: " + propertyName);
            printUsage();
            System.exit(3);
        }
        return value;
    }

    private static Properties loadProperties(File f) throws IOException {
        Properties p = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(f))) {
            p.load(reader);
        }
        return p;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("\t" + CreateTimeZoneBundle.class.getName() +
                " <tzupdate.properties file> <output file>");
    }
}
