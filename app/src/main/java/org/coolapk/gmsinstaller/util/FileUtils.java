package org.coolapk.gmsinstaller.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

/**
 * Created by xifan on 15-4-9.
 */
public class FileUtils {
    public static final int REQUEST_CODE = 1;
    private static final String DOCUMENT_STORAGE_AUTHORITY = "com.android.externalstorage.documents";
    private static final String DOWNLOADS_AUTHORITY = "com.android.providers.downloads.documents";
    private static final String MEDIA_AUTHORITY = "com.android.providers.media.documents";
    private static final String GOOGLE_AUTHORITY = "com.google.android.apps.photos.content";

    public static Intent createGetZipIntent() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static String getPath(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return getDocumentPath(context, uri);
        } else {
            return getFilePath(uri);
        }
    }

    @TargetApi(19)
    public static String getDocumentPath(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (DOCUMENT_STORAGE_AUTHORITY.equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + File.separator + split[1];
                }
            } else if (DOWNLOADS_AUTHORITY.equalsIgnoreCase(uri.getAuthority())) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (MEDIA_AUTHORITY.equalsIgnoreCase(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (GOOGLE_AUTHORITY.equalsIgnoreCase(uri.getAuthority())) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        } else {
            return getFilePath(uri);
        }

        Log.e("", "get unknown uri: " + uri.getPath());
        return null;
    }

    public static String getFilePath(Uri uri) {
        String path = uri.getPath();
        if ("file".equalsIgnoreCase(uri.getScheme()) || path.startsWith("/storage") || path.startsWith("/sdcard")) {
            return path;
        }
        Log.e("", "get unknown uri: " + uri.getPath());
        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
