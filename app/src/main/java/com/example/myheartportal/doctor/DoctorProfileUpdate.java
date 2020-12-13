package com.example.myheartportal.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myheartportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class DoctorProfileUpdate extends AppCompatActivity {

    //***LAYOUTS
    private EditText etFirstName, etMiddleName, etLastName, etHospital, etMobilePhone;
    private Button btnUpdate;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //***VARIABLES
    private final String tag = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile_update);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etHospital = findViewById(R.id.etHospital);
        etMobilePhone = findViewById(R.id.etMobilePhone);
        btnUpdate = findViewById(R.id.btnUpdate);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = firebaseAuth.getCurrentUser();
            }
        };

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCurrentUser != null)
                {
                    updateInfo();
                } else {
                    Toast.makeText(DoctorProfileUpdate.this, "You can't update your information.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateInfo()
    {
        String fName = etFirstName.getText().toString().trim();
        String mName = etMiddleName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();
        String hospital = etHospital.getText().toString().trim();
        String phone = etMobilePhone.getText().toString().trim();

        if (!TextUtils.isEmpty(fName) && !TextUtils.isEmpty(lName) &&
                !TextUtils.isEmpty(hospital) && !TextUtils.isEmpty(phone))
        {
            new UploadProfile(DoctorProfileUpdate.this)
                    .execute(fName, mName, lName, hospital, phone);
        } else {
            Toast.makeText(this, "Please fill out necessary fields.", Toast.LENGTH_SHORT).show();
        }

    }

    private static class UploadProfile extends AsyncTask<String, String, Boolean>
    {
        private WeakReference<DoctorProfileUpdate> doctorProfileUpdateWeakReference;
        private FirebaseUser mCurrentUser;
        private DatabaseReference mDatabase;

        UploadProfile (DoctorProfileUpdate doctorProfileUpdate)
        {
            doctorProfileUpdateWeakReference = new WeakReference<>(doctorProfileUpdate);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            DoctorProfileUpdate doctorProfileUpdate = doctorProfileUpdateWeakReference.get();
            if (doctorProfileUpdate == null || doctorProfileUpdate.isFinishing())
            {
                return;
            }
            this.mCurrentUser = doctorProfileUpdate.mCurrentUser;
            this.mDatabase = doctorProfileUpdate.mDatabase;
        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            if (mCurrentUser != null)
            {
                DatabaseReference mDatabaseUser = mDatabase.child("Users").child(mCurrentUser.getUid())
                        .child("doctor_details");
                //fName, mName, lName, hospital, phone, mail
                Map<String, Object> docProfile = new HashMap<>();

                docProfile.put("first_name", strings[0]);
                docProfile.put("middle_name", strings[1]);
                docProfile.put("last_name", strings[2]);
                docProfile.put("hospital", strings[3]);
                docProfile.put("own_phone", strings[4]);
                docProfile.put("mail", mCurrentUser.getEmail());
                mDatabaseUser.setValue(docProfile);

                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            DoctorProfileUpdate doctorProfileUpdate = doctorProfileUpdateWeakReference.get();
            if (doctorProfileUpdate == null || doctorProfileUpdate.isFinishing())
            {
                return;
            }

            if (aBoolean)
            {
                Toast.makeText(doctorProfileUpdate, "Update successful.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(doctorProfileUpdate, DoctorRoomList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                doctorProfileUpdate.startActivity(intent);
                doctorProfileUpdate.finish();
            } else {
                Toast.makeText(doctorProfileUpdate, "Update failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        mCurrentUser = mAuth.getCurrentUser();
    }
}
