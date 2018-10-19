package com.sxw.recyclerview_fastscroller;

import android.app.Activity;

import com.sxw.recyclerview_fastscroller.base.BaseAdapter;
import com.sxw.recyclerview_fastscroller.base.HolderHandle;
import com.sxw.recyclerview_fastscroller.base.MyViewHolder;
import com.sxw.recyclerview_fastscroller.fast_scroller.SelectNameAdapter;

import java.util.List;

/**
 * Administrator on 2018/10/19/019 10:43
 */
public class SimpleAdapter2 extends BaseAdapter<String> implements SelectNameAdapter {
    public SimpleAdapter2(Activity mActivity, List<String> list) {
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

    @Override
    public String getSelectName(int position) {
        if (position > mList.size() - 1) {
            position = mList.size() - 1;
        }
        return mList.get(position);
    }
}
