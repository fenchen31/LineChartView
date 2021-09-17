package com.practice.linechartview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LineChartView lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineChart = findViewById(R.id.lc_view);
        setData();
    }

    private void setData(){
        ArrayList<DataBean> data = new ArrayList<>();
        for (int i = 0; i < 5; i ++){
            DataBean bean = new DataBean();
            bean.setDate("202" + i + ".0" + i + ".0" + i);
            bean.setPrice("1" + i + ".1" + i);
            bean.setxCut(i);
            if (i == 3){
                bean.setyCut(2);
            } else if (i == 4){
                bean.setyCut(3);
            } else {
                bean.setyCut(i);
            }
            data.add(bean);
        }
        lineChart.setData(data, 66);
    }
}