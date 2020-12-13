package com.example.myheartportal.room_ui;

import android.app.Application;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myheartportal.PointValueEncECG;
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

class TabEcgViewModel extends AndroidViewModel {

    //***FIREBASE
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    //***LIVEDATA
    private MutableLiveData<String> patientId, latestECGKey;
    private MutableLiveData<String> highRR, highDateTime, aveRR, currRR;
    private MutableLiveData<LineData> lineDataLive = new MutableLiveData<>(new LineData());
    //***VARIABLES
    private LineDataSet lineDataSet1 = new LineDataSet(null, null);
    private LineDataSet lineDataSet2 = new LineDataSet(null, null);
    private ArrayList<ILineDataSet> iLineDataSets = new ArrayList<ILineDataSet>();
    private ArrayList<Entry> rawDataVals, filterDataVals;
    private ArrayList<String> xTime;
    private String[] roomInfo; //user_type, doctor_id, room_code, room_name, history_file
    private final String tag = this.getClass().getSimpleName();
    private boolean cbFilter, cbRaw;
    private int indexInECG;
    //***SecureEncryption
    SecureEncryption secureEncryption = new SecureEncryption();

    public TabEcgViewModel(@NonNull Application application) {
        super(application);

        this.cbFilter = true;
        this.cbRaw = true;
    }


    void setRoomInfo(String[] roomInfo)
    {
        this.roomInfo = roomInfo;
    }

    ArrayList<Entry> getRawDataVals() {
        return this.rawDataVals;
    }

    ArrayList<Entry> getFilterDataVals() {
        return this.filterDataVals;
    }

    void initEcg()
    {
        if (patientId == null)
        {
            patientId = new MutableLiveData<>("");
            getPatientID();
        }
        if (latestECGKey == null)
        {
            latestECGKey = new MutableLiveData<>("");
        }

        if (highRR == null)
        {
            highRR = new MutableLiveData<>("");
        }
        if (highDateTime == null)
        {
            highDateTime = new MutableLiveData<>("");
        }
        if (aveRR == null)
        {
            aveRR = new MutableLiveData<>("");
        }
        if (currRR == null)
        {
            currRR = new MutableLiveData<>("");
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

    void findLatestECGKey (String patient_id)
    {
        final Query lastECGQuery = mDatabase.child("Readings").child(patient_id).child("ECG").orderByKey().limitToLast(1);
        lastECGQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String file_name = dataSnapshot.getKey();
                latestECGKey.setValue(file_name);
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

    MutableLiveData<String> getLatestECGKey()
    {
        return this.latestECGKey;
    }


    ArrayList<String> getxTime()
    {
        return this.xTime;
    }

    MutableLiveData<String> getAveRR()
    {
        return this.aveRR;
    }

    MutableLiveData<String> getCurrRR()
    {
        return this.currRR;
    }

    MutableLiveData<String> getHighRR()
    {
        return this.highRR;
    }

    MutableLiveData<String> getHighDateTime()
    {
        return this.highDateTime;
    }

    void setCbRaw (boolean cbRawIsChecked)
    {
        this.cbRaw = cbRawIsChecked;
    }

    void setCbFilter (boolean cbFilterIsChecked)
    {
        this.cbFilter = cbFilterIsChecked;
    }

    MutableLiveData<LineData> getLineDataLive()
    {
        return this.lineDataLive;
    }


    void readECGData(final String patient_id, final String latestECGKey)
    {
        if (rawDataVals == null)
        {
            rawDataVals = new ArrayList<>();
        } else {
            rawDataVals.clear();
        }
        if (filterDataVals == null)
        {
            filterDataVals = new ArrayList<>();
        } else {
            filterDataVals.clear();
        }
        if (xTime == null)
        {
            xTime = new ArrayList<>();
        } else {
            xTime.clear();
        }

        indexInECG = 0;

        mDatabase.child("Readings").child(patient_id).child("ECG").child(latestECGKey)
        .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {

                String key = (String) dataSnapshot.getKey();

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "ecgdata"))
                {
                    dataSnapshot.getRef().addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            PointValueEncECG pointValueEncECG = dataSnapshot.getValue(PointValueEncECG.class);

                            assert pointValueEncECG != null;

                            float decRaw = 0, decFiltered = 0;
                            String decTime = null, decAverage = null, decCurrent = null;
                            try {
                                decRaw = Float.parseFloat(secureEncryption.decryptData(pointValueEncECG.getEncryptedRaw(), patient_id));
                                decFiltered = Float.parseFloat(secureEncryption.decryptData(pointValueEncECG.getEncryptedFilter(), patient_id));
                                decTime = secureEncryption.decryptData(pointValueEncECG.getxTime(), patient_id);
                                decAverage = secureEncryption.decryptData(pointValueEncECG.getEncryptedAve(), patient_id);
                                decCurrent = secureEncryption.decryptData(pointValueEncECG.getEncryptedCurrent(), patient_id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            rawDataVals.add(new Entry((float) indexInECG, (float) decRaw));
                            filterDataVals.add(new Entry((float) indexInECG, (float) decFiltered));
                            xTime.add(indexInECG, decTime);

                            indexInECG++;

                            aveRR.setValue(decAverage);
                            currRR.setValue(decCurrent);

                            lineDataSet1.setValues(rawDataVals);
                            lineDataSet1.setLabel("Raw ECG");
                            lineDataSet1.setLineWidth(2);
                            lineDataSet1.setColor(Color.RED);
                            lineDataSet1.setDrawCircles(false);
                            lineDataSet1.setValueTextSize(10);
                            lineDataSet1.setDrawValues(false);

                            lineDataSet2.setValues(filterDataVals);
                            lineDataSet2.setLabel("Filtered ECG");
                            lineDataSet2.setLineWidth(2);
                            lineDataSet2.setColor(Color.BLUE);
                            lineDataSet2.setDrawCircles(false);
                            lineDataSet2.setValueTextSize(10);
                            lineDataSet2.setDrawValues(false);

                            if (iLineDataSets.size() < 2)
                            {
                                iLineDataSets.add(0, lineDataSet1);
                                iLineDataSets.add(1, lineDataSet2);
                            } else {
                                iLineDataSets.set(0, lineDataSet1);
                                iLineDataSets.set(1, lineDataSet2);
                            }

                            LineData lineData = new LineData(iLineDataSets);
                            if (cbRaw)
                            {
                                lineData.getDataSetByIndex(0).setVisible(true);
                            } else {
                                lineData.getDataSetByIndex(0).setVisible(false);
                            }
                            if (cbFilter)
                            {
                                lineData.getDataSetByIndex(1).setVisible(true);
                            } else {
                                lineData.getDataSetByIndex(1).setVisible(false);
                            }

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

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "high_rr"))
                {
                    String hRR = "", hDateTime = "";
                    if (dataSnapshot.hasChild("rr"))
                    {
                        try {
                            hRR = secureEncryption.decryptData(dataSnapshot.child("rr").getValue().toString(), patient_id);
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
                    highRR.setValue(hRR);
                    highDateTime.setValue(hDateTime);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String key = dataSnapshot.getKey();

                if (!TextUtils.isEmpty(key) && TextUtils.equals(key, "high_rr"))
                {
                    String hRR = "", hDateTime = "";
                    if (dataSnapshot.hasChild("rr"))
                    {
                        try {
                            hRR = secureEncryption.decryptData(dataSnapshot.child("rr").getValue().toString(), patient_id);
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
                    highRR.setValue(hRR);
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
                mDatabase.child("Readings").child(patient_id).child("ECG").child(latestECGKey)
                        .removeEventListener(this);
            }
        });
    }
}
