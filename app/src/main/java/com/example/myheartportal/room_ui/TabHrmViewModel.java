package com.example.myheartportal.room_ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myheartportal.PointValueEncHRM;
import com.example.myheartportal.SecureEncryption;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class TabHrmViewModel extends AndroidViewModel {

    //***FIREBASE
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    //***LIVEDATA
    private MutableLiveData<String> patientId, latestHRMKey;
    private MutableLiveData<String> highBPM, highDateTime, hrconfidence, hractivity;
    private MutableLiveData<LineData> lineDataLive = new MutableLiveData<>(new LineData());
    //***VARIABLES
    private LineDataSet lineDataSet1 = new LineDataSet(null, null);
    private ArrayList<ILineDataSet> iLineDataSets = new ArrayList<ILineDataSet>();
    private ArrayList<Entry> heartRateVals;
    private ArrayList<Double> xConfidence;
    private ArrayList<String> xActivity;
    private String[] roomInfo; //user_type, doctor_id, room_code, room_name, history_file
    private final String tag = this.getClass().getSimpleName();
    private int indexHRM;
    //***Secure Encryption
    SecureEncryption secureEncryption = new SecureEncryption();

    public TabHrmViewModel(@NonNull Application application) {
        super(application);
    }

    void setRoomInfo(String[] roomInfo)
    {
        this.roomInfo = roomInfo;
    }

    void initHRM()
    {
        if (patientId == null)
        {
            patientId = new MutableLiveData<>("");
            getPatientID();
        }
        if (latestHRMKey == null)
        {
            latestHRMKey = new MutableLiveData<>("");
        }

        if (highBPM == null)
        {
            highBPM = new MutableLiveData<>("");
        }
        if (highDateTime == null)
        {
            highDateTime = new MutableLiveData<>("");
        }
        if (hrconfidence == null)
        {
            hrconfidence = new MutableLiveData<>("");
        }
        if (hractivity == null)
        {
            hractivity = new MutableLiveData<>("");
        }
    }

    private void getPatientID()
    {
        mDatabase.child("Rooms").child(roomInfo[1]).child(roomInfo[2])
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("patient_id"))
                {
                    String patient_id = (String) dataSnapshot.child("patient_id").getValue();
                    patientId.setValue(patient_id);
                }
                else {
                    patientId.setValue("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Rooms").child(roomInfo[1]).child(roomInfo[2]).removeEventListener(this);
            }
        });
    }

    MutableLiveData<String> getPatientId()
    {
        return this.patientId;
    }

    void findLatestHrmKey (String patient_id)
    {
        final Query lastECGQuery = mDatabase.child("Readings").child(patient_id).child("HRM").orderByKey().limitToLast(1);
        lastECGQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String file_name = dataSnapshot.getKey();
                latestHRMKey.setValue(file_name);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    MutableLiveData<String> getLatestHRMKey()
    {
        return this.latestHRMKey;
    }

    ArrayList<Entry> getHeartRateVals()
    {
        return this.heartRateVals;
    }

    ArrayList<String> getxActivity()
    {
        return this.xActivity;
    }

    ArrayList<Double> getxConfidence()
    {
        return this.xConfidence;
    }

    MutableLiveData<String> getHighBPM() {
        return this.highBPM;
    }

    MutableLiveData<String> getHrconfidence() {
        return this.hrconfidence;
    }

    MutableLiveData<String> getHractivity() {
        return this.hractivity;
    }

    MutableLiveData<String> getHighDateTime()
    {
        return this.highDateTime;
    }

    MutableLiveData<LineData> getLineDataLive()
    {
        return this.lineDataLive;
    }

    void readHRMData(final String patient_id, final String latestHRMKey)
    {
        if (heartRateVals == null)
        {
            heartRateVals = new ArrayList<>();
        } else {
            heartRateVals.clear();
        }
        if (xConfidence == null)
        {
            xConfidence = new ArrayList<>();
        } else {
            xConfidence.clear();
        }
        if (xActivity == null)
        {
            xActivity = new ArrayList<>();
        } else {
            xActivity.clear();
        }

        indexHRM = 0;

        mDatabase.child("Readings").child(patient_id).child("HRM").child(latestHRMKey)
        .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {

                String key = (String) dataSnapshot.getKey();

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "hrmdata"))
                {
                    dataSnapshot.getRef().addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            PointValueEncHRM pointValueEncHRM = dataSnapshot.getValue(PointValueEncHRM.class);
                            assert pointValueEncHRM != null;

                            float decHeartRate = 0;
                            double decHRConfidence = 0;
                            String decActivity = null;

                            try {
                                decHeartRate = Float.parseFloat(secureEncryption.decryptData(pointValueEncHRM.getEncryptedHeartRate(), patient_id));
                                decHRConfidence = Double.parseDouble(secureEncryption.decryptData(pointValueEncHRM.getEncryptedHRConfidence(), patient_id));
                                decActivity = secureEncryption.decryptData(pointValueEncHRM.getEncryptedActivity(), patient_id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            heartRateVals.add(new Entry((float) indexHRM, (float) decHeartRate));
                            xConfidence.add(indexHRM, decHRConfidence);
                            xActivity.add(indexHRM, decActivity);

                            indexHRM++;

                            lineDataSet1.setValues(heartRateVals);
                            lineDataSet1.setLabel("Heart Rate Monitor");
                            lineDataSet1.setLineWidth(2);
                            lineDataSet1.setColor(Color.RED);
                            lineDataSet1.setDrawCircles(false);
                            lineDataSet1.setValueTextSize(10);
                            lineDataSet1.setDrawValues(false);

                            if (iLineDataSets.size() < 1)
                            {
                                iLineDataSets.add(0, lineDataSet1);
                            } else {
                                iLineDataSets.set(0, lineDataSet1);
                            }

                            LineData lineData = new LineData(iLineDataSets);
                            lineDataLive.setValue(lineData);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dataSnapshot.getRef().removeEventListener(this);
                        }
                    });
                }

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "high_bpm"))
                {
                    String hBPM = "", hDateTime = "", hConf = "", hAct = "";

                    if (dataSnapshot.hasChild("bpm"))
                    {
                        try {
                            hBPM = secureEncryption.decryptData(dataSnapshot.child("bpm").getValue().toString(),
                                    patient_id) + " bpm";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dataSnapshot.hasChild("time"))
                    {
                        long xtime = 0;
                        try {
                            xtime = Long.parseLong(secureEncryption.decryptData(dataSnapshot.child("time").getValue().toString(),
                                    patient_id));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                        hDateTime = format.format(new Date(xtime));
                    }
                    if (dataSnapshot.hasChild("confidence"))
                    {
                        try {
                            hConf = secureEncryption.decryptData(dataSnapshot.child("confidence").getValue().toString(),
                                    patient_id) + "%";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dataSnapshot.hasChild("activity"))
                    {
                        try {
                            hAct = secureEncryption.decryptData(dataSnapshot.child("activity").getValue().toString(),
                                    patient_id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    highBPM.setValue(hBPM);
                    highDateTime.setValue(hDateTime);
                    hrconfidence.setValue(hConf);
                    hractivity.setValue(hAct);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String key = dataSnapshot.getKey();

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "high_bpm"))
                {
                    String hBPM = "", hDateTime = "", hConf = "", hAct = "";

                    if (dataSnapshot.hasChild("bpm"))
                    {
                        try {
                            hBPM = secureEncryption.decryptData(dataSnapshot.child("bpm").getValue().toString(),
                                    patient_id) + " bpm";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dataSnapshot.hasChild("time"))
                    {
                        long xtime = 0;
                        try {
                            xtime = Long.parseLong(secureEncryption.decryptData(dataSnapshot.child("time").getValue().toString(),
                                    patient_id));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                        hDateTime = format.format(new Date(xtime));
                    }
                    if (dataSnapshot.hasChild("confidence"))
                    {
                        try {
                            hConf = secureEncryption.decryptData(dataSnapshot.child("confidence").getValue().toString(),
                                    patient_id) + "%";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dataSnapshot.hasChild("activity"))
                    {
                        try {
                            hAct = secureEncryption.decryptData(dataSnapshot.child("activity").getValue().toString(),
                                    patient_id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    highBPM.setValue(hBPM);
                    highDateTime.setValue(hDateTime);
                    hrconfidence.setValue(hConf);
                    hractivity.setValue(hAct);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Readings").child(patient_id).child("HRM").child(latestHRMKey)
                        .removeEventListener(this);
            }
        });
    }
}
