package com.example.myheartportal.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.app.AlertDialog;
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

import com.example.myheartportal.LogInChooser;
import com.example.myheartportal.R;
import com.example.myheartportal.UserViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

import static com.example.myheartportal.Constants.USER_TYPE;

public class DoctorRegister extends AppCompatActivity {

    //***LIFECYCLE
    UserViewModel userViewModel;
    LiveData<Boolean> userIsRegistered;
    //***LAYOUTS
    private EditText etFirstName, etMiddleName, etLastName, etHospital, etMobilePhone, etMailAddress, etRegPassword, etReEnterPass;
    private CheckBox cbMail, cbPhone;
    private LinearLayout lvPassword, lvReEnterPass, lvVerify;
    private Button btnDocRegister;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //***SPOTS-DIALOG
    private AlertDialog spotsDialog;
    //***VARIABLES
    private String tag = this.getClass().getSimpleName();
    private boolean isBoxChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_register);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etHospital = findViewById(R.id.etHospital);
        etMobilePhone = findViewById(R.id.etMobilePhone);
        etMailAddress = findViewById(R.id.etMailAddress);
        etRegPassword = findViewById(R.id.etRegPassword);
        etReEnterPass = findViewById(R.id.etReEnterPass);
        lvPassword = findViewById(R.id.lvPassword);
        lvReEnterPass = findViewById(R.id.lvReEnterPass);
        lvVerify = findViewById(R.id.lvVerify);
        cbMail = findViewById(R.id.cbMail);
        cbPhone = findViewById(R.id.cbPhone);
        btnDocRegister = findViewById(R.id.btnDocRegister);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setUser_type("doctor", "doctor");
        userViewModel.initCheckIfUserReg();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = firebaseAuth.getCurrentUser();
                if (mCurrentUser != null)
                {
                    userViewModel.setUserIdForReg(mCurrentUser.getUid());
                }
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        spotsDialog = new SpotsDialog.Builder().setContext(this).setTheme(R.style.CustomSpotDialog)
                .setCancelable(true).build();

        userIsRegistered = userViewModel.getUserIsRegistered();
        userIsRegistered.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (mCurrentUser != null && !aBoolean)
                {
                    lvPassword.setVisibility(View.GONE);
                    lvReEnterPass.setVisibility(View.GONE);
                    lvVerify.setVisibility(View.GONE);
                }
            }
        });

        cbMail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isChecked())
                {
                    cbPhone.setChecked(false);
                    isBoxChecked = true;
                }
                else
                {
                    isBoxChecked = false;
                }
            }
        });

        cbPhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isChecked())
                {
                    cbMail.setChecked(false);
                    isBoxChecked = true;
                }
                else
                {
                    isBoxChecked = false;
                }
            }
        });

        btnDocRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerIsClicked();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        mCurrentUser = mAuth.getCurrentUser();
    }

    private void registerIsClicked()
    {
        String first_name = etFirstName.getText().toString().trim();
        String middle_name = etMiddleName.getText().toString().trim();
        String last_name = etLastName.getText().toString().trim();
        String hospital = etHospital.getText().toString().trim();
        String own_phone = etMobilePhone.getText().toString().trim();
        String mail = etMailAddress.getText().toString().trim();
        String password = etRegPassword.getText().toString();
        String rePassword = etReEnterPass.getText().toString();

        boolean passwordOkay;
        if (password.equals(rePassword))
        {
            if (password.length() < 6)
            {
                passwordOkay = false;
            }
            else
                passwordOkay = true;
        }
        else
        {
            passwordOkay = false;
        }

        if (mCurrentUser != null)
        {
            if (userIsRegistered.getValue() != null && !userIsRegistered.getValue())
            {
                if (!TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name)
                        && !TextUtils.isEmpty(hospital) && !TextUtils.isEmpty(own_phone))
                {
                    if (TextUtils.isEmpty(mail) && !TextUtils.isEmpty(own_phone)) //register by phone
                    {
                        Toast.makeText(this, "Registering by phone is not yet implemented, please enter your email.",
                                Toast.LENGTH_SHORT).show();
                    }
                    else if (!TextUtils.isEmpty(mail)) //email is used for registering
                    {
                        if (TextUtils.equals(mCurrentUser.getEmail(), mail))
                        {
                            new AddToDatabase(DoctorRegister.this)
                                .execute(first_name, middle_name, last_name, hospital, own_phone, mail);
                        } else {
                            Toast.makeText(this, "Another email used by Patient/Guardian is logged in. Please log it out first.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Please fill up all necessary fields.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(DoctorRegister.this, "Please fill up and double check all necessary fields.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(DoctorRegister.this, "You are already registered.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (passwordOkay)
            {
                if (isBoxChecked)
                {
                    if (!TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name) && !TextUtils.isEmpty(hospital))
                    {
                        if (TextUtils.isEmpty(mail) && !TextUtils.isEmpty(own_phone)) //register by phone
                        {
                            Toast.makeText(this, "Registering by phone is not yet implemented, please enter your email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else if (!TextUtils.isEmpty(mail)) //email is used for registering
                        {
                            registerNewUser (first_name, middle_name, last_name, hospital, own_phone, mail, password);

                        } else {
                            Toast.makeText(this, "Please fill up all necessary fields.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DoctorRegister.this, "Please fill up and double check all necessary fields",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DoctorRegister.this, "Please select verification type.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DoctorRegister.this, "Passwords too short or doesn't match.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class AddToDatabase extends AsyncTask<String, Void, Boolean>
    {
        private WeakReference<DoctorRegister> doctorRegisterWeakReference;
        private FirebaseUser mCurrentUser;
        private DatabaseReference mDatabase;

        AddToDatabase (DoctorRegister doctorRegister)
        {
            doctorRegisterWeakReference = new WeakReference<>(doctorRegister);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            DoctorRegister doctorRegister = doctorRegisterWeakReference.get();
            if (doctorRegister == null || doctorRegister.isFinishing())
            {
                return;
            }

            this.mCurrentUser = doctorRegister.mCurrentUser;
            this.mDatabase = doctorRegister.mDatabase;
            doctorRegister.spotsDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) { //fname, mname, lname, hosp, phone, mail

            DatabaseReference doctorDatabase = mDatabase.child("Users").child(mCurrentUser.getUid())
                .child("doctor_details");
            Map<String, Object> docProfile = new HashMap<>();

            docProfile.put("first_name", strings[0]);
            docProfile.put("middle_name", strings[1]);
            docProfile.put("last_name", strings[2]);
            docProfile.put("hospital", strings[3]);
            docProfile.put("own_phone", strings[4]);
            docProfile.put("mail", strings[5]);
            doctorDatabase.setValue(docProfile);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            DoctorRegister doctorRegister = doctorRegisterWeakReference.get();
            if (doctorRegister == null || doctorRegister.isFinishing())
            {
                return;
            }

            doctorRegister.spotsDialog.dismiss();
            if (aBoolean)
            {
                Toast.makeText(doctorRegister, "Registration as doctor successful.", Toast.LENGTH_SHORT).show();

                Intent doctorIntent = new Intent(doctorRegister, LogInChooser.class);
                doctorIntent.putExtra(USER_TYPE, "doctor");
                doctorIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                doctorRegister.startActivity(doctorIntent);
                doctorRegister.finish();
            } else {
                Toast.makeText(doctorRegister, "Registration failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerNewUser (final String first_name, final String middle_name, final String last_name,
                                 final String hospital, final String own_phone, final String mail, String password)
    {
        spotsDialog.show();

        if (cbMail.isChecked()) //verify by mail
        {
            mAuth.createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener(DoctorRegister.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful())
                    {
                        if (mCurrentUser != null)
                        {
                            mCurrentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        spotsDialog.dismiss();

                                        new AddToDatabase(DoctorRegister.this)
                                                .execute(first_name, middle_name, last_name, hospital, own_phone, mail);
                                    } else {
                                        if (task.getException() != null)
                                            Toast.makeText(DoctorRegister.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        spotsDialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            spotsDialog.dismiss();
                        }
                    } else {
                        spotsDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(DoctorRegister.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    spotsDialog.dismiss();
                }
            });
        } else if (cbPhone.isChecked())
        {
            Toast.makeText(this, "Verification by phone not yet implemented.", Toast.LENGTH_SHORT).show();
            spotsDialog.dismiss();
        }
    }
}
