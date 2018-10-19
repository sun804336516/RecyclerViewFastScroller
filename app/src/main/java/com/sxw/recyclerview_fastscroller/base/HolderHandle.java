package com.sxw.recyclerview_fastscroller.base;

import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 作者：孙贤武 on 2016/12/6 09:41
 * 邮箱：sun91985415@163.com
 * LIKE:The best ChaoMei
 */
public class HolderHandle
{
    private View convertView = null;
    private SparseArray<View> mSparseArray = null;

    private HolderHandle(View convertView)
    {
        this.convertView = convertView;
        convertView.setTag(this);
        mSparseArray = new SparseArray<>();
    }

    public static HolderHandle getInstace(View convertView)
    {
        HolderHandle holderHandle = (HolderHandle) convertView.getTag();
        if (holderHandle == null) {
            holderHandle = new HolderHandle(convertView);
        }
        return holderHandle;
    }

    public <T extends View> T getViewById(int resId)
    {
        View view = mSparseArray.get(resId);
        if (view == null) {
            view = convertView.findViewById(resId);
            mSparseArray.put(resId, view);
        }
        return (T) view;
    }

    public HolderHandle setTextViewText(int resId, String str)
    {
        TextView tv = getViewById(resId);
        tv.setText(str);
        return this;
    }
    public HolderHandle setButtonText(int resId, String str)
    {
        Button bt = getViewById(resId);
        bt.setText(str);
        return this;
    }
    public HolderHandle setImage(int resId, int DrawId)
    {
        ImageView img = getViewById(resId);
        img.setImageResource(DrawId);
        return this;
    }

    public View getConvertView()
    {
        return convertView;
    }
}
