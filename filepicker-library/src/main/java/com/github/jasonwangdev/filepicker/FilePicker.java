package com.github.jasonwangdev.filepicker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;

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

    private static final String MIME_TYPE = "*/*";

    private static final int REQUEST_PICKER = 0xF0;

    private OnFilePickerListener onFilePickerListener;

    private Fragment fragment;


    public void showPicker(Fragment fragment) {
        this.fragment = fragment;

        PackageManager pm = fragment.getContext().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MIME_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos.size() > 0)
            fragment.startActivityForResult(intent, REQUEST_PICKER);
        else
            Log.d("TAG", "NO APPs");
    }

    public void setOnFilePickerListener(OnFilePickerListener onFilePickerListener) {
        this.onFilePickerListener = onFilePickerListener;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_PICKER != requestCode)
            return;

        if (Activity.RESULT_OK != resultCode)
            return;

        List<Uri> uris = getUris(data);
        List<File> files = getFiles(uris);

        if (null != onFilePickerListener)
            onFilePickerListener.onFileChoose(files);
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

    private List<File> getFilesApiAbove19(List<Uri> uris) {
        List<File> files = new ArrayList<>();

        // TODO:

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
