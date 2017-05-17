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

package libcore.tzdata.prototypedata;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.TimeZoneRulesDataContract;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import libcore.io.Streams;

import static android.content.res.AssetManager.ACCESS_STREAMING;
import static android.provider.TimeZoneRulesDataContract.COLUMN_DISTRO_MAJOR_VERSION;
import static android.provider.TimeZoneRulesDataContract.COLUMN_DISTRO_MINOR_VERSION;
import static android.provider.TimeZoneRulesDataContract.COLUMN_OPERATION;
import static android.provider.TimeZoneRulesDataContract.COLUMN_REVISION;
import static android.provider.TimeZoneRulesDataContract.COLUMN_RULES_VERSION;
import static android.provider.TimeZoneRulesDataContract.OPERATION_INSTALL;

/**
 * A basic implementation of a time zone data provider that can be used by OEMs to implement
 * an APK asset-based solution for time zone updates.
 */
public final class TimeZoneRulesDataProvider extends ContentProvider {

    static final String TAG = "TimeZoneRulesDataProvider";

    private static final String METADATA_KEY_OPERATION = "android.timezoneprovider.OPERATION";
    private static final String METADATA_KEY_ASSET = "android.timezoneprovider.DATA_ASSET";
    private static final String METADATA_KEY_DISTRO_MAJOR_VERSION
            = "android.timezoneprovider.DISTRO_MAJOR_VERSION";
    private static final String METADATA_KEY_DISTRO_MINOR_VERSION
            = "android.timezoneprovider.DISTRO_MINOR_VERSION";
    private static final String METADATA_KEY_RULES_VERSION
            = "android.timezoneprovider.RULES_VERSION";
    private static final String METADATA_KEY_REVISION
            = "android.timezoneprovider.REVISION";

    private static final Set<String> KNOWN_COLUMN_NAMES;
    private static final Map<String, Class<?>> KNOWN_COLUMN_TYPES;
    static {
        Set<String> columnNames = new HashSet<>();
        columnNames.add(COLUMN_OPERATION);
        columnNames.add(COLUMN_DISTRO_MAJOR_VERSION);
        columnNames.add(COLUMN_DISTRO_MINOR_VERSION);
        columnNames.add(COLUMN_RULES_VERSION);
        columnNames.add(COLUMN_REVISION);
        KNOWN_COLUMN_NAMES = Collections.unmodifiableSet(columnNames);

        Map<String, Class<?>> columnTypes = new HashMap<>();
        columnTypes.put(COLUMN_OPERATION, String.class);
        columnTypes.put(COLUMN_DISTRO_MAJOR_VERSION, Integer.class);
        columnTypes.put(COLUMN_DISTRO_MINOR_VERSION, Integer.class);
        columnTypes.put(COLUMN_RULES_VERSION, String.class);
        columnTypes.put(COLUMN_REVISION, Integer.class);
        KNOWN_COLUMN_TYPES = Collections.unmodifiableMap(columnTypes);
    }

