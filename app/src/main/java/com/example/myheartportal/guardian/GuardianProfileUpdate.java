package com.example.myheartportal.guardian;

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

public class GuardianProfileUpdate extends AppCompatActivity {

    //***LAYOUTS
    private EditText etFirstName, etMiddleName, etLastName, etMobilePhone;
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
        setContentView(R.layout.activity_guardian_profile_update);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etMobilePhone = findViewById(R.id.etMobilePhone);
        btnUpdate = findViewById(R.id.btnUpdate);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
                    Toast.makeText(GuardianProfileUpdate.this, "You can't update your information.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateInfo()
    {
        String fName = etFirstName.getText().toString().trim();
        String mName = etMiddleName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();
        String phone = etMobilePhone.getText().toString().trim();

        if (!TextUtils.isEmpty(fName) && !TextUtils.isEmpty(lName) && !TextUtils.isEmpty(phone))
        {
            new UploadProfile(GuardianProfileUpdate.this)
                    .execute(fName, mName, lName, phone);
        } else {
            Toast.makeText(this, "Please fill out necessary fields.", Toast.LENGTH_SHORT).show();
        }

    }

    private static class UploadProfile extends AsyncTask<String, Void, Boolean>
    {
        private WeakReference<GuardianProfileUpdate> guardianProfileUpdateWeakReference;
        private FirebaseUser mCurrentUser;
        private DatabaseReference mDatabase;

        UploadProfile (GuardianProfileUpdate guardianProfileUpdate)
        {
            guardianProfileUpdateWeakReference = new WeakReference<>(guardianProfileUpdate);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            GuardianProfileUpdate guardianProfileUpdate = guardianProfileUpdateWeakReference.get();
            if (guardianProfileUpdate == null || guardianProfileUpdate.isFinishing())
            {
                return;
            }
            this.mCurrentUser = guardianProfileUpdate.mCurrentUser;
            this.mDatabase = guardianProfileUpdate.mDatabase;
        }

        @Override
        protected Boolean doInBackground(String... strings) //fName, mName, lName, phone

        {
            if (mCurrentUser != null)
            {
                DatabaseReference mDatabaseUser = mDatabase.child("Users").child(mCurrentUser.getUid())
                        .child("guardian_details");
                Map<String, Object> guaProfile = new HashMap<>();

                guaProfile.put("first_name", strings[0]);
                guaProfile.put("middle_name", strings[1]);
                guaProfile.put("last_name", strings[2]);
                guaProfile.put("own_phone", strings[3]);
                guaProfile.put("mail", mCurrentUser.getEmail());
                mDatabaseUser.setValue(guaProfile);

                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            GuardianProfileUpdate guardianProfileUpdate = guardianProfileUpdateWeakReference.get();
            if (guardianProfileUpdate == null || guardianProfileUpdate.isFinishing())
            {
                return;
            }

            if (aBoolean)
            {
                Toast.makeText(guardianProfileUpdate, "Update successful.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(guardianProfileUpdate, GuardianRoomList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                guardianProfileUpdate.startActivity(intent);
                guardianProfileUpdate.finish();
            } else {
                Toast.makeText(guardianProfileUpdate, "Update failed.", Toast.LENGTH_SHORT).show();
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
