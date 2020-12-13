package com.example.myheartportal.room_ui;

import android.app.Application;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TabProfileViewModel extends AndroidViewModel {

    private final String tag = this.getClass().getSimpleName();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private MutableLiveData<String> patName, patInfo;
    private MutableLiveData<String> docName, docInfo;
    private String doctor_id, room_code;
    private Application tabProfile;

    public TabProfileViewModel(@NonNull Application application) {
        super(application);

        this.tabProfile = application;
    }

    void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }

    public void setRoom_code(String room_code) {
        this.room_code = room_code;
    }

    void init()
    {
        if (docName == null && docInfo == null)
        {
            this.docName = new MutableLiveData<>("Doctor's Name");
            this.docInfo = new MutableLiveData<>("Doc's Info");

            if (!TextUtils.isEmpty(doctor_id) && !TextUtils.isEmpty(room_code))
            {
                getDoctorInfo(doctor_id);
            }
        }

        if (patName == null && patInfo == null)
        {
            this.patName = new MutableLiveData<>("Patient's Name");
            this.patInfo = new MutableLiveData<>("Patient's Info");

            if (!TextUtils.isEmpty(doctor_id) && !TextUtils.isEmpty(room_code))
            {
                getPatientInfo(doctor_id, room_code);
            }
        }
    }

    MutableLiveData<String> getDocInfo()
    {
        return this.docInfo;
    }

    MutableLiveData<String> getDocName()
    {
        return this.docName;
    }

    MutableLiveData<String> getPatName()
    {
        return this.patName;
    }

    MutableLiveData<String> getPatInfo()
    {
        return this.patInfo;
    }

    private void getDoctorInfo(final String doctor_id)
    {
        mDatabase.child("Users").child(doctor_id).child("doctor_details")
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String fName = "", mName = "", lName = "";
                if (dataSnapshot.hasChild("last_name"))
                {
                    lName = (String) dataSnapshot.child("last_name").getValue();
                }
                if (dataSnapshot.hasChild("first_name"))
                {
                    fName = (String) dataSnapshot.child("first_name").getValue();
                }
                if (dataSnapshot.hasChild("middle_name"))
                {
                    mName = (String) dataSnapshot.child("middle_name").getValue();
                }
                String fullName = lName + ", " + fName + " " + mName;
                docName.setValue(fullName);

                String hospital = "", ownPhone = "", mail = "";
                if (dataSnapshot.hasChild("hospital"))
                {
                    hospital = (String) dataSnapshot.child("hospital").getValue();
                }
                if (dataSnapshot.hasChild("own_phone"))
                {
                    ownPhone = (String) dataSnapshot.child("own_phone").getValue();
                }
                if (dataSnapshot.hasChild("mail"))
                {
                    mail = (String) dataSnapshot.child("mail").getValue();
                }
                String docsInfo = hospital + "\n" + ownPhone + "\n" + mail;
                docInfo.setValue(docsInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Users").child(doctor_id).child("doctor_details").removeEventListener(this);
            }
        });
    }

    private void getPatientInfo(final String doctor_id, final String room_code)
    {
        mDatabase.child("Rooms").child(doctor_id).child(room_code)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("patient_id"))
                {
                    final String patient_id = (String) dataSnapshot.child("patient_id").getValue();
                    if (!TextUtils.isEmpty(patient_id))
                    {
                        mDatabase.child("Users").child(patient_id).child("patient_details")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String fName = "", mName = "", lName = "";
                                if (dataSnapshot.hasChild("last_name"))
                                {
                                    lName = (String) dataSnapshot.child("last_name").getValue();
                                }
                                if (dataSnapshot.hasChild("first_name"))
                                {
                                    fName = (String) dataSnapshot.child("first_name").getValue();
                                }
                                if (dataSnapshot.hasChild("middle_name"))
                                {
                                    mName = (String) dataSnapshot.child("middle_name").getValue();
                                }
                                String fullName = lName + ", " + fName + " " + mName;
                                patName.setValue(fullName);

                                String dateOfBirth = "", ownPhone = "", mail = "";
                                if (dataSnapshot.hasChild("date_of_birth"))
                                {
                                    dateOfBirth = (String) dataSnapshot.child("date_of_birth").getValue();
                                }
                                if (dataSnapshot.hasChild("own_phone"))
                                {
                                    ownPhone = (String) dataSnapshot.child("own_phone").getValue();
                                }
                                if (dataSnapshot.hasChild("mail"))
                                {
                                    mail = (String) dataSnapshot.child("mail").getValue();
                                }
                                String patsInfo = dateOfBirth + "\n" + ownPhone + "\n" + mail;

                                String gender = "", height = "", weight = "";
                                if (dataSnapshot.hasChild("gender"))
                                {
                                    gender = (String) dataSnapshot.child("gender").getValue();
                                    if (!TextUtils.equals(gender, "Not specified "))
                                    {
                                        patsInfo = patsInfo.concat("\n" + gender);
                                    }
                                }
                                if (dataSnapshot.hasChild("height"))
                                {
                                    height = (String) dataSnapshot.child("height").getValue();
                                    if (!TextUtils.equals(height, "Not specified "))
                                    {
                                        patsInfo = patsInfo.concat("\n" + height);
                                    }
                                }
                                if (dataSnapshot.hasChild("weight"))
                                {
                                    weight = (String) dataSnapshot.child("weight").getValue();
                                    if (!TextUtils.equals(weight, "Not specified "))
                                    {
                                        patsInfo = patsInfo.concat(" and " + weight);
                                    }
                                }

                                patInfo.setValue(patsInfo);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                mDatabase.child("Users").child(patient_id).child("patient_details").removeEventListener(this);
                            }
                        });
                    } else {
                        Toast.makeText(tabProfile, "Empty ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    patName.setValue("No patient inside.");
                    patInfo.setValue("No informations");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Rooms").child(doctor_id).child(room_code).removeEventListener(this);
            }
        });
    }
}
