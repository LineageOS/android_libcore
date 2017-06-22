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
package libcore.tzdata.prototype_updater;

import android.app.timezone.Callback;
import android.app.timezone.DistroFormatVersion;
import android.app.timezone.DistroRulesVersion;
import android.app.timezone.RulesManager;
import android.app.timezone.RulesState;
import android.app.timezone.RulesUpdaterContract;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.provider.TimeZoneRulesDataContract;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import libcore.io.Streams;

// TODO(nfuller): Prevent multiple broadcasts being handled at once?
// TODO(nfuller): Improve logging
// TODO(nfuller): Make the rules check async?
// TODO(nfuller): Need async generally for SystemService calls from BroadcastReceiver?
public class RulesCheckReceiver extends BroadcastReceiver {
    final static String TAG = "RulesCheckReceiver";

    private RulesManager mRulesManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!RulesUpdaterContract.ACTION_TRIGGER_RULES_UPDATE_CHECK.equals(intent.getAction())) {
            // Unknown. Do nothing.
            Log.w(TAG, "Unrecognized intent action received: " + intent
                    + ", action=" + intent.getAction());
            return;
        }

        mRulesManager = (RulesManager) context.getSystemService("timezone");

        byte[] token = intent.getByteArrayExtra(RulesUpdaterContract.EXTRA_CHECK_TOKEN);

        // Note: We rely on the system server to check that the configured data application is the
        // one that exposes the content provider with the well-known authority, and is a privileged
        // application as required. It is *not* checked here and it is assumed the updater can trust
        // the data application.

        // Obtain the information about what the data app is telling us to do.
        String operation = getOperation(context);
        if (operation == null) {
            // TODO Log
            boolean success = true; // No point in retrying.
            handleCheckComplete(token, success);
            return;
        }
        switch (operation) {
            case TimeZoneRulesDataContract.OPERATION_NO_OP:
                // TODO Log
                // No-op. Just acknowledge the check.
                handleCheckComplete(token, true /* success */);
                break;
            case TimeZoneRulesDataContract.OPERATION_UNINSTALL:
                // TODO Log
                handleUninstall(token);
                break;
            case TimeZoneRulesDataContract.OPERATION_INSTALL:
                // TODO Log
                DistroVersionInfo distroVersionInfo = getDistroVersionInfo(context);
                handleCopyAndInstall(context, token, distroVersionInfo);
                break;
            default:
                // TODO Log
                final boolean success = true; // No point in retrying.
                handleCheckComplete(token, success);
        }
    }

    private String getOperation(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(TimeZoneRulesDataContract.OPERATION_URI,
                        new String[] { TimeZoneRulesDataContract.COLUMN_OPERATION },
                        null /* selection */, null /* selectionArgs */, null /* sortOrder */);
        if (cursor == null) {
            Log.e(TAG, "getOperation: query returned null");
            return null;
        }
        if (!cursor.moveToFirst()) {
            Log.e(TAG, "getOperation: query returned empty results");
            return null;
        }

        try {
            return cursor.getString(0);
        } catch (Exception e) {
            Log.e(TAG, "getOperation: getString() threw an exception", e);
            return null;
        }
    }

    private DistroVersionInfo getDistroVersionInfo(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(TimeZoneRulesDataContract.OPERATION_URI,
                        new String[] {
                                TimeZoneRulesDataContract.COLUMN_DISTRO_MAJOR_VERSION,
                                TimeZoneRulesDataContract.COLUMN_DISTRO_MINOR_VERSION,
                                TimeZoneRulesDataContract.COLUMN_RULES_VERSION,
                                TimeZoneRulesDataContract.COLUMN_REVISION},
                        null /* selection */, null /* selectionArgs */, null /* sortOrder */);
        if (cursor == null) {
            Log.e(TAG, "getDistroVersionInfo: query returned null");
            return null;
        }
        if (!cursor.moveToFirst()) {
            Log.e(TAG, "getDistroVersionInfo: query returned empty results");
            return null;
        }

        try {
            return new DistroVersionInfo(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getInt(3));
        } catch (Exception e) {
            Log.e(TAG, "getDistroVersionInfo: getInt()/getString() threw an exception", e);
            return null;
        }
    }

    private void handleCopyAndInstall(Context context, byte[] checkToken,
            DistroVersionInfo distroVersionInfo) {

        // Decide whether to proceed with the install.
        RulesState rulesState = mRulesManager.getRulesState();
        if (!rulesState.isDistroFormatVersionSupported(distroVersionInfo.mDistroFormatVersion)
            || rulesState.isSystemVersionNewerThan(distroVersionInfo.mDistroRulesVersion)) {
            // Nothing to do.
            handleCheckComplete(checkToken, true /* success */);
            return;
        }

        // Copy the data locally before passing it on....security and whatnot.
        // TODO(nfuller): Need to do the copy here?
        File file = copyDataToLocalFile(context);
        if (file == null) {
            // It's possible this may get better if the problem is related to storage space.
            boolean success = false;
            handleCheckComplete(checkToken, success);
            return;
        }
        handleInstall(checkToken, file);
    }

    private static File copyDataToLocalFile(Context context) {
        File extractedFile = new File(context.getFilesDir(), "temp.zip");
        ParcelFileDescriptor fileDescriptor;
        try {
            fileDescriptor = context.getContentResolver().openFileDescriptor(
                    TimeZoneRulesDataContract.DATA_URI, "r");
            if (fileDescriptor == null) {
                throw new FileNotFoundException("ContentProvider returned null");
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyDataToLocalFile: Unable to open file descriptor"
                    + TimeZoneRulesDataContract.DATA_URI, e);
            return null;
        }

        try (ParcelFileDescriptor pfd = fileDescriptor;
             InputStream fis = new FileInputStream(pfd.getFileDescriptor());
             FileOutputStream fos = new FileOutputStream(extractedFile, false /* append */)) {
            Streams.copy(fis, fos);
        } catch (IOException e) {
            Log.e(TAG, "Unable to create asset storage file: " + extractedFile, e);
            return null;
        }
        return extractedFile;
    }

    private void handleInstall(final byte[] checkToken, final File contentFile) {
        // Convert the distroFile to a ParcelFileDescriptor.
        final ParcelFileDescriptor distroFileDescriptor;
        try {
            distroFileDescriptor =
                    ParcelFileDescriptor.open(contentFile, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to create ParcelFileDescriptor from " + contentFile);
            handleCheckComplete(checkToken, false /* success */);
            return;
        }

        Callback callback = new Callback() {
            @Override
            public void onFinished(int status) {
                Log.i(TAG, "onFinished: Finished install: " + status);

                // TODO(nfuller): Can this be closed sooner?
                try {
                    distroFileDescriptor.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to close ParcelFileDescriptor for " + contentFile, e);
                } finally {
                    // Delete the file we no longer need.
                    contentFile.delete();
                }
            }
        };

        try {
            int requestStatus =
                    mRulesManager.requestInstall(distroFileDescriptor, checkToken, callback);
            Log.i(TAG, "handleInstall: Request sent:" + requestStatus);
        } catch (Exception e) {
            Log.e(TAG, "handleInstall: Error", e);
        }
    }

    private void handleUninstall(byte[] checkToken) {
        Callback callback = new Callback() {
            @Override
            public void onFinished(int status) {
                Log.i(TAG, "onFinished: Finished uninstall: " + status);
            }
        };

        try {
            int requestStatus =
                    mRulesManager.requestUninstall(checkToken, callback);
            Log.i(TAG, "handleUninstall: Request sent" + requestStatus);
        } catch (Exception e) {
            Log.e(TAG, "handleUninstall: Error", e);
        }
    }

    private void handleCheckComplete(final byte[] token, final boolean success) {
        try {
            mRulesManager.requestNothing(token, success);
            Log.i(TAG, "doInBackground: Called checkComplete: token="
                    + Arrays.toString(token) + ", success=" + success);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: Error calling checkComplete()", e);
        }
    }

    private static class DistroVersionInfo {

        final DistroFormatVersion mDistroFormatVersion;
        final DistroRulesVersion mDistroRulesVersion;

        DistroVersionInfo(int distroMajorVersion, int distroMinorVersion,
                String rulesVersion, int revision) {
            mDistroFormatVersion = new DistroFormatVersion(distroMajorVersion, distroMinorVersion);
            mDistroRulesVersion = new DistroRulesVersion(rulesVersion, revision);
        }
    }
}
