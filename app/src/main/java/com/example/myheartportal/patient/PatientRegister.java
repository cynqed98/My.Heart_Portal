package com.example.myheartportal.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

import static com.example.myheartportal.Constants.USER_TYPE;

public class PatientRegister extends AppCompatActivity {

    //***LIFECYCLE
    UserViewModel userViewModel;
    LiveData<Boolean> userIsRegistered;
    //***LAYOUTS
    private EditText etFirstName, etMiddleName, etLastName, etSelectDate, etMobilePhone, etMailAddress, etRegPassword, etReEnterPass;
    private Button btnPatRegister;
    private LinearLayout lvPassword, lvReEnterPass, lvVerify;
    private CheckBox cbMail, cbPhone;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //***SPOTS-DIALOG
    private AlertDialog spotsDialog;
    //***VARIABLES
    private String tag = this.getClass().getSimpleName();
    private boolean isBoxChecked = false; //check if box is checked

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_register);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etSelectDate = findViewById(R.id.etSelectDate);
        etMobilePhone = findViewById(R.id.etMobilePhone);
        etMailAddress = findViewById(R.id.etMailAddress);
        etRegPassword = findViewById(R.id.etRegPassword);
        etReEnterPass = findViewById(R.id.etReEnterPass);
        btnPatRegister = findViewById(R.id.btnPatRegister);
        lvPassword = findViewById(R.id.lvPassword);
        lvReEnterPass = findViewById(R.id.lvReEnterPass);
        lvVerify = findViewById(R.id.lvVerify);
        cbMail = findViewById(R.id.cbMail);
        cbPhone = findViewById(R.id.cbPhone);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setUser_type("patient", "patient");
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

        //REGISTERING
        btnPatRegister.setOnClickListener(new View.OnClickListener() {
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
        String date_of_birth = etSelectDate.getText().toString().trim();
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
                if (!TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name) && isValidDate(date_of_birth))
                {
                    if (TextUtils.isEmpty(mail) && !TextUtils.isEmpty(own_phone)) //register by phone
                    {
                        Toast.makeText(this, "Registering by phone is not yet implemented. Please enter you email.",
                                Toast.LENGTH_SHORT).show();
                    }
                    else if (!TextUtils.isEmpty(mail)) //email is used for registering
                    {
                        if (TextUtils.equals(mCurrentUser.getEmail(), mail))
                        {
                            new AddToDatabase(PatientRegister.this)
                                    .execute(first_name, middle_name, last_name, date_of_birth, own_phone, mail);
                        } else {
                            Toast.makeText(this, "Another email used by Doctor/Guardian is logged in. Please log it out first.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(this, "Please fill up all necessary fields.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(PatientRegister.this, "Please fill up and double check all necessary fields.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(PatientRegister.this, "You are already registered.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (passwordOkay)
            {
                if (isBoxChecked)
                {
                    if (!TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name) && isValidDate(date_of_birth))
                    {
                        if (TextUtils.isEmpty(mail) && !TextUtils.isEmpty(own_phone)) //register by phone
                        {
                            Toast.makeText(this, "Registering by phone is not yet implemented. Please use e-mail instead.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else if (!TextUtils.isEmpty(mail)) //email is used for registering
                        {
                            registerTheUser(first_name, middle_name, last_name, date_of_birth, own_phone, mail, password);
                        }
                        else {
                            Toast.makeText(this, "Please fill up all necessary fields", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PatientRegister.this, "Please fill up and double check all necessary fields",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PatientRegister.this, "Please select verification type.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PatientRegister.this, "Passwords too short or doesn't match.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class AddToDatabase extends AsyncTask<String, Void, Boolean>
    {
        private WeakReference<PatientRegister> patientRegisterWeakReference;
        private DatabaseReference mDatabase;
        private FirebaseUser mCurrentUser;

        AddToDatabase (PatientRegister patientRegister)
        {
            patientRegisterWeakReference = new WeakReference<>(patientRegister);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            PatientRegister patientRegister = patientRegisterWeakReference.get();
            if (patientRegister == null || patientRegister.isFinishing())
            {
                return;
            }
            this.mDatabase = patientRegister.mDatabase;
            this.mCurrentUser = patientRegister.mCurrentUser;
            patientRegister.spotsDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            //first_name, middle_name, last_name, date_of_birth, own_phone, mail

            DatabaseReference patientDatabase = mDatabase.child("Users").child(mCurrentUser.getUid())
                    .child("patient_details");
            Map<String, Object> patProfile = new HashMap<>();

            patProfile.put("first_name", strings[0]);
            patProfile.put("middle_name", strings[1]);
            patProfile.put("last_name", strings[2]);
            patProfile.put("date_of_birth", strings[3]);
            patProfile.put("own_phone", strings[4]);
            patProfile.put("mail", strings[5]);
            patientDatabase.setValue(patProfile);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            PatientRegister patientRegister = patientRegisterWeakReference.get();
            if (patientRegister == null || patientRegister.isFinishing())
            {
                return;
            }

            patientRegister.spotsDialog.dismiss();
            if (aBoolean)
            {
                Toast.makeText(patientRegister, "Registration as patient successful.", Toast.LENGTH_SHORT).show();
                Intent patientIntent = new Intent(patientRegister, PatientMain.class);
                patientIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                patientIntent.putExtra(USER_TYPE, "patient");
                patientRegister.startActivity(patientIntent);
                patientRegister.finish();
            } else {
                Toast.makeText(patientRegister, "Registration failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerTheUser(final String first_name, final String middle_name, final String last_name,
                                 final String date_of_birth, final String own_phone, final String mail, String password)
    {
        spotsDialog.show();
        if (cbMail.isChecked())
        {
            mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(PatientRegister.this,
            new OnCompleteListener<AuthResult>() {
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
                                        new AddToDatabase(PatientRegister.this)
                                                .execute(first_name, middle_name, last_name, date_of_birth, own_phone, mail);
                                    } else {
                                        if (task.getException() != null)
                                            Toast.makeText(PatientRegister.this, task.getException()
                                                    .getMessage(), Toast.LENGTH_SHORT).show();
                                        spotsDialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            spotsDialog.dismiss();
                        }
                    }
                    else {
                        spotsDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PatientRegister.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    spotsDialog.dismiss();
                }
            });
        } else if (cbPhone.isChecked()) //not implemented
        {
            Toast.makeText(this, "Verification by phone not yet implemented.", Toast.LENGTH_SHORT).show();
            spotsDialog.dismiss();
        }
    }

    private boolean isValidDate (String date)
    {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date DOB = null;
        dateFormat.setLenient(false);

        try
        {
            DOB = dateFormat.parse(date);
            return true;

        } catch (ParseException e) {

            Toast.makeText(PatientRegister.this, "Please check the date format.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
