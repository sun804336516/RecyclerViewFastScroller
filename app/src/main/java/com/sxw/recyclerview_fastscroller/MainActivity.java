package com.sxw.recyclerview_fastscroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.noPopTextvIew:
                startActivity(new Intent(this, FastScrollerActivity.class));
                break;
            case R.id.withPopTextvIew:
                startActivity(new Intent(this, FastScrollerActivity2.class));
                break;
            case R.id.GridwithPopTextvIew:
                startActivity(new Intent(this, FastScrollerActivity3.class));
                break;
        }
    }
}
