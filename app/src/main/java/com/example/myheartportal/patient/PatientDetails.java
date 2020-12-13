package com.example.myheartportal.patient;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.example.myheartportal.R;

import java.lang.ref.WeakReference;

public class PatientDetails extends AppCompatActivity {

    //***LAYOUTS
    private EditText etFirstName, etMiddleName, etLastName;
    private ImageView ivAddPhone, ivRemovePhone;
    private Button btnProceed;
    private EditText edittTxt;
    private EditText etPhone0;
    private LinearLayout llvPhoneNumbers;
    //***SQLITE
    private PatientDatabaseHelper patientDatabaseHelper;
    private Cursor result;
    //***VARIABLES
    private final int currentUserUid = 53;
    private final int id1 = 1;
    private final int id2 = 2;
    private int hint = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        patientDatabaseHelper = new PatientDatabaseHelper(this);
        result = patientDatabaseHelper.getAllData();

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etPhone0 = findViewById(R.id.etPhone0);
        ivAddPhone = findViewById(R.id.ivAddPhone);
        ivRemovePhone = findViewById(R.id.ivRemovePhone);
        btnProceed = findViewById(R.id.btnProceed);
        llvPhoneNumbers = findViewById(R.id.llvPhoneNumbers);

        ivAddPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hint >= 0 && hint < 2) {

                    createEditTextView();

                } else {
                    Toast.makeText(PatientDetails.this, "Maximum amount of contacts reached."
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivRemovePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hint > 0 && hint <= 2) {

                    removeEditTextView();

                } else {
                    Toast.makeText(PatientDetails.this, "Minimum amount of contacts reached."
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String first_name = etFirstName.getText().toString().trim();
                String middle_name = null;
                if (!etMiddleName.getText().toString().isEmpty())
                {
                    middle_name = etMiddleName.getText().toString().trim();
                }
                String last_name = etLastName.getText().toString().trim();
                String contact_number1 = etPhone0.getText().toString().trim();
                String contact_number2 = null;
                String contact_number3 = null;
                if (hint > 0)
                {
                    EditText phone2 = llvPhoneNumbers.findViewById(id1);
                    contact_number2 = phone2.getText().toString().trim();

                    if (hint == 2) {
                        EditText phone3 = llvPhoneNumbers.findViewById(id2);
                        contact_number3 = phone3.getText().toString().trim();
                    }
                }

                new UpdatePatientUser(PatientDetails.this).execute(first_name, middle_name, last_name
                        , contact_number1, contact_number2, contact_number3);

            }
        });
    }

    private static class UpdatePatientUser extends AsyncTask <String, Void, Boolean>
    {
        private WeakReference<PatientDetails> patientDetailsWeakReference;
        Cursor results;
        PatientDatabaseHelper patientDatabaseHelper;
        private int currentUserUid;

        UpdatePatientUser (PatientDetails patientDetails)
        {
            patientDetailsWeakReference = new WeakReference<>(patientDetails);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            PatientDetails patientDetails = patientDetailsWeakReference.get();
            if (patientDetails == null || patientDetails.isFinishing())
            {
                return;
            }
            this.results = patientDetails.result;
            this.patientDatabaseHelper = patientDetails.patientDatabaseHelper;
            this.currentUserUid = patientDetails.currentUserUid;
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            if (!strings[0].isEmpty() && !strings[2].isEmpty() && !strings[3].isEmpty()) {

                if (results.getCount() == 0)
                {
                    patientDatabaseHelper.insertData(currentUserUid, strings[0], strings[1], strings[2]
                            , strings[3], strings[4], strings[5]);
                }
                else {
                    patientDatabaseHelper.updateData(currentUserUid, strings[0], strings[1], strings[2]
                            , strings[3], strings[4], strings[5]);
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            PatientDetails patientDetails = patientDetailsWeakReference.get();
            if (patientDetails == null || patientDetails.isFinishing())
            {
                return;
            }

            if (aBoolean)
            {
                Intent saveDataIntent = new Intent(patientDetails, PatientMain.class);
                saveDataIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                patientDetails.startActivity(saveDataIntent);
                Toast.makeText(patientDetails, "Saved Patient Data", Toast.LENGTH_SHORT).show();
                patientDetails.finish();
            }
            else {
                Toast.makeText(patientDetails, "Please enter all necessary fields", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeEditTextView()
    {
        EditText removeEditText = llvPhoneNumbers.findViewById(hint);
        llvPhoneNumbers.removeView(removeEditText);
        hint = hint - 1;
    }

    private void createEditTextView()
    {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins(0, 10, 0, 10);

        edittTxt = new EditText(this);
        hint = hint + 1;
        edittTxt.setHint("Enter phone number");
        edittTxt.setHintTextColor(Color.GRAY);
        edittTxt.setLayoutParams(params);
        edittTxt.setInputType(InputType.TYPE_CLASS_PHONE);
        edittTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        edittTxt.setId(hint);

        llvPhoneNumbers.addView(edittTxt);
    }
}
