package com.example.myheartportal.room_ui;

import android.app.Application;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myheartportal.PointValueEncTEMP;
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

import java.util.ArrayList;

class TabTempViewModel extends AndroidViewModel {

    //***FIREBASE
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    //***LIVEDATA
    private MutableLiveData<String> patientId, latestTEMPKey;
    private MutableLiveData<String> highTEMP, highDateTime;
    private MutableLiveData<LineData> lineDataLive = new MutableLiveData<>(new LineData());
    //***VARIABLES
    private LineDataSet lineDataSet1 = new LineDataSet(null, null);
    private ArrayList<ILineDataSet> iLineDataSets = new ArrayList<ILineDataSet>();
    private ArrayList<Entry> tempDataVals;
    private ArrayList<String> xTime;
    private String[] roomInfo; //user_type, doctor_id, room_code, room_name, history_file
    private final String tag = this.getClass().getSimpleName();
    private int indexTEMP;
    //***Secure Encryption
    SecureEncryption secureEncryption = new SecureEncryption();

    public TabTempViewModel(@NonNull Application application) {
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
        if (latestTEMPKey == null)
        {
            latestTEMPKey = new MutableLiveData<>("");
        }

        if (highTEMP == null)
        {
            highTEMP = new MutableLiveData<>("");
        }
        if (highDateTime == null)
        {
            highDateTime = new MutableLiveData<>("");
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

    void findLatestTempKey (String patient_id)
    {
        final Query lastECGQuery = mDatabase.child("Readings").child(patient_id).child("TEMP").orderByKey().limitToLast(1);
        lastECGQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String file_name = dataSnapshot.getKey();
                latestTEMPKey.setValue(file_name);
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
                lastECGQuery.removeEventListener(this);
            }
        });
    }

    MutableLiveData<String> getLatestTEMPKey()
    {
        return this.latestTEMPKey;
    }

    ArrayList<Entry> getTempDataVals()
    {
        return this.tempDataVals;
    }

    ArrayList<String> getxTime()
    {
        return this.xTime;
    }

    MutableLiveData<String> getHighTEMP() {
        return this.highTEMP;
    }

    MutableLiveData<String> getHighDateTime()
    {
        return this.highDateTime;
    }

    MutableLiveData<LineData> getLineDataLive()
    {
        return this.lineDataLive;
    }

    void readTEMPData(final String patient_id, final String latestTEMPKey)
    {
        if (tempDataVals == null)
        {
            tempDataVals = new ArrayList<>();
        } else {
            tempDataVals.clear();
        }
        if (xTime == null)
        {
            xTime = new ArrayList<>();
        } else {
            xTime.clear();
        }

        indexTEMP = 0;

        mDatabase.child("Readings").child(patient_id).child("TEMP").child(latestTEMPKey)
        .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {

                String key = (String) dataSnapshot.getKey();

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "tempdata"))
                {
                    dataSnapshot.getRef().addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            PointValueEncTEMP pointValueEncTEMP = dataSnapshot.getValue(PointValueEncTEMP.class);
                            assert pointValueEncTEMP != null;

                            float decTemperature = 0;
                            String decTime = null;

                            try {
                                decTemperature = Float.parseFloat(secureEncryption.decryptData(pointValueEncTEMP.getEncryptedTemperature(), patient_id));
                                decTime = secureEncryption.decryptData(pointValueEncTEMP.getxTime(), patient_id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            tempDataVals.add(new Entry((float) indexTEMP, (float) decTemperature));
                            xTime.add(indexTEMP, decTime);

                            indexTEMP++;

                            lineDataSet1.setValues(tempDataVals);
                            lineDataSet1.setLabel("Temperature Monitor");
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

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "high_temp"))
                {
                    String hTEMP = "", hDateTime = "";

                    if (dataSnapshot.hasChild("temp"))
                    {
                        try {
                            hTEMP = secureEncryption.decryptData(dataSnapshot.child("temp").getValue().toString(),
                                    patient_id) + " Celsius";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dataSnapshot.hasChild("time"))
                    {
                        try {
                            hDateTime = secureEncryption.decryptData(dataSnapshot.child("time").getValue().toString(), patient_id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    highTEMP.setValue(hTEMP);
                    highDateTime.setValue(hDateTime);

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String key = dataSnapshot.getKey();

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "high_temp"))
                {
                    String hTEMP = "", hDateTime = "";

                    if (dataSnapshot.hasChild("temp"))
                    {
                        try {
                            hTEMP = secureEncryption.decryptData(dataSnapshot.child("temp").getValue().toString(),
                                    patient_id) + " Celsius";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dataSnapshot.hasChild("time"))
                    {
                        try {
                            hDateTime = secureEncryption.decryptData(dataSnapshot.child("time").getValue().toString(), patient_id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    highTEMP.setValue(hTEMP);
                    highDateTime.setValue(hDateTime);
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
                mDatabase.child("Readings").child(patient_id).child("TEMP").child(latestTEMPKey)
                        .removeEventListener(this);
            }
        });
    }
}
