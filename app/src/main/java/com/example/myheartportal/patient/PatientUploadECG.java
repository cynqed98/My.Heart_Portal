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

import com.example.myheartportal.PointValueEncECG;
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

public class PatientUploadECG extends Worker
{
    //***VARIABLES
    public final String tag = this.getClass().getSimpleName();
    private String [] infoData;
    //***FIREBASE
    private DatabaseReference mDatabaseReadings;

    public PatientUploadECG(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
            double high_rr = 0.0;
            String high_rrtime = "";
            double threshhold_RR = 120.0; //tentative value
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] tokens = line.split(","); //split by commas

                if (Double.parseDouble(tokens[6]) >= high_rr) //Get highest RR
                {
                    high_rr = Double.parseDouble(tokens[6]);
                    high_rrtime = tokens[0];

                    if (high_rr >= threshhold_RR)
                    {
                        outputData = new Data.Builder().putBoolean(TRIGGER_SMS, true).build();
                    }
                }

                if (!TextUtils.isEmpty(patient_id)) //Upload ECG
                {
                    uploadECG (tokens, fileName, high_rr, high_rrtime, row, patient_id);
                }

                //************************ SKIP
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
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
    private void uploadECG(String[] tokens, String fileTitle, double high_rr, String high_rrtime, int row, String encpassword)
    {
        DatabaseReference mDatabaseECG = mDatabaseReadings.child("ECG").child(fileTitle);

        String encdate = null, encfiltered = null, encraw = null, encaverage = null, enccurrent = null;
        SecureEncryption secureEncryption = new SecureEncryption();

        try {
            encdate = secureEncryption.encryptData(tokens[0], encpassword);
            encfiltered = secureEncryption.encryptData(tokens[2], encpassword);
            encraw = secureEncryption.encryptData(tokens[3], encpassword);
            encaverage = secureEncryption.encryptData(tokens[6], encpassword);
            enccurrent = secureEncryption.encryptData(tokens[7], encpassword);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
        }

        PointValueEncECG pointValueEncECG = new PointValueEncECG(encdate, encraw, encfiltered, encaverage, enccurrent);

        mDatabaseECG.child("ecgdata").child(String.valueOf(row)).setValue(pointValueEncECG);

        String enchighrr = null, encrrtime = null;
        try {
            enchighrr = secureEncryption.encryptData(String.valueOf(high_rr), encpassword);
            encrrtime = secureEncryption.encryptData(high_rrtime, encpassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDatabaseECG.child("high_rr").child("rr").setValue(enchighrr); //For Highest R-to-R
        mDatabaseECG.child("high_rr").child("time").setValue(encrrtime); //For the time of highest R-to-R

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
