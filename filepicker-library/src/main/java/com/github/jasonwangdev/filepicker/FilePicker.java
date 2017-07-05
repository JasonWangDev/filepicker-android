package com.github.jasonwangdev.filepicker;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;

import com.github.jasonwangdev.filepicker.utils.PermissionResult;
import com.github.jasonwangdev.filepicker.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * 參考文獻:
 * 1. Android 如何選取圖片或是檔案？
 *     https://magiclen.org/android-filechooser/
 *
 * Created by Jason on 2017/7/3.
 */

public class FilePicker {

    private static final String STORAGE_PERMISSIONS = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final String ALL_MIME = "*/*";
    private static final String IMAGE_MIME = "image/*";
    private static final String VIDEO_MIME = "video/*";

    private static final int REQUEST_PICKER = 0xF0;

    private OnFilePickerListener onFilePickerListener;

    private Fragment fragment;


    public void showMediaPicker(Fragment fragment) {
        this.fragment = fragment;

        if (!PermissionUtils.checkPermission(fragment, STORAGE_PERMISSIONS))
            PermissionUtils.requestPermission(fragment, STORAGE_PERMISSIONS);
        else
            showMediaPicker();
    }

    public void setOnFilePickerListener(OnFilePickerListener onFilePickerListener) {
        this.onFilePickerListener = onFilePickerListener;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        List<PermissionResult> permissionResults = PermissionUtils.getPermissionResults(fragment, requestCode, permissions, grantResults);
        for(PermissionResult permissionResult : permissionResults)
        {
            if (!permissionResult.isGrant())
            {
                if (STORAGE_PERMISSIONS.equals(permissionResult.getPermission()))
                {
                    if (null != onFilePickerListener)
                        onFilePickerListener.onFilePickerError(permissionResult.isChoseNeverAskAgain() ? Error.STORAGE_PERMISSION_NEVER_DENIED : Error.STORAGE_PERMISSION_DENIED);

                    return;
                }
            }
        }

        showMediaPicker();
    }

    public void onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data) {
        this.fragment = fragment;

        if (REQUEST_PICKER != requestCode)
            return;

        if (Activity.RESULT_OK != resultCode)
            return;

        List<Uri> uris = getUris(data);
        List<File> files = getFiles(uris);

        if (null != onFilePickerListener)
            onFilePickerListener.onFileChoose(files);
    }


    private void showMediaPicker() {
        PackageManager pm = fragment.getContext().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            intent.setType(ALL_MIME);
            String[] mimeTypes = {IMAGE_MIME, VIDEO_MIME};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        else
            intent.setType(IMAGE_MIME + ", " + VIDEO_MIME);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos.size() > 0)
            fragment.startActivityForResult(intent, REQUEST_PICKER);
        else
        {
            if (null != onFilePickerListener)
                onFilePickerListener.onFilePickerError(Error.APP_NOT_SUPPORT);
        }
    }

    private List<Uri> getUris(Intent data) {
        // 單選
        Uri uri = data.getData();
        if (null != uri)
            return Collections.singletonList(uri);

        // 多選 (only for API 16 以上)
        ClipData clipData = data.getClipData();
        if (null != clipData)
        {
            List<Uri> uris = new ArrayList<>(clipData.getItemCount());
            for (int i = 0 ; i < clipData.getItemCount() ; i++)
                uris.add(clipData.getItemAt(i).getUri());

            return uris;
        }

        return new ArrayList<>();
    }

    private List<File> getFiles(List<Uri> uris) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return getFilesApiAbove19(uris);
        else
            return getFilesApiBelow19(uris);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private List<File> getFilesApiAbove19(List<Uri> uris) {
        List<File> files = new ArrayList<>();

        if (null != uris)
        {
            for (Uri uri : uris)
            {
                if (DocumentsContract.isDocumentUri(fragment.getContext(), uri))
                {
                    String path = null;
                    String authority = uri.getAuthority();
                    if ("com.android.externalstorage.documents".equals(authority))
                    {
                        String id = DocumentsContract.getDocumentId(uri);
                        String[] divide = id.split(":");
                        String type = divide[0];
                        if ("primary".equals(type))
                            path = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                        else
                            path = "/storage/".concat(type).concat("/").concat(divide[1]);
                    }
                    else if ("com.android.providers.downloads.documents".equals(authority))
                    {
                        String id = DocumentsContract.getDocumentId(uri);
                        Uri _uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                                             Long.parseLong(id));

                        path = getPath(_uri);
                    }
                    else if ("com.android.providers.media.documents".equals(authority))
                    {
                        String id = DocumentsContract.getDocumentId(uri);
                        String[] divide = id.split(":");
                        String type = divide[0];
                        Uri _uri = null;
                        if ("image".equals(type))
                            _uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        else if ("video".equals(type))
                            _uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        else if ("audio".equals(type))
                            _uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        _uri = ContentUris.withAppendedId(_uri, Long.parseLong(divide[1]));

                        path = getPath(_uri);
                    }

                    File file = createFile(path);
                    if (null != file)
                        files.add(file);
                }
            }
        }

        return files;
    }

    private List<File> getFilesApiBelow19(List<Uri> uris) {
        List<File> files = new ArrayList<>();

        if (null != uris)
        {
            for (Uri uri : uris)
            {
                String path = null;
                String scheme = uri.getScheme();
                if ("content".equals(scheme))
                    path = getPath(uri);
                else if ("file".equals(scheme))
                    path = uri.getPath();

                File file = createFile(path);
                if (null != file)
                    files.add(file);
            }
        }

        return files;
    }

    private String getPath(Uri uri) {
        String path = null;

        if (null != uri)
        {
            Cursor cursor = fragment.getContext().getContentResolver().query(uri,
                                                                             new String[]{MediaStore.MediaColumns.DATA},
                                                                             null,
                                                                             null,
                                                                             null);
            if (null != cursor && cursor.moveToFirst())
            {
                int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                path = cursor.getString(index);
            }

            if (null != cursor)
                cursor.close();
        }

        return path;
    }

    private File createFile(String path) {
        File file = null;

        if (null != path)
        {
            File _file = new File(path);
            _file.setReadable(true);

            if (!_file.canRead())
                return null;

            file = _file.getAbsoluteFile();
        }

        return file;
    }

}
