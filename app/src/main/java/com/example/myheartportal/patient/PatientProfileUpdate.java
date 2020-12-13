package com.example.myheartportal.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myheartportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PatientProfileUpdate extends AppCompatActivity {

    //***LAYOUTS
    private EditText etFirstName, etMiddleName, etLastName, etSelectDate, etMobilePhone, etHeight, etWeight;
    private LinearLayout llMoreInfo;
    private CheckBox cbMoreInfo, cbMale, cbFemale;
    private CheckBox cbInches, cbCenti, cbMeter, cbKilo, cbPound;
    private Button btnPatUpdate;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //***VARIABLES
    private final String tag = this.getClass().getSimpleName();
    private boolean moreInfoChecked = false;
    private boolean heightChecked = false;
    private boolean weightChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile_update);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etSelectDate = findViewById(R.id.etSelectDate);
        etMobilePhone = findViewById(R.id.etMobilePhone);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        llMoreInfo = findViewById(R.id.llMoreInfo);
        cbMoreInfo = findViewById(R.id.cbMoreInfo);
        cbMale = findViewById(R.id.cbMale);
        cbFemale = findViewById(R.id.cbFemale);
        cbInches = findViewById(R.id.cbInches);
        cbCenti = findViewById(R.id.cbCenti);
        cbMeter = findViewById(R.id.cbMeter);
        cbKilo = findViewById(R.id.cbKilo);
        cbPound = findViewById(R.id.cbPound);
        btnPatUpdate = findViewById(R.id.btnPatUpdate);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = firebaseAuth.getCurrentUser();
            }
        };

        btnPatUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCurrentUser != null)
                {
                    updatePatientInfo();
                } else {
                    Toast.makeText(PatientProfileUpdate.this, "You can't update your information.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cbMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isChecked())
                {
                    cbFemale.setChecked(false);
                }
            }
        });
        cbFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                {
                    cbMale.setChecked(false);
                }
            }
        });

        cbInches.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                {
                    cbCenti.setChecked(false);
                    cbMeter.setChecked(false);
                    heightChecked = true;
                } else {
                    heightChecked = false;
                }
            }
        });
        cbCenti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                {
                    cbInches.setChecked(false);
                    cbMeter.setChecked(false);
                    heightChecked = true;
                } else {
                    heightChecked = false;
                }
            }
        });
        cbMeter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                {
                    cbInches.setChecked(false);
                    cbCenti.setChecked(false);
                    heightChecked = true;
                } else {
                    heightChecked = false;
                }
            }
        });

        cbKilo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                {
                    cbPound.setChecked(false);
                    weightChecked = true;
                } else {
                    weightChecked = false;
                }
            }
        });
        cbPound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                {
                    cbKilo.setChecked(false);
                    weightChecked = true;
                } else {
                    weightChecked = false;
                }
            }
        });

        llMoreInfo.setVisibility(View.GONE);
        cbMoreInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isChecked())
                {
                    moreInfoChecked = true;
                    llMoreInfo.setVisibility(View.VISIBLE);
                } else {
                    moreInfoChecked = false;
                    llMoreInfo.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updatePatientInfo()
    {
        String fName = etFirstName.getText().toString().trim();
        String mName = etMiddleName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();
        String dob = etSelectDate.getText().toString().trim();
        String phone = etMobilePhone.getText().toString().trim();
        String gender = "";
        String height = "";
        String weight = "";

        if (moreInfoChecked)
        {
            if (cbMale.isChecked())
            {
                gender = "Male";
            }
            else if (cbFemale.isChecked())
            {
                gender = "Female";
            }
            else {
                gender = "Not specified ";
            }
            height = etHeight.getText().toString().toLowerCase().trim();
            weight = etWeight.getText().toString().toLowerCase().trim();
        }

        if (moreInfoChecked)
        {
            if (!TextUtils.isEmpty(fName) && !TextUtils.isEmpty(lName) && isValidDate(dob) &&
                    !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(height) && !TextUtils.isEmpty(weight))
            {
                if (heightChecked && weightChecked)
                {
                    if (cbInches.isChecked())
                    {
                        height = height.concat(" in");
                    }
                    else if (cbCenti.isChecked())
                    {
                        height = height.concat(" cm");
                    }
                    else if (cbMeter.isChecked())
                    {
                        height = height.concat(" m");
                    }

                    if (cbKilo.isChecked())
                    {
                        weight = weight.concat(" kgs");
                    }
                    else if (cbPound.isChecked())
                    {
                        weight = weight.concat(" lbs");
                    }
                    new UpdatePatientProfile(PatientProfileUpdate.this)
                            .execute(fName, mName, lName, dob, phone, gender, height, weight);

                } else {
                    Toast.makeText(this, "Please specify the units of measurement in checkboxes.",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Please fill out necessary fields", Toast.LENGTH_SHORT).show();
            }

        } else {

            if (!TextUtils.isEmpty(fName) && !TextUtils.isEmpty(lName) &&
                    isValidDate(dob) && !TextUtils.isEmpty(phone))
            {
                gender = "Not specified ";
                height = "Not specified ";
                weight = "Not specified ";
                new UpdatePatientProfile(PatientProfileUpdate.this)
                        .execute(fName, mName, lName, dob, phone, gender, height, weight);
            } else {
                Toast.makeText(this, "Please fill out necessary fields.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class UpdatePatientProfile extends AsyncTask<String, Void, Boolean>
    {
        private WeakReference<PatientProfileUpdate> patientProfileUpdateWeakReference;
        private FirebaseUser mCurrentUser;
        private DatabaseReference mDatabase;

        UpdatePatientProfile (PatientProfileUpdate patientProfileUpdate)
        {
            patientProfileUpdateWeakReference = new WeakReference<>(patientProfileUpdate);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            PatientProfileUpdate patientProfileUpdate = patientProfileUpdateWeakReference.get();
            if (patientProfileUpdate == null || patientProfileUpdate.isFinishing())
            {
                return;
            }
            this.mCurrentUser = patientProfileUpdate.mCurrentUser;
            this.mDatabase = patientProfileUpdate.mDatabase;
        }

        @Override
        protected Boolean doInBackground(String... strings) { //fName, mName, lName, dob, phone, gender, height, weight

            if (mCurrentUser != null)
            {
                DatabaseReference patientDatabase = mDatabase.child("Users").child(mCurrentUser.getUid())
                        .child("patient_details");
                Map<String, Object> patProfile = new HashMap<>();

                patProfile.put("first_name", strings[0]);
                patProfile.put("middle_name", strings[1]);
                patProfile.put("last_name", strings[2]);
                patProfile.put("date_of_birth", strings[3]);
                patProfile.put("own_phone", strings[4]);
                patProfile.put("gender", strings[5]);
                patProfile.put("height", strings[6]);
                patProfile.put("weight", strings[7]);
                patProfile.put("mail", mCurrentUser.getEmail());
                patientDatabase.setValue(patProfile);

                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            PatientProfileUpdate patientProfileUpdate = patientProfileUpdateWeakReference.get();
            if (patientProfileUpdate == null || patientProfileUpdate.isFinishing())
            {
                return;
            }
            if (aBoolean)
            {
                Toast.makeText(patientProfileUpdate, "Update successful.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(patientProfileUpdate, PatientMain.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                patientProfileUpdate.startActivity(intent);
                patientProfileUpdate.finish();
            } else {
                Toast.makeText(patientProfileUpdate, "Update failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidDate (String date)
    {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date DOB = null;
        dateFormat.setLenient(false);

        try
        {
            DOB = dateFormat.parse(date);
            return true;

        } catch (ParseException e) {

            Toast.makeText(PatientProfileUpdate.this, "Please check the date format.",
                    Toast.LENGTH_SHORT).show();
            return false;
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
