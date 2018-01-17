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
        changeView(R.layout.layout_more_gradual_color);
    }

    public void onClick(View view) {
        int layoutId;
        switch (view.getId()) {
            case R.id.btn_gradual_color:
            default:
                layoutId = R.layout.layout_more_gradual_color;
                break;
            case R.id.btn_quick_cut:
                // can cut the section quickly, when you touch it
                layoutId = R.layout.layout_more_quick_cut;
                break;
            case R.id.btn_different_color:
                // different color for start and end anchor, with the right gravity
                layoutId = R.layout.layout_more_diffent_color;
                break;
            case R.id.btn_no_numbers:
                layoutId = R.layout.layout_more_no_numbers;
                break;
            case R.id.btn_chinese:
                /*
                 Set chinese to the text of anchor,
                 and anchors will merge when the start minute is 0, and the end minute is 60
                  */
                layoutId = R.layout.layout_more_chinese;
                break;
            case R.id.btn_multi_section:
                // Set the sum of sections(default value is 3), so you can add plenty of sections
                layoutId = R.layout.layout_more_multi_section;
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
