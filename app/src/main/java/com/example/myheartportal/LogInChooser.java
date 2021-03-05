package com.example.myheartportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myheartportal.doctor.DoctorRegister;
import com.example.myheartportal.doctor.DoctorRoomList;
import com.example.myheartportal.guardian.GuardianRegister;
import com.example.myheartportal.guardian.GuardianRoomList;
import com.example.myheartportal.patient.PatientMain;
import com.example.myheartportal.patient.PatientRegister;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

import static com.example.myheartportal.Constants.USER_TYPE;

public class LogInChooser extends AppCompatActivity {

    //***LIFECYCLE
    UserViewModel userViewModel;
    LiveData<Boolean> userIsRegistered;
    //***LAYOUTS
    private Button btnLogMailPhone;
    private EditText etLogin, etPassword;
    private TextView tvRegister, tvReset;
    private ImageView ivLoginLogo;
    //***FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    //***SPOTS-DIALOG
    private AlertDialog spotsDialog;
    //***VARIABLES
    private String tag = this.getClass().getSimpleName();
    private String user_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_chooser);

        btnLogMailPhone = findViewById(R.id.btnLogMailPhone);
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        tvRegister = findViewById(R.id.tvRegister);
        tvReset = findViewById(R.id.tvReset);
        ivLoginLogo = findViewById(R.id.ivLoginLogo);

        user_type = getIntent().getStringExtra(USER_TYPE);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setUser_type(user_type, user_type);
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
                .setCancelable(false).build();

        userIsRegistered = userViewModel.getUserIsRegistered();
        userIsRegistered.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (mCurrentUser != null)
                {
                    if (mCurrentUser.isEmailVerified())
                    {
                        if (aBoolean)
                        {
                            if (TextUtils.equals(user_type, "doctor"))
                            {
                                Intent loginIntent = new Intent(LogInChooser.this, DoctorRoomList.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(LogInChooser.this, "Doctor logged in", Toast.LENGTH_SHORT).show();
                                startActivity(loginIntent);
                                spotsDialog.dismiss();
                                finish();
                            }
                            else if (TextUtils.equals(user_type, "patient"))
                            {
                                Intent loginIntent = new Intent(LogInChooser.this, PatientMain.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(LogInChooser.this, "Patient logged in.", Toast.LENGTH_SHORT).show();
                                startActivity(loginIntent);
                                spotsDialog.dismiss();
                                finish();
                            }
                            else if (TextUtils.equals(user_type, "guardian"))
                            {
                                Intent loginIntent = new Intent(LogInChooser.this, GuardianRoomList.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(LogInChooser.this, "Guardian logged in.", Toast.LENGTH_SHORT).show();
                                startActivity(loginIntent);
                                spotsDialog.dismiss();
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(LogInChooser.this, "Your email is not yet verified.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ivLoginLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(LogInChooser.this, "Sign in to My.Heart Portal", Toast.LENGTH_SHORT).show();
            }
        });

        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(etLogin.getText().toString()))
                {
                    mAuth.sendPasswordResetEmail(etLogin.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                Toast.makeText(LogInChooser.this, "Password reset sent.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LogInChooser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LogInChooser.this, "Please specify your email on the field above.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (user_type) {
                    case "patient":
                        Intent patientIntent = new Intent(LogInChooser.this, PatientRegister.class);
                        startActivity(patientIntent);
                        finish();
                        break;
                    case "doctor":
                        Intent doctorIntent = new Intent(LogInChooser.this, DoctorRegister.class);
                        startActivity(doctorIntent);
                        finish();
                        break;
                    case "guardian":
                        Intent guardianIntent = new Intent(LogInChooser.this, GuardianRegister.class);
                        startActivity(guardianIntent);
                        finish();
                        break;
                    default:
                        Toast.makeText(LogInChooser.this, "Error in My.Heart", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        btnLogMailPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mail_phone = etLogin.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!TextUtils.isEmpty(mail_phone) && !TextUtils.isEmpty(password))
                {
                    loginUser(mail_phone, password, user_type);
                }
                else
                {
                    Toast.makeText(LogInChooser.this, "Please fill up all necessary fields.", Toast.LENGTH_SHORT).show();
                }
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

    private void loginUser(final String mail_phone, final String password, final String user_type)
    {
        spotsDialog.show();
        if (Patterns.EMAIL_ADDRESS.matcher(mail_phone).matches())
        {
            //email is used for logging in
            mAuth.signInWithEmailAndPassword(mail_phone, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    if (authResult.getUser() != null && authResult.getUser().isEmailVerified())
                    {
                        //transfer
                        if (TextUtils.equals(user_type, "doctor"))
                        {
                            if (userIsRegistered.getValue() != null && userIsRegistered.getValue())
                            {
                                Intent loginIntent = new Intent(LogInChooser.this, DoctorRoomList.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(LogInChooser.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                                startActivity(loginIntent);
                                spotsDialog.dismiss();
                                finish();
                            } else {
                                Toast.makeText(LogInChooser.this, "Please register as doctor.", Toast.LENGTH_SHORT).show();
                                spotsDialog.dismiss();
                            }
                        }
                        else if (TextUtils.equals(user_type, "patient"))
                        {
                            if (userIsRegistered.getValue() != null && userIsRegistered.getValue())
                            {
                                Intent loginIntent = new Intent(LogInChooser.this, PatientMain.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(LogInChooser.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                                startActivity(loginIntent);
                                spotsDialog.dismiss();
                                finish();
                            } else {
                                Toast.makeText(LogInChooser.this, "Please register as patient.", Toast.LENGTH_SHORT).show();
                                spotsDialog.dismiss();
                            }
                        }
                        else if (TextUtils.equals(user_type, "guardian"))
                        {
                            if (userIsRegistered.getValue() != null && userIsRegistered.getValue())
                            {
                                Intent loginIntent = new Intent(LogInChooser.this, GuardianRoomList.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(LogInChooser.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                                startActivity(loginIntent);
                                spotsDialog.dismiss();
                                finish();
                            } else {
                                Toast.makeText(LogInChooser.this, "Please register as guardian.", Toast.LENGTH_SHORT).show();
                                spotsDialog.dismiss();
                            }
                        }
                        else {
                            Toast.makeText(LogInChooser.this, "Error in my.Heart.", Toast.LENGTH_SHORT).show();
                            spotsDialog.dismiss();
                        }
                    }
                    else {
                        Toast.makeText(LogInChooser.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        authResult.getUser().sendEmailVerification();
                        spotsDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LogInChooser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    spotsDialog.dismiss();
                }
            });
        }
        else if (Patterns.PHONE.matcher(mail_phone).matches())
        {
            //phone number is used for logging in
            Toast.makeText(this, "Login by phone not yet implemented", Toast.LENGTH_SHORT).show();
            spotsDialog.dismiss();
        }
        else
        {
            Toast.makeText(this, "You entered an invalid email.", Toast.LENGTH_SHORT).show();
            spotsDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.login_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.action_reload)
        {
            if (mCurrentUser != null)
            {
                mCurrentUser.reload();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
