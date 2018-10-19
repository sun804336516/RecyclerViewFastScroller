package com.sxw.recyclerview_fastscroller;

import android.app.Activity;

import com.sxw.recyclerview_fastscroller.base.BaseAdapter;
import com.sxw.recyclerview_fastscroller.base.HolderHandle;
import com.sxw.recyclerview_fastscroller.base.MyViewHolder;

import java.util.List;

/**
 * Administrator on 2018/10/19/019 10:43
 */
public class SimpleAdapter extends BaseAdapter<String> {
    public SimpleAdapter(Activity mActivity, List<String> list) {
        super(mActivity, list);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.item_layout;
    }

    @Override
    protected void Convert(MyViewHolder holder, HolderHandle holderHandle, int position) {
        String s = mList.get(position);
        holderHandle.setTextViewText(R.id.TextvIew, s);
    }
}
