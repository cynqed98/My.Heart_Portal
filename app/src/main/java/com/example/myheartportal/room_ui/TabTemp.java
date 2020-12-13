package com.example.myheartportal.room_ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.myheartportal.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import static com.example.myheartportal.Constants.ROOM_INFO;

import java.util.ArrayList;

public class TabTemp extends Fragment {

    //***LAYOUTS
    private TextView tvHighTEMP, tvDateTime;
    private LineChart linechart_temp;
    //***VARIABLES
    private final String tag = "TabTemp";
    private String [] room_info;
    //***LIFECYCLE
    private TabTempViewModel tabTempViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View mTEMPView = inflater.inflate(R.layout.tab_temp, container, false);

        linechart_temp = mTEMPView.findViewById(R.id.linechart_temp);
        tvHighTEMP = mTEMPView.findViewById(R.id.tvHighTEMP);
        tvDateTime = mTEMPView.findViewById(R.id.tvDateTime);

        tabTempViewModel = new ViewModelProvider(this).get(TabTempViewModel.class);

        if (getArguments() != null)
        {
            room_info = getArguments().getStringArray(ROOM_INFO);
        } else {
            room_info = new String[4];
        }

        tabTempViewModel.setRoomInfo(room_info);
        tabTempViewModel.initHRM();

        tabTempViewModel.getPatientId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String patient_id) {

                if (!TextUtils.isEmpty(patient_id))
                {
                    if (!TextUtils.isEmpty(room_info[4]) && room_info[4].toLowerCase().startsWith("temp"))
                    {
                        tabTempViewModel.readTEMPData(patient_id, room_info[4]);

                        Description description = new Description();
                        description.setText(room_info[4]);
                        description.setTextSize(10);
                        description.setTextColor(Color.BLUE);
                        linechart_temp.setDescription(description);

                    } else {
                        tabTempViewModel.findLatestTempKey(patient_id);
                    }
                }
            }
        });
        tabTempViewModel.getLatestTEMPKey().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String latestTEMPKey) {

                String patient_id = tabTempViewModel.getPatientId().getValue();

                if (!TextUtils.isEmpty(patient_id) && !TextUtils.isEmpty(latestTEMPKey))
                {
                    tabTempViewModel.readTEMPData(patient_id, latestTEMPKey);

                    Description description = new Description();
                    description.setText(latestTEMPKey);
                    description.setTextSize(10);
                    description.setTextColor(Color.BLUE);
                    linechart_temp.setDescription(description);
                }

            }
        });
        tabTempViewModel.getHighTEMP().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String hightemp) {
                tvHighTEMP.setText(hightemp);
            }
        });
        tabTempViewModel.getHighDateTime().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String datetime) {
                tvDateTime.setText(datetime);
            }
        });


        tabTempViewModel.getLineDataLive().observe(getViewLifecycleOwner(), new Observer<LineData>() {
            @Override
            public void onChanged(LineData lineData) {

                if (lineData != null)
                {
                    linechart_temp.clear();

                    XAxis xAxis = linechart_temp.getXAxis();
                    YAxis yAxisleft = linechart_temp.getAxisLeft();
                    YAxis yAxisRight = linechart_temp.getAxisRight();
                    yAxisRight.setEnabled(false);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                    IMarker marker = new YourMarkerView(mTEMPView.getContext(), R.layout.textview_temp);

                    linechart_temp.setMarker(marker);
                    linechart_temp.setData(lineData);
                    if (tabTempViewModel.getTempDataVals() != null && tabTempViewModel.getTempDataVals().size() > 0)
                    {
                        xAxis.setAxisMinimum(tabTempViewModel.getTempDataVals().get(0).getX());
                        xAxis.setValueFormatter(new MyDataValuesFormatter(tabTempViewModel.getxTime()));
                        linechart_temp.setVisibleXRange(tabTempViewModel.getTempDataVals().get(0).getX(),
                                (float) tabTempViewModel.getTempDataVals().size()/2);
                    }
//                    linechart_temp.setVisibleXRange(0, 50);
                    linechart_temp.notifyDataSetChanged();
                }
            }
        });

        linechart_temp.setNoDataText("No TEMP Data");
        linechart_temp.setNoDataTextColor(Color.RED);
        linechart_temp.clear();
        linechart_temp.invalidate();

        return mTEMPView;
    }

    public class YourMarkerView extends MarkerView
    {
        private TextView tvTempx, tvDatex;

        public YourMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvTempx = findViewById(R.id.tvTempx);
            tvDatex = findViewById(R.id.tvDatex);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            String markTemp = e.getY() + " Celsius";
            String markdatetime = tabTempViewModel.getxTime().get((int) e.getX());

            tvTempx.setText(markTemp);
            tvDatex.setText(markdatetime);

            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {

            if (mOffset == null)
            {
                mOffset = new MPPointF(-(float) (getWidth() / 2), -getHeight());
            }
            return mOffset;
        }
    }

    private static class MyDataValuesFormatter extends ValueFormatter
    {
        ArrayList<String> xTime;

        MyDataValuesFormatter(ArrayList<String> xTime)
        {
            this.xTime = xTime;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            axis.setLabelCount(3, true);

            return super.getAxisLabel(value, axis);
        }

        @Override
        public String getFormattedValue(float value) {

            if (value < xTime.size() - 1)
            {
                return xTime.get((int) Math.floor(value) + 1);
            } else {
//                return xTime.get((int) Math.floor(value));
                return "max";
            }
        }
    }
}
