package com.sxw.recyclerview_fastscroller.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MyViewHolder extends RecyclerView.ViewHolder {
    private HolderHandle mHandle;
    private View itemView;

    public MyViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        mHandle = HolderHandle.getInstace(itemView);
    }

    public HolderHandle getHandle() {
        return mHandle;
    }
}
