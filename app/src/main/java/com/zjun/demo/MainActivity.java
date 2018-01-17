package com.zjun.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zjun.widget.RingTimeView;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 *
 * @author Ralap
 * @version v1
 * @description
 *
 * @date 2018-01-15
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private LinearLayout ll_parent;
    private RingTimeView rtv_time;
    private TextView tv_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll_parent = findViewById(R.id.ll_parent);
        rtv_time = findViewById(R.id.rtv_time);
        tv_time = findViewById(R.id.tv_time);

        initView();
        initData();
    }

    private void initView() {
        rtv_time.setOnTimeChangeListener(new RingTimeView.IOnTimeChangedListener() {
            @Override
            public void onChanged(RingTimeView view, List<RingTimeView.TimePart> timePartList) {
                Log.d(TAG, "onChanged: ");
                updateTime(timePartList);
            }

            @Override
            public void onInsert(RingTimeView.TimePart part) {
                Log.d(TAG, String.format("onInsert: %d ~ %d", part.getStart(), part.getEnd()));
            }

            @Override
            public void onSelectStart(int minute) {
                Log.d(TAG, "onSelectStart: " + minute);
            }

            @Override
            public void onSelectChanged(int minute) {
                Log.d(TAG, "onSelectChanged: " + minute);
            }

            @Override
            public void onSelectFinished() {
                Log.d(TAG, "onSelectFinished: ");
            }
        });
    }

    private void initData() {
        updateTime(rtv_time.getTimeSections());
    }

    private void updateTime(List<RingTimeView.TimePart> timePartList) {
        if (timePartList == null || timePartList.isEmpty()) {
            tv_time.setText("No TimePart");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (RingTimeView.TimePart part : timePartList) {
            sb.append(",  ")
                    .append("08:").append(fillZero(part.getStart()))
                    .append("-08:").append(fillZero(part.getEnd()));
        }
        if (sb.length() > 0) {
            sb.delete(0, 2);
        }
        sb.insert(0, "Time: ");
        tv_time.setText(sb.toString());
    }

    private String fillZero(int num) {
        return num < 10 ? "0" + num : String.valueOf(num);
    }





    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_set:
                List<RingTimeView.TimePart> partList = new ArrayList<>();
                partList.add(new RingTimeView.TimePart(10, 20));
                partList.add(new RingTimeView.TimePart(40, 50));
                rtv_time.setTimeSections(partList);
                break;
            case R.id.btn_clear:
                rtv_time.clearTimeSections();
                break;
            case R.id.btn_other_style:
                startActivity(new Intent(this, MoreActivity.class));
            default: break;
        }
    }


}
