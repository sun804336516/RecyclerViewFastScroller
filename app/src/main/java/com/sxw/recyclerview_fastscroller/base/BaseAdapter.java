package com.sxw.recyclerview_fastscroller.base;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * 作者：孙贤武 on 2016/12/6 09:46
 * 邮箱：sun91985415@163.com
 * LIKE:The best ChaoMei
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<MyViewHolder> {
    protected Activity mActivity = null;
    protected List<T> mList = null;

    public BaseAdapter(Activity mActivity, List<T> list) {
        this.mActivity = mActivity;
        mList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mActivity).inflate(getLayoutId(viewType), parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Convert(holder, holder.getHandle(), position);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Convert(holder, holder.getHandle(), position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        int size = mList.size();
        return size;
    }

    protected abstract int getLayoutId(int viewType);

    protected abstract void Convert(MyViewHolder holder, HolderHandle holderHandle, int position);

    protected void Convert(MyViewHolder holder, HolderHandle holderHandle, int position, List<Object> payloads) {

    }

}
