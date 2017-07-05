package com.github.jasonwangdev.filepicker.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.jasonwangdev.filepicker.Error;
import com.github.jasonwangdev.filepicker.FilePicker;
import com.github.jasonwangdev.filepicker.OnFilePickerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2017/7/3.
 */

public class MainFragment extends Fragment implements View.OnClickListener, OnFilePickerListener {

    List<File> fileList;
    ThumbnailAdapter adapter;

    FilePicker filePicker;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileList = new ArrayList<>();
        adapter = new ThumbnailAdapter(fileList);

        filePicker = new FilePicker();
        filePicker.setOnFilePickerListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        view.findViewById(R.id.button_picker).setOnClickListener(this);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);

        ((RecyclerView) view.findViewById(R.id.lv)).setLayoutManager(llm);
        ((RecyclerView) view.findViewById(R.id.lv)).setAdapter(adapter);

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        filePicker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        filePicker.onActivityResult(this, requestCode, resultCode, data);
    }


    @Override
    public void onClick(View v) {
        filePicker.showMediaPicker(this);
    }

    @Override
    public void onFilePickerError(Error error) {
        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileChoose(List<File> files) {
        fileList.clear();
        fileList.addAll(files);

        adapter.notifyDataSetChanged();
    }

}
