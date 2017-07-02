package com.github.jasonwangdev.filepicker.demo;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.jasonwangdev.filepicker.FilePicker;
import com.github.jasonwangdev.filepicker.OnFilePickerListener;

import java.io.File;

/**
 * Created by Jason on 2017/7/3.
 */

public class MainFragment extends Fragment implements View.OnClickListener, OnFilePickerListener {

    FilePicker filePicker;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filePicker = new FilePicker();
        filePicker.setOnFilePickerListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        view.findViewById(R.id.button_picker).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        filePicker.showPicker(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        filePicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFileChoose(File file) {
        Log.d("TAG", "onFileChoose");
        ((ImageView) getView().findViewById(R.id.iv)).setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
    }

}
