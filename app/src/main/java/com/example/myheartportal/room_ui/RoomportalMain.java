package com.example.myheartportal.room_ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.example.myheartportal.R;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import static com.example.myheartportal.Constants.DOCTOR_ID;
import static com.example.myheartportal.Constants.HISTORY_FILE;
import static com.example.myheartportal.Constants.PATIENT_ID;
import static com.example.myheartportal.Constants.REQUEST_TYPE;
import static com.example.myheartportal.Constants.ROOM_CODE;
import static com.example.myheartportal.Constants.ROOM_NAME;
import static com.example.myheartportal.Constants.USER_TYPE;
import static com.example.myheartportal.Constants.ROOM_INFO;

public class RoomportalMain extends AppCompatActivity {

    //***SECTIONS-PAGER-ADAPTER
    private SectionsPagerAdapter sectionsPagerAdapter;
    //***LAYOUTS
    private CustomViewPager view_pager;
    private TabLayout tabs;
    //***FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //***VARIABLES
    private final String tag = this.getClass().getSimpleName();
    private String [] roomInfoList;
    private String user_type, doctor_id, room_code, room_name, history_file, patient_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomportal_main);

        Utils.init(this);

        user_type = getIntent().getStringExtra(USER_TYPE);
        doctor_id = getIntent().getStringExtra(DOCTOR_ID);
        room_code = getIntent().getStringExtra(ROOM_CODE);
        room_name = getIntent().getStringExtra(ROOM_NAME);
        history_file = getIntent().getStringExtra(HISTORY_FILE);
        patient_id = getIntent().getStringExtra(PATIENT_ID);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = firebaseAuth.getCurrentUser();
            }
        };

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), 0);
        view_pager = findViewById(R.id.view_pager);
        view_pager.setPagingEnabled(false);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(view_pager);

        roomInfoList = new String[] {user_type, doctor_id, room_code, room_name, history_file};
        setupViewPage(sectionsPagerAdapter, view_pager, roomInfoList);
        view_pager.setOffscreenPageLimit(3);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        mCurrentUser = mAuth.getCurrentUser();
    }

    private void setupViewPage(SectionsPagerAdapter sectionsPagerAdapter, ViewPager view_pager, String[] roomInfoList)
    {
        Bundle bundle = new Bundle();
        bundle.putStringArray(ROOM_INFO, roomInfoList);

        TabProfile tabProfile = new TabProfile();
        tabProfile.setArguments(bundle);

        TabEcg tabEcg = new TabEcg();
        tabEcg.setArguments(bundle);

        TabHrm tabHrm = new TabHrm();
        tabHrm.setArguments(bundle);

        TabTemp tabTemp = new TabTemp();
        tabTemp.setArguments(bundle);

        sectionsPagerAdapter.addFragment(tabProfile, "Details");
        sectionsPagerAdapter.addFragment(tabEcg, "ECG");
        sectionsPagerAdapter.addFragment(tabHrm, "HRM");
        sectionsPagerAdapter.addFragment(tabTemp, "TEMP");

        view_pager.setAdapter(sectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        switch (user_type) {
            case "doctor":
                getMenuInflater().inflate(R.menu.doctor_portal_menu, menu);
                break;
            case "patient":
                getMenuInflater().inflate(R.menu.patient_portal_menu, menu);
                break;
            case "guardian":
                getMenuInflater().inflate(R.menu.guardian_portal_menu, menu);
                break;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.action_patient_request)
        {
            Intent patientRequestIntent = new Intent(RoomportalMain.this, RoomRequests.class);
            patientRequestIntent.putExtra(REQUEST_TYPE, "patient_request");
            patientRequestIntent.putExtra(USER_TYPE, user_type);
            patientRequestIntent.putExtra(DOCTOR_ID, doctor_id);
            patientRequestIntent.putExtra(ROOM_CODE, room_code);
            patientRequestIntent.putExtra(ROOM_NAME, room_name);
            patientRequestIntent.putExtra(PATIENT_ID, patient_id);
            patientRequestIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(patientRequestIntent);
        }

        if (item.getItemId() == R.id.action_guardian_request)
        {
            Intent guardianRequestIntent = new Intent(RoomportalMain.this, RoomRequests.class);
            guardianRequestIntent.putExtra(REQUEST_TYPE, "guardian_request");
            guardianRequestIntent.putExtra(USER_TYPE, user_type);
            guardianRequestIntent.putExtra(DOCTOR_ID, doctor_id);
            guardianRequestIntent.putExtra(ROOM_CODE, room_code);
            guardianRequestIntent.putExtra(ROOM_NAME, room_name);
            guardianRequestIntent.putExtra(PATIENT_ID, patient_id);
            guardianRequestIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(guardianRequestIntent);
        }

        if (item.getItemId() == R.id.action_refresh)
        {
            Intent refreshIntent = new Intent(RoomportalMain.this, RoomportalMain.class);
            refreshIntent.putExtra(USER_TYPE, user_type);
            refreshIntent.putExtra(DOCTOR_ID, doctor_id);
            refreshIntent.putExtra(ROOM_CODE, room_code);
            refreshIntent.putExtra(ROOM_NAME, room_name);
            refreshIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(refreshIntent);
            finish();
        }

        if (item.getItemId() == R.id.action_ecghistory)
        {
            Intent ecgIntent = new Intent(RoomportalMain.this, RoomHistoryECG.class);
            ecgIntent.putExtra(USER_TYPE, user_type);
            ecgIntent.putExtra(DOCTOR_ID, doctor_id);
            ecgIntent.putExtra(ROOM_CODE, room_code);
            ecgIntent.putExtra(ROOM_NAME, room_name);
            ecgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(ecgIntent);
        }

        if (item.getItemId() == R.id.action_hrmhistory)
        {
            Intent hrmIntent = new Intent(RoomportalMain.this, RoomHistoryHRM.class);
            hrmIntent.putExtra(USER_TYPE, user_type);
            hrmIntent.putExtra(DOCTOR_ID, doctor_id);
            hrmIntent.putExtra(ROOM_CODE, room_code);
            hrmIntent.putExtra(ROOM_NAME, room_name);
            hrmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(hrmIntent);
        }

        if (item.getItemId() == R.id.action_temphistory)
        {
            Intent tempIntent = new Intent(RoomportalMain.this, RoomHistoryTEMP.class);
            tempIntent.putExtra(USER_TYPE, user_type);
            tempIntent.putExtra(DOCTOR_ID, doctor_id);
            tempIntent.putExtra(ROOM_CODE, room_code);
            tempIntent.putExtra(ROOM_NAME, room_name);
            tempIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(tempIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}