    private Map<String, Object> mColumnData = new HashMap<>();
    private String mAssetName;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);

        // Sanity check our security
        if (!TimeZoneRulesDataContract.AUTHORITY.equals(info.authority)) {
            // The authority looked for by the time zone updater is fixed.
            throw new SecurityException(
                    "android:authorities must be \"" + TimeZoneRulesDataContract.AUTHORITY + "\"");
        }
        if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grant uri permissions");
        }
        if (!info.exported) {
            // The content provider is accessed directly so must be exported.
            throw new SecurityException("android:exported must be \"true\"");
        }
        if (info.pathPermissions != null || info.writePermission != null) {
            // Use readPermission only to implement permissions.
            throw new SecurityException("Use android:readPermission only");
        }
        if (!android.Manifest.permission.UPDATE_TIME_ZONE_RULES.equals(info.readPermission)) {
            // Writing is not supported.
            throw new SecurityException("android:readPermission must be set to \""
                    + android.Manifest.permission.UPDATE_TIME_ZONE_RULES
                    + "\" is: " + info.readPermission);
        }

        // info.metadata is not filled in by default. Must ask for it again.
        final ProviderInfo infoWithMetadata = context.getPackageManager()
                .resolveContentProvider(info.authority, PackageManager.GET_META_DATA);
        Bundle metaData = infoWithMetadata.metaData;
        if (metaData == null) {
            throw new SecurityException("meta-data must be set");
        }

        String operation;
        try {
            operation = getMandatoryMetaDataString(metaData, METADATA_KEY_OPERATION);
            mColumnData.put(COLUMN_OPERATION, operation);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(METADATA_KEY_OPERATION + " meta-data not set.");
        }
        if (OPERATION_INSTALL.equals(operation)) {
            mColumnData.put(
                    COLUMN_DISTRO_MAJOR_VERSION,
                    getMandatoryMetaDataInt(metaData, METADATA_KEY_DISTRO_MAJOR_VERSION));
            mColumnData.put(
                    COLUMN_DISTRO_MINOR_VERSION,
                    getMandatoryMetaDataInt(metaData, METADATA_KEY_DISTRO_MINOR_VERSION));
            mColumnData.put(
                    COLUMN_RULES_VERSION,
                    getMandatoryMetaDataString(metaData, METADATA_KEY_RULES_VERSION));
            mColumnData.put(
                    COLUMN_REVISION,
                    getMandatoryMetaDataInt(metaData, METADATA_KEY_REVISION));

            // Make sure the asset containing the data to install exists.
            String assetName = getMandatoryMetaDataString(metaData, METADATA_KEY_ASSET);
            try {
                InputStream is = context.getAssets().open(assetName);
                // An exception is thrown if the asset does not exist. list(assetName) appears not
                // to work with file paths.
                is.close();
            } catch (IOException e) {
                throw new SecurityException("Unable to open asset:" + assetName);
            }
            mAssetName = assetName;
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (!TimeZoneRulesDataContract.OPERATION_URI.equals(uri)) {
            return null;
        }
        final List<String> projectionList = Arrays.asList(projection);
        if (projection != null && !KNOWN_COLUMN_NAMES.containsAll(projectionList)) {
            throw new UnsupportedOperationException(
                    "Only " + KNOWN_COLUMN_NAMES + " columns supported.");
        }

        return new AbstractCursor() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public String[] getColumnNames() {
                return projectionList.toArray(new String[0]);
            }

            @Override
            public int getType(int column) {
                String columnName = projectionList.get(column);
                Class<?> columnJavaType = KNOWN_COLUMN_TYPES.get(columnName);
                if (columnJavaType == String.class) {
                    return Cursor.FIELD_TYPE_STRING;
                } else if (columnJavaType == Integer.class) {
                    return Cursor.FIELD_TYPE_INTEGER;
                } else {
                    throw new UnsupportedOperationException(
                            "Unsupported type: " + columnJavaType + " for " + columnName);
                }
            }

            @Override
            public String getString(int column) {
                checkPosition();
                String columnName = projectionList.get(column);
                if (KNOWN_COLUMN_TYPES.get(columnName) != String.class) {
                    throw new UnsupportedOperationException();
                }
                return (String) mColumnData.get(columnName);
            }

            @Override
            public short getShort(int column) {
                checkPosition();
                throw new UnsupportedOperationException();
            }

            @Override
            public int getInt(int column) {
                checkPosition();
                String columnName = projectionList.get(column);
                if (KNOWN_COLUMN_TYPES.get(columnName) != Integer.class) {
                    throw new UnsupportedOperationException();
                }
                return (Integer) mColumnData.get(columnName);
            }

            @Override
            public long getLong(int column) {
                return getInt(column);
            }

            @Override
            public float getFloat(int column) {
                throw new UnsupportedOperationException();
            }

            @Override
            public double getDouble(int column) {
                checkPosition();
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isNull(int column) {
                checkPosition();
                return column != 0;
            }
        };
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        if (!TimeZoneRulesDataContract.DATA_URI.equals(uri)) {
            return null;
        }
        if (mAssetName == null) {
            throw new FileNotFoundException();
        }
        if (!mode.equals("r")) {
            throw new SecurityException("Only read-only access supported.");
        }

        // Extract the asset to a local dir. We do it every time: we don't make assumptions that the
        // current copy (if any) is valid.
        File localFile = extractAssetToLocalFile();

        // Create a read-only ParcelFileDescriptor that can be passed to the caller process.
        try {
            return ParcelFileDescriptor.open(localFile, ParcelFileDescriptor.MODE_READ_ONLY,
                    new Handler(Looper.getMainLooper()),
                    e -> {
                        if (e != null) {
                            Log.w(TAG, "Error in OnCloseListener for " + localFile, e);
                        }
                        localFile.delete();
                    });
        } catch (IOException e) {
            throw new RuntimeException("Unable to open asset file", e);
        }
    }

    private File extractAssetToLocalFile() throws FileNotFoundException {
        File extractedFile = new File(getContext().getFilesDir(), "timezone_data.zip");
        InputStream is;
        try {
            is = getContext().getAssets().open(mAssetName, ACCESS_STREAMING);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            FileNotFoundException fnfe = new FileNotFoundException("Problem reading asset");
            fnfe.initCause(e);
            throw fnfe;
        }

        try (InputStream fis = is;
                FileOutputStream fos = new FileOutputStream(extractedFile, false /* append */)) {
            Streams.copy(fis, fos);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create asset storage file: " + extractedFile, e);
        }
        return extractedFile;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private static String getMandatoryMetaDataString(Bundle metaData, String key) {
        if (!metaData.containsKey(key)) {
            throw new SecurityException("No metadata with key " + key + " found.");
        }
        return metaData.getString(key);
    }

    private static int getMandatoryMetaDataInt(Bundle metaData, String key) {
        if (!metaData.containsKey(key)) {
            throw new SecurityException("No metadata with key " + key + " found.");
        }
        return metaData.getInt(key, -1);
    }
}
