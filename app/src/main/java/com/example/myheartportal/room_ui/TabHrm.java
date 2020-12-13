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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.ArrayList;

import static com.example.myheartportal.Constants.ROOM_INFO;

public class TabHrm extends Fragment {

    //***LAYOUTS
    private LineChart linechart_hrm;
    private TextView tvHighBPM, tvDateTime, tvConfidence, tvActivity;
    //***LIFECYCLE
    private TabHrmViewModel tabHrmViewModel;
    //***VARIABLES
    private final String tag = "TabHrm";
    private String [] room_info;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View mHRMView = inflater.inflate(R.layout.tab_hrm, container, false);

        linechart_hrm = mHRMView.findViewById(R.id.linechart_hrm);
        tvHighBPM = mHRMView.findViewById(R.id.tvHighBPM);
        tvDateTime = mHRMView.findViewById(R.id.tvDateTime);
        tvConfidence = mHRMView.findViewById(R.id.tvConfidence);
        tvActivity = mHRMView.findViewById(R.id.tvActivity);

        tabHrmViewModel = new ViewModelProvider(this).get(TabHrmViewModel.class);

        if (getArguments() != null)
        {
            room_info = getArguments().getStringArray(ROOM_INFO);
        } else {
            room_info = new String[4];
        }

        tabHrmViewModel.setRoomInfo(room_info);
        tabHrmViewModel.initHRM();

        tabHrmViewModel.getPatientId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String patient_id) {

                if (!TextUtils.isEmpty(patient_id))
                {
                    if (!TextUtils.isEmpty(room_info[4]) && room_info[4].toLowerCase().startsWith("hrm"))
                    {
                        tabHrmViewModel.readHRMData(patient_id, room_info[4]);

                        Description description = new Description();
                        description.setText(room_info[4]);
                        description.setTextSize(10);
                        description.setTextColor(Color.BLUE);
                        linechart_hrm.setDescription(description);

                    } else {
                        tabHrmViewModel.findLatestHrmKey(patient_id);
                    }
                }
            }
        });
        tabHrmViewModel.getLatestHRMKey().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String latestHRMKey) {

                String patient_id = tabHrmViewModel.getPatientId().getValue();

                if (!TextUtils.isEmpty(patient_id) && !TextUtils.isEmpty(latestHRMKey))
                {
                    tabHrmViewModel.readHRMData(patient_id, latestHRMKey);

                    Description description = new Description();
                    description.setText(latestHRMKey);
                    description.setTextSize(10);
                    description.setTextColor(Color.BLUE);
                    linechart_hrm.setDescription(description);
                }

            }
        });
        tabHrmViewModel.getHighBPM().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String highbpm) {
                tvHighBPM.setText(highbpm);
            }
        });
        tabHrmViewModel.getHighDateTime().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String datetime) {
                tvDateTime.setText(datetime);
            }
        });
        tabHrmViewModel.getHrconfidence().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String confidence) {
                tvConfidence.setText(confidence);
            }
        });
        tabHrmViewModel.getHractivity().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String activity) {
                tvActivity.setText(activity);
            }
        });

        tabHrmViewModel.getLineDataLive().observe(getViewLifecycleOwner(), new Observer<LineData>() {
            @Override
            public void onChanged(LineData lineData) {

                if (lineData != null)
                {
                    linechart_hrm.clear();

                    XAxis xAxis = linechart_hrm.getXAxis();
                    YAxis yAxisleft = linechart_hrm.getAxisLeft();
                    YAxis yAxisRight = linechart_hrm.getAxisRight();
                    yAxisRight.setEnabled(false);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                    IMarker marker = new YourMarkerView(mHRMView.getContext(), R.layout.textview_hrm);

                    linechart_hrm.setMarker(marker);
                    linechart_hrm.setData(lineData);
                    if (tabHrmViewModel.getHeartRateVals() != null && tabHrmViewModel.getHeartRateVals().size() > 0)
                    {
                        xAxis.setAxisMinimum(tabHrmViewModel.getHeartRateVals().get(0).getX());
                        linechart_hrm.setVisibleXRange(tabHrmViewModel.getHeartRateVals().get(0).getX(),
                                (float) tabHrmViewModel.getHeartRateVals().size()/2);
                    }
//                    linechart_hrm.setVisibleXRange(0, 180);
                    linechart_hrm.notifyDataSetChanged();
                }
            }
        });

        linechart_hrm.setNoDataText("No HRM Data");
        linechart_hrm.setNoDataTextColor(Color.RED);
        linechart_hrm.clear();
        linechart_hrm.invalidate();

        return mHRMView;
    }

    public class YourMarkerView extends MarkerView
    {
        private TextView tvHRMx, tvConfx, tvActx;

        public YourMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvHRMx = findViewById(R.id.tvHRMx);
            tvConfx = findViewById(R.id.tvConfx);
            tvActx = findViewById(R.id.tvActx);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            String markHRM = e.getY() + " bpm";
            String markConf = tabHrmViewModel.getxConfidence().get((int) e.getX()) + "%";
            String markAct = tabHrmViewModel.getxActivity().get((int) e.getX());

            tvHRMx.setText(markHRM);
            tvConfx.setText(markConf);
            tvActx.setText(markAct);

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
}
