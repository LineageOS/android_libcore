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

package libcore.heapdumper;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@code Instrumentation} that dumps the process heap, performs some action, and then dumps the
 * process heap again, all before creating and starting the target application, which finishes
 * straight away.
 *
 * <p>The instrumentation should be invoked with two arguments:
 * <ul>
 *     <li>one called {@code dumpdir} which gives the name of a directory to put the dumps in,
 *     relative to the public external storage directory;
 *     <li>one called {@code action} which gives the name of an {@link Actions} value to run between
 *     the two heap dumps.
 * </ul>
 *
 * <p>If there is a problem, it will try to create a file called {@code error} in the output
 * directory, containing a failure message.
 */
public class HeapDumpInstrumentation extends Instrumentation {

    private static final String TAG = "HeapDumpInstrumentation";

    private File mOutputDirectory;
    private Runnable mAction;

    @Override
    public void onCreate(Bundle icicle) {
        mOutputDirectory = resolveOutputDirectory(icicle);
        try {
            mAction = loadAction(icicle);
            dumpHeap("before.hprof");
            mAction.run();
            dumpHeap("after.hprof");
        } catch (Exception e) {
            recordException(e);
        }
        super.onCreate(icicle);
        start();
    }

    @Override
    public void onStart() {
        // Everything has been done in onCreate, onStart can just finish immediately.
        finish(Activity.RESULT_OK, new Bundle());
    }

    /**
     * Resolves the directory to use for output, based on the arguments in the bundle.
     */
    private static File resolveOutputDirectory(Bundle icicle) {
        String relativeDirectoryName = icicle.getString("dumpdir");
        if (relativeDirectoryName == null) {
            throw new IllegalArgumentException(
                    "Instrumentation invocation missing dumpdir argument");
        }
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            throw new IllegalStateException("External storage unavailable");
        }
        File dir = Environment.getExternalStoragePublicDirectory(relativeDirectoryName);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(
                    "Instrumentation invocation's dumpdir argument is not a directory: "
                            + dir.getAbsolutePath());
        }
        return dir;
    }

    /**
     * Returns the {@link Runnable} to run between heap dumps, based on the arguments in the bundle.
     */
    private static Runnable loadAction(Bundle icicle) {
        String name = icicle.getString("action");
        if (name == null) {
            throw new IllegalArgumentException(
                    "Instrumentation invocation missing action argument");
        }
        return Actions.valueOf(name);
    }

    /**
     * Dumps the process heap into a file with the given name relative to {@link #mOutputDirectory}.
     */
    private void dumpHeap(String relativeDumpFilename) throws IOException {
        File dumpFile = new File(mOutputDirectory, relativeDumpFilename);
        System.gc();
        System.runFinalization();
        System.gc();
        Debug.dumpHprofData(dumpFile.getCanonicalPath());
        Log.i(TAG, "Wrote to heap dump to " + dumpFile.getCanonicalPath());
    }

    /**
     * Write an {@code error} file into {@link #mOutputDirectory} containing the message of the
     * exception.
     */
    private void recordException(Exception e) {
        Log.e(TAG, "Exception while doing heap measurements", e);
        String contents = e.getMessage();
        File errorFile = new File(mOutputDirectory, "error");
        try {
            try (OutputStream errorStream = new FileOutputStream(errorFile)) {
                errorStream.write(contents.getBytes("UTF-8"));
            }
        } catch (IOException e2) {
            throw new RuntimeException("Exception writing error file!", e2);
        }
    }
}
