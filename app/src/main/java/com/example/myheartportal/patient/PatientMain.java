package com.example.myheartportal.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.os.AsyncTask;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myheartportal.LogInChooser;
import com.example.myheartportal.R;
import com.example.myheartportal.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.example.myheartportal.Constants.PHONE_NUMBERS;
import static com.example.myheartportal.Constants.SERVICE_START;
import static com.example.myheartportal.Constants.USER_TYPE;

public class PatientMain extends AppCompatActivity {

    //***LIFECYCLE
    UserViewModel userViewModel;
    LiveData<Boolean> userIsRegistered, patientHasRoom;
    //***CUSTOMDIALOG
    private AlertDialog addRoomDialog;
    //***LAYOUTS
    private LinearLayout llbtnSwitch, llbtnConnect, llbtnRoom;
    private TextView tvSwitch, tvConnect;
    private ImageView ivRoomIcon;
    //***SQLITE
    private PatientDatabaseHelper patientDatabaseHelper;
    //***FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    //***VARIABLES
    private String [] patientSavedInfo;
    private String user_type;
    private String tag = this.getClass().getSimpleName();
    private static final int PERMISSION_CODE = 53; // for android permissions
    private boolean isServiceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        user_type = getIntent().getStringExtra(USER_TYPE);

        patientDatabaseHelper = new PatientDatabaseHelper(this);
        llbtnSwitch = findViewById(R.id.llbtnSwitch);
        llbtnConnect = findViewById(R.id.llbtnConnect);
        llbtnRoom = findViewById(R.id.llbtnRoom);
        tvSwitch = findViewById(R.id.tvSwitch);
        tvConnect = findViewById(R.id.tvConnect);
        ivRoomIcon = findViewById(R.id.ivRoomIcon);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setUser_type(user_type, "patient");
        userViewModel.initCheckPatRoom();
        userViewModel.initCheckIfUserReg();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = firebaseAuth.getCurrentUser();
                if (mCurrentUser != null)
                {
                    userViewModel.setUseridForRoom(mCurrentUser.getUid());
                    userViewModel.setUserIdForReg(mCurrentUser.getUid());
                    mCurrentUser.reload();
                }
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        userIsRegistered = userViewModel.getUserIsRegistered();
        userIsRegistered.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                arrangeUIMessage(aBoolean);
            }
        });

        patientHasRoom = userViewModel.getPatientHasRoom();
        patientHasRoom.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean)
                {
                    ivRoomIcon.setImageResource(R.drawable.copendoor_icon);
                } else {
                    ivRoomIcon.setImageResource(R.drawable.closedoor_icon);
                }
            }
        });

        llbtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isServiceStarted)
                {
                    stopWatchingMaxim();
                } else {
                    switchToMaxim();
                }
            }
        });

        llbtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userIsRegistered.getValue() != null && mCurrentUser != null)
                {
                    if (userIsRegistered.getValue())
                    {
                        logOutPatient();
                    }
                    else {
                        registerDocGuard();
                    }
                } else {
                    logInPatient();
                }
            }
        });

        llbtnRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //***Build the Add Room Dialog
                if (mCurrentUser != null)
                {
                    if (mCurrentUser.isEmailVerified())
                    {
                        if (userIsRegistered.getValue() != null && userIsRegistered.getValue())
                        {
                            if (patientHasRoom.getValue() != null && patientHasRoom.getValue())
                            {
                                Intent patientRoomIntent = new Intent(PatientMain.this, PatientRoomList.class);
                                patientRoomIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(patientRoomIntent);
                            } else {
                                Toast.makeText(PatientMain.this, "You haven't been accepted in any room.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(PatientMain.this, "Your are not registered as patient", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PatientMain.this, "Please verify your email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PatientMain.this, "You are not online.", Toast.LENGTH_SHORT).show();
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

        if (ContextCompat.checkSelfPermission(PatientMain.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(PatientMain.this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(PatientMain.this, new String [] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.SEND_SMS},
                    PERMISSION_CODE);
        }

        if (getIntent().getBooleanExtra(SERVICE_START, false))
        {
            isServiceStarted = getIntent().getBooleanExtra(SERVICE_START, false);
            if (isServiceStarted)
            {
                tvSwitch.setText("Stop Service");
            } else {
                tvSwitch.setText("Switch to MAXIM App");
            }
        }

        new GetEmergencyDetails(PatientMain.this).execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        if (requestCode == PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
            }
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(PatientMain.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(this);

                    dialog.setMessage("Please permit My.Heart Portal to read health-related data stored on this device.").
                            setTitle("Important permission required");
                    dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(PatientMain.this, new String [] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.SEND_SMS},
                                    PERMISSION_CODE);
                        }
                    });

                    dialog.setNegativeButton("NO!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(PatientMain.this, "Analysis of data will not work.", Toast.LENGTH_SHORT).show();

                        }
                    });
                    dialog.show();
                }
                else
                {
                    Toast.makeText(this, "You can permit the storage at this device's Settings. This dialog will never be shown again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(PatientMain.this,
                        Manifest.permission.SEND_SMS))
                {
                    androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(this);

                    dialog.setMessage("Please permit My.Heart Portal to send SMS in order to notify authorized persons in case of emergency.").
                            setTitle("Important permission required");
                    dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(PatientMain.this, new String [] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.SEND_SMS},
                                    PERMISSION_CODE);
                        }
                    });

                    dialog.setNegativeButton("NO!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(PatientMain.this, "Sending SMS will not work", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                }
                else
                {
                    Toast.makeText(this, "You can permit SMS at this device's Settings. This dialog will never be shown again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(PatientMain.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(PatientMain.this,
                        Manifest.permission.SEND_SMS))
                {
                    androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(this);

                    dialog.setMessage("Please permit My.Heart Portal to read health-related data and send SMS to authorized persons in case of emergency.").
                            setTitle("Important permission required");
                    dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(PatientMain.this, new String [] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.SEND_SMS},
                                    PERMISSION_CODE);
                        }
                    });

                    dialog.setNegativeButton("NO!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(PatientMain.this, "The App's SMS and data analysis will not work.", Toast.LENGTH_SHORT).show();

                        }
                    });
                    dialog.show();
                }
                else
                {
                    Toast.makeText(this, "You can permit this at this device's Settings. This dialog will never be shown again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void roomDialogBuild()
    {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.room_add_dialog, null);
        addRoomDialog = new AlertDialog.Builder(this).create();
        addRoomDialog.setView(view);
        addRoomDialog.setTitle("Add Room");
        addRoomDialog.setIcon(R.mipmap.myheart_logo_round);
        addRoomDialog.setCancelable(true);
        addRoomDialog.setMessage("Please enter the room code you got from your doctor.");

        addRoomDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SEARCH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing here, only instantiating the button
            }
        });

        addRoomDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PatientMain.this, "Cancelled.", Toast.LENGTH_SHORT).show();
            }
        });

        addRoomDialog.show();

        addRoomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Boolean wantToCloseDialog = false; //set false if you want the dialog to stay open when button is clicked.

                EditText etRoomCode = addRoomDialog.findViewById(R.id.etRoomCode);

                if (!etRoomCode.getText().toString().isEmpty())
                {
                    String room_code = etRoomCode.getText().toString().trim();
                    checkIfRoomExists(room_code, etRoomCode);

                } else{
                    Toast.makeText(PatientMain.this, "Please enter a code.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkIfRoomExists(final String room_code, final EditText etRoomCode)
    {
        mDatabase.child("Rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean roomExists = false;

                for (final DataSnapshot data : dataSnapshot.getChildren())
                {
                    if (data.hasChild(room_code))
                    {
                        boolean full = (boolean) data.child(room_code).child("full").getValue();
                        if (full)
                        {
                            Toast.makeText(PatientMain.this, "One patient per room only",
                                    Toast.LENGTH_SHORT).show();
                            etRoomCode.setError("The room already has a patient inside.");
                        }
                        else {
                            String doctor_id = (String) data.child(room_code).child("doctor_id").getValue();
                            if (!TextUtils.isEmpty(doctor_id))
                            {
                                mDatabase.child("ReadingsAccess").child(mCurrentUser.getUid()).child(doctor_id).setValue(true);

                                mDatabase.child("Users").child(mCurrentUser.getUid()).child("patient_details")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        String first_name, middle_name, last_name, date_of_birth;

                                        first_name = (String) dataSnapshot.child("first_name").getValue();
                                        middle_name = (String) dataSnapshot.child("middle_name").getValue();
                                        last_name = (String) dataSnapshot.child("last_name").getValue();
                                        date_of_birth = (String) dataSnapshot.child("date_of_birth").getValue();

                                        String fullName = last_name + ", " + first_name + " " + middle_name;

                                        Map<String, Object> patRequest = new HashMap<>();

                                        patRequest.put("patient", fullName);
                                        patRequest.put("date_of_birth", date_of_birth);
                                        data.child(room_code).child("patient_requests").child(mCurrentUser.getUid())
                                                .getRef().setValue(patRequest);

                                        addRoomDialog.dismiss();
                                        Toast.makeText(PatientMain.this, "Request to enter room sent.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        mDatabase.child("Users").child(mCurrentUser.getUid()).child("patient_details")
                                                .removeEventListener(this);
                                    }
                                });
                            } else {
                                Toast.makeText(PatientMain.this, "Cannot identify the room's doctor.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        roomExists = true;
                    }
                }
                if (!roomExists)
                {
                    Toast.makeText(PatientMain.this, "The room does not exists", Toast.LENGTH_SHORT).show();
                    etRoomCode.setError("The room may not exist. Also note that the code is case sensitive.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                mDatabase.child("Rooms").removeEventListener(this);
            }
        });
    }

    private void arrangeUIMessage(Boolean aBoolean)
    {
        if (mCurrentUser != null)
        {
            if (aBoolean)
            {
                tvConnect.setText("Log out");
            } else {
                tvConnect.setText("Register as patient");
            }
        }
        else {
            tvConnect.setText("Connect Online");
        }
    }

    private void registerDocGuard()
    {
        Intent docGuardIntent = new Intent(PatientMain.this, PatientRegister.class);
        docGuardIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(docGuardIntent);
    }

    private void stopWatchingMaxim()
    {
        isServiceStarted = false;
        tvSwitch.setText("Switch to MAXIM App");

        Intent watchIntent = new Intent(this, PatientBackWorker.class);
        stopService(watchIntent);
    }

    private void switchToMaxim()
    {
        isServiceStarted = true;
        tvSwitch.setText("Stop Service");

        Intent watchIntent = new Intent(this, PatientBackWorker.class);
        watchIntent.putExtra(PHONE_NUMBERS, patientSavedInfo);
        startService(watchIntent);

        try {
            Intent maximIntent = getPackageManager()
                    .getLaunchIntentForPackage("com.maximintegrated.hsp.maxrefdes101");
            startActivity(maximIntent);
        }catch (ActivityNotFoundException | NullPointerException e)
        {
            Toast.makeText(this, "MAXIM app is not installed.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private static class GetEmergencyDetails extends AsyncTask<Void, Void, String[]>
    {
        private WeakReference<PatientMain> patientMainWeakReference;
        private PatientDatabaseHelper patientDatabaseHelper;
        private String[] patientSavedInfo;

        GetEmergencyDetails (PatientMain patientMain)
        {
            patientMainWeakReference = new WeakReference<>(patientMain);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            PatientMain patientMain = patientMainWeakReference.get();
            if (patientMain == null || patientMain.isFinishing())
            {
                return;
            }
            this.patientDatabaseHelper = patientMain.patientDatabaseHelper;
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            Cursor result = patientDatabaseHelper.getAllData();

            if (result.getCount() == 0)
            {
                return null;
            }

            while (result.moveToNext())
            {
                String fName = result.getString(1);
                String mName = result.getString(2);
                String lName = result.getString(3);

                String patientName, phone1, phone2, phone3;
                if (!TextUtils.isEmpty(mName))
                {
                    patientName = lName + ", " + fName + " " + mName;
                } else {
                    patientName = lName + ", " + fName;
                }
                phone1 = result.getString(4);
                phone2 = result.getString(5);
                phone3 = result.getString(6);

                if (!TextUtils.isEmpty(phone1))
                {
                    if (!TextUtils.isEmpty(phone2))
                    {
                        patientSavedInfo = new String[]{patientName, phone1, phone2};

                    }
                    if (!TextUtils.isEmpty(phone2) && !TextUtils.isEmpty(phone3))
                    {
                        patientSavedInfo = new String[]{patientName, phone1, phone2, phone3};
                    }
                    else if (!TextUtils.isEmpty(phone2) || !TextUtils.isEmpty(phone3))
                    {
                        if (!TextUtils.isEmpty(phone2))
                        {
                            patientSavedInfo = new String[]{patientName, phone1, phone2};
                        }
                        else if (!TextUtils.isEmpty(phone3))
                        {
                            patientSavedInfo = new String[]{patientName, phone1, phone3};
                        }
                    } else {
                        patientSavedInfo = new String[]{patientName, phone1};
                    }
                }
            }
            return patientSavedInfo;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            PatientMain patientMain = patientMainWeakReference.get();
            if (patientMain == null || patientMain.isFinishing())
            {
                return;
            }
            patientMain.patientSavedInfo = strings;
        }
    }

    private static class ViewOfflineData extends AsyncTask<Void, Void, String>
    {
        private WeakReference<PatientMain> patientMainWeakReference;
        private PatientDatabaseHelper patientDatabaseHelper;

        ViewOfflineData (PatientMain patientMain)
        {
            patientMainWeakReference = new WeakReference<>(patientMain);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            PatientMain patientMain = patientMainWeakReference.get();
            if (patientMain == null || patientMain.isFinishing())
            {
                return;
            }
            this.patientDatabaseHelper = patientMain.patientDatabaseHelper;
        }

        @Override
        protected String doInBackground(Void... voids) {

            Cursor result = patientDatabaseHelper.getAllData();

            if (result.getCount() == 0)
            {
                return "Nothing found.";
            }

            StringBuilder builder = new StringBuilder();
            while (result.moveToNext())
            {
                builder.append("First name: ").append(result.getString(1)).append("\n");
                builder.append("Middle name: ");
                if (result.getString(2) != null)
                {
                    builder.append(result.getString(2)).append("\n");
                } else {
                    builder.append("\n");
                }
                builder.append("Last name: ").append(result.getString(3)).append("\n");
                builder.append("Contact's number: ").append(result.getString(4)).append("\n");
                builder.append("Contact's number: ");
                if (result.getString(5) != null)
                {
                    builder.append(result.getString(5)).append("\n");
                } else {
                    builder.append("\n");
                }
                builder.append("Contact's number: ");
                if (result.getString(6) != null)
                {
                    builder.append(result.getString(6)).append("\n");
                } else {
                    builder.append("\n");
                }
            }

            return builder.toString();
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            PatientMain patientMain = patientMainWeakReference.get();
            if (patientMain == null || patientMain.isFinishing())
            {
                return;
            }

            patientMain.showMessage("Saved Offline Data", string);
        }
    }

    private void showMessage (String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    private void logOutPatient()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton("Yes, let me go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mAuth.signOut();
                Toast.makeText(PatientMain.this, "Sign out successful.", Toast.LENGTH_SHORT).show();

                ivRoomIcon.setImageResource(R.drawable.closedoor_icon);
                tvConnect.setText("Connect Online");
            }
        });
        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle("Are you sure?");
        builder.setMessage("You will be signed out from My.Heart Portal.");
        builder.show();
    }

    private void logInPatient()
    {
        Intent patientIntent = new Intent(PatientMain.this, LogInChooser.class);
        patientIntent.putExtra(USER_TYPE, "patient");
        patientIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(patientIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.patient_main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if (item.getItemId() == R.id.action_view_offline)
        {
            new ViewOfflineData(PatientMain.this).execute();
        }

        if (item.getItemId() == R.id.action_update)
        {
            Intent updateIntent = new Intent(PatientMain.this, PatientDetails.class);
            updateIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(updateIntent);
        }

        if (item.getItemId() == R.id.action_add)
        {
            //***Build the Add Room Dialog
            if (mCurrentUser != null)
            {
                mCurrentUser.reload();

                if (mCurrentUser.isEmailVerified())
                {
                    if (userIsRegistered.getValue() != null && userIsRegistered.getValue()) {

                        roomDialogBuild();
                    }
                    else {
                        Toast.makeText(this, "You are not registered as patient", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please verify your email.", Toast.LENGTH_SHORT).show();
                }
            } else {
            Toast.makeText(this, "Please register as patient.", Toast.LENGTH_SHORT).show();
            }
        }

        if (item.getItemId() == R.id.action_logout)
        {
            logOutPatient();
        }

        if (item.getItemId() == R.id.action_view_profile)
        {
            if (mCurrentUser != null && mCurrentUser.isEmailVerified())
            {
                if (userIsRegistered.getValue() != null && userIsRegistered.getValue())
                {
                    Intent viewIntent = new Intent(PatientMain.this, PatientProfileView.class);
                    viewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(viewIntent);
                }
            }
        }

        if (item.getItemId() == R.id.action_resend)
        {
            if (mCurrentUser != null)
            {
                if (!mCurrentUser.isEmailVerified())
                {
                    mCurrentUser.sendEmailVerification();
                    Toast.makeText(this, "Email verification is re-sent. Please check your email-box.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Your email is already verified.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Login first before asking for a resend request.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
