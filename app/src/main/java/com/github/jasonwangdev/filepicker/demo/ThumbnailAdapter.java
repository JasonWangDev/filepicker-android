package com.github.jasonwangdev.filepicker.demo;

import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by jason on 2017/7/3.
 */

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {

    private List<File> files;


    public ThumbnailAdapter(List<File> files) {
        this.files = files;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumbnail, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File file = files.get(position);
        String fileType = URLConnection.guessContentTypeFromName(file.getPath());
        if (fileType.startsWith("image"))
        {
            holder.iv.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getPath()), 100, 100));
        }
        else if (fileType.startsWith("video"))
            holder.iv.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv;

        public ViewHolder(View itemView) {
            super(itemView);

            iv = (ImageView) itemView.findViewById(R.id.iv);
        }
    }

}
