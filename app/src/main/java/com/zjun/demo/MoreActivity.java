package com.zjun.demo;

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * MoreActivity
 *
 * @author Ralap
 * @version v1
 * @description
 *
 * @date 2018-01-15
 */
public class MoreActivity extends AppCompatActivity {

    private FrameLayout fl_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        fl_container = findViewById(R.id.fl_container);
        changeView(R.layout.layout_more_1);
    }

    public void onClick(View view) {
        int layoutId;
        switch (view.getId()) {
            case R.id.btn_1:
            default:
                layoutId = R.layout.layout_more_1;
                break;
            case R.id.btn_2:
                layoutId = R.layout.layout_more_2;
                break;
            case R.id.btn_3:
                layoutId = R.layout.layout_more_3;
                break;
            case R.id.btn_4:
                layoutId = R.layout.layout_more_4;
                break;
            case R.id.btn_5:
                layoutId = R.layout.layout_more_5;
                break;
            case R.id.btn_6:
                layoutId = R.layout.layout_more_6;
                break;
        }
        changeView(layoutId);
    }

    private void changeView(@LayoutRes int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(layoutId, null, true);
        fl_container.removeAllViews();
        fl_container.addView(view);
    }
}
