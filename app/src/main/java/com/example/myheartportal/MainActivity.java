package com.example.myheartportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myheartportal.patient.PatientDatabaseHelper;
import com.example.myheartportal.patient.PatientDetails;
import com.example.myheartportal.patient.PatientMain;

import static com.example.myheartportal.Constants.PATIENT_ID;
import static com.example.myheartportal.Constants.USER_TYPE;

public class MainActivity extends AppCompatActivity {

    private PatientDatabaseHelper patientDatabaseHelper;
    private int logoClicks = 0;
    private final String tag = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        patientDatabaseHelper = new PatientDatabaseHelper(this);

        LinearLayout llbtnDoctor = findViewById(R.id.llbtnDoctor);
        LinearLayout llbtnPatient = findViewById(R.id.llbtnPatient);
        LinearLayout llbtnGuardian = findViewById(R.id.llbtnGuardian);
        ImageView ivLogo = findViewById(R.id.ivLogo);


        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (logoClicks == 0)
                {
                    Toast.makeText(MainActivity.this, "Welcome to My.Heart Portal", Toast.LENGTH_SHORT).show();
                    logoClicks = logoClicks + 1;
                }
                else if (logoClicks == 1)
                {
                    Toast.makeText(MainActivity.this, "My.Heart wishes you a wonderful day.", Toast.LENGTH_SHORT).show();
                    logoClicks = 0;
                }
            }
        });

        llbtnPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkHasSavedData();
            }
        });

        llbtnDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent doctorIntent = new Intent(getBaseContext(), LogInChooser.class);
                doctorIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                doctorIntent.putExtra(USER_TYPE, "doctor");
                startActivity(doctorIntent);
            }
        });

        llbtnGuardian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent guardianIntent = new Intent(getBaseContext(), LogInChooser.class);
                guardianIntent.putExtra(USER_TYPE, "guardian");
                startActivity(guardianIntent);
            }
        });
    }

    private void checkHasSavedData()
    {
        Cursor result = patientDatabaseHelper.getAllData();

        if (result.getCount() == 0) {

            Intent patientIntent = new Intent(getBaseContext(), PatientDetails.class);
            startActivity(patientIntent);

        } else {
            Intent patientIntent = new Intent(getBaseContext(), PatientMain.class);
            startActivity(patientIntent);
        }
    }
}
