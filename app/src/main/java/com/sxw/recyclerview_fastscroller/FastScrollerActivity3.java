package com.sxw.recyclerview_fastscroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sxw.recyclerview_fastscroller.fast_scroller.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FastScrollerActivity3 extends AppCompatActivity {

    @BindView(R.id.fastScrollRcv)
    FastScrollRecyclerView mFastScrollRcv;
    private List<String> mList = new ArrayList<>();
    private SimpleAdapter2 mSimpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_scroller);
        ButterKnife.bind(this);
        
        mFastScrollRcv.setVerticalGridLayoutManager(3);
        mSimpleAdapter = new SimpleAdapter2(this, mList);
        mFastScrollRcv.setAdapter(mSimpleAdapter);

        for (int i = 0; i < 200; i++) {
            mList.add(DataString.get() + "-" + i);
        }
        mSimpleAdapter.notifyDataSetChanged();

    }
}
