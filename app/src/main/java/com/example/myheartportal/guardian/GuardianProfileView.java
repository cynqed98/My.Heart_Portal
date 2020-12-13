package com.example.myheartportal.guardian;

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

public class GuardianProfileView extends AppCompatActivity {

    //***LAYOUTS
    private TextView tvGuaName, tvOwnPhone, tvMailAddress;
    //***LIFECYCLE
    private MutableLiveData<String> guaName, guaPhone, guaMail;
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
        setContentView(R.layout.activity_guardian_profile_view);

        tvGuaName = findViewById(R.id.tvGuaName);
        tvOwnPhone = findViewById(R.id.tvOwnPhone);
        tvMailAddress = findViewById(R.id.tvMailAddress);

        guaName = new MutableLiveData<>("Full name");
        guaPhone = new MutableLiveData<>("Phone number");
        guaMail = new MutableLiveData<>("Mail address");

        guaName.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvGuaName.setText(s);
            }
        });
        guaPhone.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvOwnPhone.setText(s);
            }
        });
        guaMail.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvMailAddress.setText(s);
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
                    setGuardianUI (mCurrentUser.getUid());
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

    private void setGuardianUI (final String uid)
    {
        mDatabase.child("Users").child(uid).child("guardian_details")
        .addListenerForSingleValueEvent(new ValueEventListener()
        {
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
                guaName.setValue(fullName);

                String ownPhone = "", mail = "";
                if (dataSnapshot.hasChild("own_phone"))
                {
                    ownPhone = (String) dataSnapshot.child("own_phone").getValue();
                }
                if (dataSnapshot.hasChild("mail"))
                {
                    mail = (String) dataSnapshot.child("mail").getValue();
                }
                guaPhone.setValue(ownPhone);
                guaMail.setValue(mail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Users").child(uid).child("guardian_details").removeEventListener(this);
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
            Intent updateIntent = new Intent(GuardianProfileView.this, GuardianProfileUpdate.class);
            updateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(updateIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
