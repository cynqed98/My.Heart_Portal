package com.example.myheartportal.patient;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myheartportal.PointValueEncTEMP;
import com.example.myheartportal.R;
import com.example.myheartportal.SecureEncryption;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.example.myheartportal.Constants.INFO_DATA;
import static com.example.myheartportal.Constants.TRIGGER_SMS;

public class PatientUploadTEMP extends Worker
{
    //***VARIABLES
    public final String tag = this.getClass().getSimpleName();
    private String [] infoData;
    //***FIREBASE
    private DatabaseReference mDatabaseReadings;

    public PatientUploadTEMP(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        infoData = getInputData().getStringArray(INFO_DATA); //path, name, patientid
        if (infoData != null)
        {
            displayFileNameNotification("My.Heart Portal", "Analyzing: " + infoData[1]);
            if (!TextUtils.isEmpty(infoData[2]))
            {
                mDatabaseReadings = FirebaseDatabase.getInstance().getReference().child("Readings").child(infoData[2]);
            }
        }
    }

    @NonNull
    @Override
    public Result doWork()
    {
        if (infoData != null)
        {
            infoData[1] = infoData[1].replace(".tmp", "");
            readAndUpload(infoData[0], infoData[1], infoData[2]);
        }

        if (outputData != null)
        {
            return Result.success(outputData);
        } else {
            return Result.success();
        }
    }

    private Data outputData;

    private void readAndUpload(String filePath, String fileName, String patient_id)
    {
        Log.d(tag, "entered tem");

        File file = new File(filePath, fileName);
        fileName = fileName.substring(0, fileName.indexOf("."));

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            inputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            int row = 1;
            bufferedReader.readLine(); //SKIP ONE STEP
            bufferedReader.readLine(); //SKIP ONE STEP
            String line = "";
            double high_temp = 0.0;
            String high_temptime = "";
            double threshold_temp = 40.0;
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] tokens = line.split(","); //split by commas

                if (Double.parseDouble(tokens[2]) >= high_temp) //Get highest temp
                {
                    high_temp = Double.parseDouble(tokens[2]);
                    high_temptime = tokens[0];

                    if (high_temp >= threshold_temp)
                    {
                        outputData = new Data.Builder().putBoolean(TRIGGER_SMS, true).build();
                    }
                }

                if (!TextUtils.isEmpty(patient_id)) //upload data
                {
                    uploadTEMP(tokens, fileName, high_temp, high_temptime, row, patient_id);
                }

                //************************ SKIP
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
                //************************ SKIP

                row = row + 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null)
            {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null)
            {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null)
            {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dataUploaded)
            {
                dataUploaded = false;
            }
        }


    }

    private boolean dataUploaded = false;
    private void uploadTEMP(String[] tokens, String fileTitle, double high_temp, String high_temptime, int row, String encpassword)
    {
        DatabaseReference mDatabaseTEMP = mDatabaseReadings.child("TEMP").child(fileTitle);

        String encdate = null, enctemp = null;
        SecureEncryption secureEncryption = new SecureEncryption();

        try {
            encdate = secureEncryption.encryptData(tokens[0], encpassword);
            enctemp = secureEncryption.encryptData(tokens[2], encpassword);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
        }

        PointValueEncTEMP pointValueEncTEMP = new PointValueEncTEMP(encdate, enctemp);

        mDatabaseTEMP.child("tempdata").child(String.valueOf(row)).setValue(pointValueEncTEMP);

        String encHighTemp = null, encHighTempTime = null;
        try {
            encHighTemp = secureEncryption.encryptData(String.valueOf(high_temp), encpassword);
            encHighTempTime = secureEncryption.encryptData(high_temptime, encpassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDatabaseTEMP.child("high_temp").child("temp").setValue(encHighTemp);
        mDatabaseTEMP.child("high_temp").child("time").setValue(encHighTempTime);

        if (!dataUploaded)
        {
            dataUploaded = true;
            displayUploadNotification("My.Heart Portal", "Connected to Server.");
        }
    }

    private void displayFileNameNotification(String title, String task)
    {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("MyHeartFileName",
                    "MyHeartFileName",
                    NotificationManager.IMPORTANCE_DEFAULT);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(),
                "MyHeartFileName")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.myheart_logo_round);

        assert notificationManager != null;
        notificationManager.notify(3, notification.build());
    }

    private void displayUploadNotification(String title, String task)
    {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("MyHeartUpload",
                    "MyHeartUpload",
                    NotificationManager.IMPORTANCE_DEFAULT);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(),
                "MyHeartUpload")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.myheart_logo_round);

        assert notificationManager != null;
        notificationManager.notify(2, notification.build());
    }
}
