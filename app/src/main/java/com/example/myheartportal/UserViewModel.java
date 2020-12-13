package com.example.myheartportal;

import android.app.Application;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserViewModel extends AndroidViewModel
{
    private String tag = this.getClass().getSimpleName();
    private Application userCheck;
    private String user_type;
    private MutableLiveData<Boolean> userIsRegistered;
    private MutableLiveData<Boolean> patientHasRoom;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public UserViewModel(@NonNull Application application)
    {
        super(application);
        this.userCheck = application;
    }

    public void setUser_type(String user_type, String defaultVal)
    {
        if (TextUtils.isEmpty(user_type))
        {
            this.user_type = defaultVal;
        }
        else {
            this.user_type = user_type;
        }
    }

    public void initCheckPatRoom()
    {
        if (patientHasRoom == null)
        {
            this.patientHasRoom = new MutableLiveData<>(false);
        }
    }
    public void setUseridForRoom(String user_id)
    {
        if (patientHasRoom != null)
        {
            new CheckIfPatientHasRoom(userCheck, mDatabase, patientHasRoom).execute(user_id);
        }
    }
    public MutableLiveData<Boolean> getPatientHasRoom()
    {
        return this.patientHasRoom;
    }

    public void initCheckIfUserReg()
    {
        if (userIsRegistered == null)
        {
            this.userIsRegistered = new MutableLiveData<>(false);
        }
    }
    public void setUserIdForReg(String user_id)
    {
        if (userIsRegistered != null)
        {
            if (TextUtils.equals(user_type, "patient"))
            {
                new CheckIfUserIsPatient(userCheck, mDatabase, userIsRegistered).execute(user_id);
            }
            else if (TextUtils.equals(user_type, "doctor"))
            {
                new CheckIfUserIsDoctor(userCheck, mDatabase, userIsRegistered).execute(user_id);
            }
            else if (TextUtils.equals(user_type, "guardian"))
            {
                new CheckIfUserIsGuardian(userCheck, mDatabase, userIsRegistered).execute(user_id);
            }
        }
    }
    public MutableLiveData<Boolean> getUserIsRegistered()
    {
        return this.userIsRegistered;
    }

    private static class CheckIfPatientHasRoom extends AsyncTask<String, Void, Boolean>
    {
        DatabaseReference mDatabase;
        MutableLiveData<Boolean> patientHasRoom;
        Application userCheck;

        CheckIfPatientHasRoom(Application userCheck, DatabaseReference mDatabase, MutableLiveData<Boolean> patientHasRoom)
        {
            this.userCheck = userCheck;
            this.mDatabase = mDatabase;
            this.patientHasRoom = patientHasRoom;
        }

        @Override
        protected Boolean doInBackground(final String... strings) {

            mDatabase.child("Users").child(strings[0]).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("rooms_patient"))
                    {
                        patientHasRoom.setValue(true);
                    } else {
                        patientHasRoom.setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabase.child("Users").child(strings[0]).removeEventListener(this);
                }
            });

            return null;
        }
    }

    private static class CheckIfUserIsDoctor extends AsyncTask<String, Void, Boolean>
    {
        DatabaseReference mDatabase;
        MutableLiveData<Boolean> userIsDoctor;
        Application userCheck;

        CheckIfUserIsDoctor(Application userCheck, DatabaseReference mDatabase, MutableLiveData<Boolean> userIsRegistered) {
            this.mDatabase = mDatabase;
            this.userIsDoctor = userIsRegistered;
            this.userCheck = userCheck;
        }

        @Override
        protected Boolean doInBackground(final String... strings) { //userid

            mDatabase.child("Users").child(strings[0]).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("doctor_details"))
                    {
                        userIsDoctor.setValue(true);
                    } else {
                        userIsDoctor.setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabase.child("Users").child(strings[0]).removeEventListener(this);
                }
            });

            return null;
        }
    }

    private static class CheckIfUserIsPatient extends AsyncTask<String, Void, Boolean>
    {
        DatabaseReference mDatabase;
        MutableLiveData<Boolean> userIsPatient;
        Application userCheck;

        CheckIfUserIsPatient(Application userCheck, DatabaseReference mDatabase, MutableLiveData<Boolean> userIsRegistered) {
            this.mDatabase = mDatabase;
            this.userIsPatient = userIsRegistered;
            this.userCheck = userCheck;
        }

        @Override
        protected Boolean doInBackground(final String... strings) { //userid

            mDatabase.child("Users").child(strings[0]).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("patient_details"))
                    {
                        userIsPatient.setValue(true);
                    } else {
                        userIsPatient.setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabase.child("Users").child(strings[0]).removeEventListener(this);
                }
            });

            return null;
        }
    }

    private static class CheckIfUserIsGuardian extends AsyncTask<String, Void, Boolean>
    {
        DatabaseReference mDatabase;
        MutableLiveData<Boolean> userIsGuardian;
        Application userCheck;

        CheckIfUserIsGuardian(Application userCheck, DatabaseReference mDatabase, MutableLiveData<Boolean> userIsRegistered) {
            this.mDatabase = mDatabase;
            this.userIsGuardian = userIsRegistered;
            this.userCheck = userCheck;
        }

        @Override
        protected Boolean doInBackground(final String... strings) { //userid

            mDatabase.child("Users").child(strings[0]).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("guardian_details"))
                    {
                        userIsGuardian.setValue(true);
                    } else {
                        userIsGuardian.setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabase.child("Users").child(strings[0]).removeEventListener(this);
                }
            });

            return null;
        }
    }
}
