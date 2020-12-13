package com.example.myheartportal.doctor;

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

import java.util.HashMap;
import java.util.Map;

public class DoctorProfileView extends AppCompatActivity {

    //***LAYOUTS
    private TextView tvDocName, tvHospital, tvOwnPhone, tvMailAddress;
    //***LIFECYCLE
    private MutableLiveData<String> docName, docHosp, docPhone, docMail;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ValueEventListener profileViewListener;
    //***VARIABLES
    private String tag = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile_view);

        tvDocName = findViewById(R.id.tvDocName);
        tvHospital = findViewById(R.id.tvHospital);
        tvOwnPhone = findViewById(R.id.tvOwnPhone);
        tvMailAddress = findViewById(R.id.tvMailAddress);

        docName = new MutableLiveData<>("Full name");
        docHosp = new MutableLiveData<>("Hospital");
        docPhone = new MutableLiveData<>("Phone number");
        docMail = new MutableLiveData<>("Mail address");

        docName.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvDocName.setText(s);
            }
        });
        docHosp.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvHospital.setText(s);
            }
        });
        docPhone.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvOwnPhone.setText(s);
            }
        });
        docMail.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvMailAddress.setText(s);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = firebaseAuth.getCurrentUser();
                if (mCurrentUser != null)
                {
                    setDoctorUI(mCurrentUser.getUid());
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

    private void setDoctorUI(final String uid)
    {
        mDatabase.child("Users").child(uid).child("doctor_details")
        .addListenerForSingleValueEvent(profileViewListener = new ValueEventListener() {
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
                docHosp.setValue(hospital);
                docPhone.setValue(ownPhone);
                docMail.setValue(mail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Users").child(uid).child("doctor_details").removeEventListener(this);
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
            Intent updateIntent = new Intent(DoctorProfileView.this, DoctorProfileUpdate.class);
            updateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(updateIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (profileViewListener != null && mCurrentUser != null)
        {
            mDatabase.child("Users").child(mCurrentUser.getUid()).child("doctor_details")
                    .removeEventListener(profileViewListener);
        }
    }
}
