package com.github.jasonwangdev.filepicker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
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


    public void showPicker(Fragment fragment) {
        PackageManager pm = fragment.getContext().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MIME_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

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

        // 單選
        Uri uri = data.getData();
        if (null != uri)
        {
            Log.d("TAG", uri.toString());
            Log.d("TAG", uri.getPath().toString());

            if (null != onFilePickerListener)
                onFilePickerListener.onFileChoose(new File(uri.toString()));

            return;
        }

        // 多選
        ClipData clipData = data.getClipData();
        if (null != clipData)
        {
            List<Uri> uris = new ArrayList<>(clipData.getItemCount());
            for (int i = 0 ; i < clipData.getItemCount() ; i++)
            {
                Log.d("TAG", clipData.getItemAt(i).getUri().toString());
                Log.d("TAG", clipData.getItemAt(i).getUri().getPath().toString());
                uris.add(clipData.getItemAt(i).getUri());
            }

            return;
        }
    }

}
