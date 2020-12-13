package com.example.myheartportal.guardian;

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


public class GuardianRegister extends AppCompatActivity {

    //***LIFECYCLE
    UserViewModel userViewModel;
    LiveData<Boolean> userIsRegistered;
    //***LAYOUTS
    private EditText etFirstName, etMiddleName, etLastName, etMobilePhone, etMailAddress, etRegPassword, etReEnterPass;
    private CheckBox cbMail, cbPhone;
    private LinearLayout lvPassword, lvReEnterPass, lvVerify;
    private Button btnGuaRegister;
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
        setContentView(R.layout.activity_guardian_register);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etMobilePhone = findViewById(R.id.etMobilePhone);
        etMailAddress = findViewById(R.id.etMailAddress);
        etRegPassword = findViewById(R.id.etRegPassword);
        etReEnterPass = findViewById(R.id.etReEnterPass);
        lvPassword = findViewById(R.id.lvPassword);
        lvReEnterPass = findViewById(R.id.lvReEnterPass);
        lvVerify = findViewById(R.id.lvVerify);
        cbMail = findViewById(R.id.cbMail);
        cbPhone = findViewById(R.id.cbPhone);
        btnGuaRegister = findViewById(R.id.btnGuaRegister);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setUser_type("guardian", "guardian");
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

        btnGuaRegister.setOnClickListener(new View.OnClickListener() {
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
                if (!TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name) && !TextUtils.isEmpty(own_phone))
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
                            new AddToDatabase(GuardianRegister.this)
                                    .execute(first_name, middle_name, last_name, own_phone, mail);
                        } else {
                            Toast.makeText(this, "Another email used by Doctor/Patient is logged in. Please log it out first.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Please fill up all necessary fields.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(GuardianRegister.this, "Please fill up and double check all necessary fields.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(GuardianRegister.this, "You cannot register.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (passwordOkay)
            {
                if (isBoxChecked)
                {
                    if (!TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name))
                    {
                        if (TextUtils.isEmpty(mail) && !TextUtils.isEmpty(own_phone)) //register by phone
                        {
                            Toast.makeText(this, "Registering by phone is not yet implemented. " +
                                            "Please use e-mail instead.", Toast.LENGTH_SHORT).show();
                        }
                        else if (!TextUtils.isEmpty(mail)) //email is used for registering
                        {
                            registerTheUser(first_name, middle_name, last_name, own_phone, mail, password);
                        }
                        else {
                            Toast.makeText(this, "Please fill up all necessary fields",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(GuardianRegister.this, "Please fill up and double check all necessary fields",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GuardianRegister.this, "Please select verification type.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(GuardianRegister.this, "Password too short or unequal passwords entered.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class AddToDatabase extends AsyncTask<String, Void, Boolean>
    {
        private WeakReference<GuardianRegister> guardianRegisterWeakReference;
        private FirebaseUser mCurrentUser;
        private DatabaseReference mDatabase;

        AddToDatabase (GuardianRegister guardianRegister)
        {
            guardianRegisterWeakReference = new WeakReference<>(guardianRegister);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            GuardianRegister guardianRegister = guardianRegisterWeakReference.get();
            if (guardianRegister == null || guardianRegister.isFinishing())
            {
                return;
            }

            this.mCurrentUser = guardianRegister.mCurrentUser;
            this.mDatabase = guardianRegister.mDatabase;
            guardianRegister.spotsDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) { //fname, mname, lname, phone, mail

            DatabaseReference guardianDatabase = mDatabase.child("Users").child(mCurrentUser.getUid())
                    .child("guardian_details");
            Map<String, Object> guaProfile = new HashMap<>();

            guaProfile.put("first_name", strings[0]);
            guaProfile.put("middle_name", strings[1]);
            guaProfile.put("last_name", strings[2]);
            guaProfile.put("own_phone", strings[3]);
            guaProfile.put("mail", strings[4]);
            guardianDatabase.setValue(guaProfile);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            GuardianRegister guardianRegister = guardianRegisterWeakReference.get();
            if (guardianRegister == null || guardianRegister.isFinishing())
            {
                return;
            }

            guardianRegister.spotsDialog.dismiss();
            if (aBoolean)
            {
                Toast.makeText(guardianRegister, "Registration as guardian successful.", Toast.LENGTH_SHORT).show();

                Intent guardianIntent = new Intent(guardianRegister, LogInChooser.class);
                guardianIntent.putExtra(USER_TYPE, "guardian");
                guardianIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                guardianRegister.startActivity(guardianIntent);
                guardianRegister.finish();
            } else {
                Toast.makeText(guardianRegister, "Registration failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerTheUser(final String first_name, final String middle_name, final String last_name,
                                 final String own_phone, final String mail, String password)
    {
        spotsDialog.show();

        if (cbMail.isChecked()) //verify by mail
        {
            mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(GuardianRegister.this,
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
                                        spotsDialog.dismiss();

                                        new AddToDatabase(GuardianRegister.this)
                                                .execute(first_name, middle_name, last_name, own_phone, mail);
                                    } else {
                                        if (task.getException() != null)
                                            Toast.makeText(GuardianRegister.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GuardianRegister.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
