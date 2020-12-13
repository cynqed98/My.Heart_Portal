package com.example.myheartportal.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.myheartportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientProfileView extends AppCompatActivity {

    //***LAYOUTS
    private TextView tvPatName, tvDateOfBirth, tvOwnPhone, tvMailAddress, tvOther;
    //***LIFECYCLE
    private MutableLiveData<String> patName, patDob, patPhone, patMail, patOther;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //***VARIABLES
    private String tag = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile_view);

        tvPatName = findViewById(R.id.tvPatName);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvOwnPhone = findViewById(R.id.tvOwnPhone);
        tvMailAddress = findViewById(R.id.tvMailAddress);
        tvOther = findViewById(R.id.tvOther);

        patName = new MutableLiveData<>("Full name");
        patDob = new MutableLiveData<>("Date of birth");
        patPhone = new MutableLiveData<>("Phone number");
        patMail = new MutableLiveData<>("Mail address");
        patOther = new MutableLiveData<>("Sex:\nHeight:\nWeight\n");

        patName.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvPatName.setText(s);
            }
        });
        patDob.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvDateOfBirth.setText(s);
            }
        });
        patPhone.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvOwnPhone.setText(s);
            }
        });
        patMail.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvMailAddress.setText(s);
            }
        });
        patOther.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvOther.setText(s);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = firebaseAuth.getCurrentUser();
                if (mCurrentUser != null)
                {
                    setPatientUI (mCurrentUser.getUid());
                }
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        mCurrentUser = mAuth.getCurrentUser();
    }

    private void setPatientUI(final String uid)
    {
        mDatabase.child("Users").child(uid).child("patient_details")
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
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

                String dob = "", ownPhone = "", mail = "";
                if (dataSnapshot.hasChild("date_of_birth"))
                {
                    dob = (String) dataSnapshot.child("date_of_birth").getValue();
                }
                if (dataSnapshot.hasChild("own_phone"))
                {
                    ownPhone = (String) dataSnapshot.child("own_phone").getValue();
                }
                if (dataSnapshot.hasChild("mail"))
                {
                    mail = (String) dataSnapshot.child("mail").getValue();
                }
                patDob.setValue(dob);
                patPhone.setValue(ownPhone);
                patMail.setValue(mail);

                String gender = "", height = "", weight = "";
                if (dataSnapshot.hasChild("gender"))
                {
                    gender = (String) dataSnapshot.child("gender").getValue();
                }
                if (dataSnapshot.hasChild("height"))
                {
                    height = (String) dataSnapshot.child("height").getValue();
                }
                if (dataSnapshot.hasChild("weight"))
                {
                    weight = (String) dataSnapshot.child("weight").getValue();
                }
                String otherInfo = "Sex: " + gender + "\n" + "Height: " + height + "\n" + "Weight: " + weight;
                patOther.setValue(otherInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Users").child(uid).child("patient_details").removeEventListener(this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.view_profile_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.action_update_online)
        {
            Intent updatePatientIntent = new Intent(PatientProfileView.this, PatientProfileUpdate.class);
            updatePatientIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(updatePatientIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
