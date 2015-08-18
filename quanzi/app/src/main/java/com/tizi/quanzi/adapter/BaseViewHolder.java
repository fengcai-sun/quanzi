package com.tizi.quanzi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by qixingchen on 15/7/23.
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    //界面元素
    public NetworkImageView userFaceImageView, contantImageView;
    public TextView chatMessTextView, chatUserName, chatTime, voiceDuration;
    public ImageButton videoPlayButton;
    public ProgressBar audioProgressBar;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }
}