package com.github.jasonwangdev.filepicker;

import java.io.File;
import java.util.List;

/**
 * Created by Jason on 2017/7/3.
 */

public interface OnFilePickerListener {

    void onFilePickerError(Error error);
    void onFileChoose(List<File> files);

}
