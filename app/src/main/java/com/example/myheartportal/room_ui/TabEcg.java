package com.example.myheartportal.room_ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.myheartportal.R;
import com.example.myheartportal.SecureEncryption;
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

import java.util.ArrayList;

import static com.example.myheartportal.Constants.ROOM_INFO;

public class TabEcg extends Fragment {

    //***LAYOUTS
    private LineChart linechart_ecg;
    private TextView tvAveRR, tvRR, tvHighRR, tvDateTime;
    private CheckBox cbRaw, cbFilter;
    //***LIFECYCLE
    private TabEcgViewModel tabEcgViewModel;
    //***VARIABLES
    private final String tag = "TabEcg";
    private String [] room_info;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View mECGView = inflater.inflate(R.layout.tab_ecg, container, false);

        linechart_ecg = mECGView.findViewById(R.id.linechart_ecg);
        tvAveRR = mECGView.findViewById(R.id.tvAveRR);
        tvRR = mECGView.findViewById(R.id.tvRR);
        tvHighRR = mECGView.findViewById(R.id.tvHighRR);
        tvDateTime = mECGView.findViewById(R.id.tvDateTime);
        cbRaw = mECGView.findViewById(R.id.cbRaw);
        cbFilter = mECGView.findViewById(R.id.cbFilter);

        tabEcgViewModel = new ViewModelProvider(this).get(TabEcgViewModel.class);

        if (getArguments() != null)
        {
            room_info = getArguments().getStringArray(ROOM_INFO);
        } else {
            room_info = new String[5];
        }

        tabEcgViewModel.setRoomInfo(room_info);
        tabEcgViewModel.initEcg();
        tabEcgViewModel.getPatientId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String patient_id) {

                if (!TextUtils.isEmpty(patient_id))
                {
                    if (!TextUtils.isEmpty(room_info[4]) && room_info[4].toLowerCase().startsWith("ecg"))
                    {
                        tabEcgViewModel.readECGData(patient_id, room_info[4]);

                        Description description = new Description();
                        description.setText(room_info[4]);
                        description.setTextSize(10);
                        description.setTextColor(Color.BLUE);
                        linechart_ecg.setDescription(description);

                    } else {
                        tabEcgViewModel.findLatestECGKey(patient_id);
                    }
                }
            }
        });

        tabEcgViewModel.getLatestECGKey().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String latestECGKey) {

                String patient_id = tabEcgViewModel.getPatientId().getValue();

                if (!TextUtils.isEmpty(patient_id) && !TextUtils.isEmpty(latestECGKey))
                {
                    tabEcgViewModel.readECGData(patient_id, latestECGKey);

                    Description description = new Description();
                    description.setText(latestECGKey);
                    description.setTextSize(10);
                    description.setTextColor(Color.BLUE);
                    linechart_ecg.setDescription(description);
                }
            }
        });

        tabEcgViewModel.getAveRR().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String aveRR) {
                tvAveRR.setText(aveRR);
            }
        });
        tabEcgViewModel.getCurrRR().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String currRR) {
                tvRR.setText(currRR);
            }
        });
        tabEcgViewModel.getHighRR().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String highRR) {
                tvHighRR.setText(highRR);
            }
        });
        tabEcgViewModel.getHighDateTime().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String highDateTime) {
                tvDateTime.setText(highDateTime);
            }
        });


        tabEcgViewModel.getLineDataLive().observe(getViewLifecycleOwner(), new Observer<LineData>() {
            @Override
            public void onChanged(LineData lineData) {

                if (lineData != null)
                {
                    linechart_ecg.clear();

                    XAxis xAxis = linechart_ecg.getXAxis();
                    YAxis yAxisleft = linechart_ecg.getAxisLeft();
                    YAxis yAxisRight = linechart_ecg.getAxisRight();
                    yAxisRight.setEnabled(false);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                    IMarker marker = new YourMarkerView(mECGView.getContext(), R.layout.textview_ecg);

                    linechart_ecg.setMarker(marker);
                    linechart_ecg.setData(lineData);
                    if (tabEcgViewModel.getRawDataVals() != null && tabEcgViewModel.getRawDataVals().size() > 0)
                    {
                        xAxis.setAxisMinimum(tabEcgViewModel.getRawDataVals().get(0).getX());
                        xAxis.setValueFormatter(new MyDataValuesFormatter(tabEcgViewModel.getxTime()));
                        linechart_ecg.setVisibleXRange(tabEcgViewModel.getRawDataVals().get(0).getX(),
                                (float) tabEcgViewModel.getRawDataVals().size()/2);
                        linechart_ecg.setVisibleYRange(-1, 1, YAxis.AxisDependency.LEFT);
                    }
//                    linechart_ecg.setVisibleXRange(0, 255);
                    linechart_ecg.notifyDataSetChanged();
                }
            }
        });

        cbRaw.setChecked(true);
        cbFilter.setChecked(true);

        cbRaw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!buttonView.isChecked())
                {
                    tabEcgViewModel.setCbRaw(false);

                    if (tabEcgViewModel.getLineDataLive().getValue() != null)
                    {
                        linechart_ecg.clear();
                        tabEcgViewModel.getLineDataLive().getValue().getDataSetByIndex(0).setVisible(false);
                        linechart_ecg.setData(tabEcgViewModel.getLineDataLive().getValue());
                        linechart_ecg.notifyDataSetChanged();
                    }
                } else {
                    tabEcgViewModel.setCbRaw(true);

                    if (tabEcgViewModel.getLineDataLive().getValue() != null)
                    {
                        linechart_ecg.clear();
                        tabEcgViewModel.getLineDataLive().getValue().getDataSetByIndex(0).setVisible(true);
                        linechart_ecg.setData(tabEcgViewModel.getLineDataLive().getValue());
                        linechart_ecg.notifyDataSetChanged();
                    }
                }
            }
        });
        cbFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!buttonView.isChecked()) {
                    tabEcgViewModel.setCbFilter(false);

                    if (tabEcgViewModel.getLineDataLive().getValue() != null)
                    {
                        linechart_ecg.clear();
                        tabEcgViewModel.getLineDataLive().getValue().getDataSetByIndex(1).setVisible(false);
                        linechart_ecg.setData(tabEcgViewModel.getLineDataLive().getValue());
                        linechart_ecg.notifyDataSetChanged();
                    }
                } else {
                    tabEcgViewModel.setCbFilter(true);

                    if (tabEcgViewModel.getLineDataLive().getValue() != null)
                    {
                        linechart_ecg.clear();
                        tabEcgViewModel.getLineDataLive().getValue().getDataSetByIndex(1).setVisible(true);
                        linechart_ecg.setData(tabEcgViewModel.getLineDataLive().getValue());

                    }
                }
            }
        });

        linechart_ecg.setNoDataText("No ECG Data");
        linechart_ecg.setNoDataTextColor(Color.RED);
        linechart_ecg.clear();
        linechart_ecg.invalidate();

        return mECGView;
    }

    public class YourMarkerView extends MarkerView
    {
        private TextView tvContent, tvContentY;

        public YourMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvContent = findViewById(R.id.tvContent);
            tvContentY = findViewById(R.id.tvContentY);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            String marker = "t: " + tabEcgViewModel.getxTime().get((int) e.getX());
            String markerY = "y: " + e.getY();
            tvContent.setText(marker);
            tvContentY.setText(markerY);

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
