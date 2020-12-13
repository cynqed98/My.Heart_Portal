package com.example.myheartportal.patient;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.myheartportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.UUID;

import static com.example.myheartportal.Constants.INFO_DATA;
import static com.example.myheartportal.Constants.PHONE_NUMBERS;
import static com.example.myheartportal.Constants.SERVICE_START;
import static com.example.myheartportal.Constants.TRIGGER_SMS;
import static com.example.myheartportal.MyHeartPortalApp.CHANNEL_ID;

public class PatientBackWorker extends LifecycleService implements LifecycleOwner {

    //***WORKMANAGER
    private OneTimeWorkRequest ecgWorkRequest, hrmWorkRequest, tempWorkRequest;
    //***NOTIFICATION
    Notification notification;
    //***FILE OBSERVER
    public static FileObserver fileCreateObserver;
    //***PENDINGINTENT
    PendingIntent pendingIntent;
    //***FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    //***VARIABLES
    private final String tag = this.getClass().getSimpleName();
    public final String pathToWatch = android.os.Environment.getExternalStorageDirectory().toString() + "/MaximHSP/";
    public String fileName;
    //***SENDSMS
    SmsManager smsManager;
    private String[] phoneNumbers;

    public PatientBackWorker() {

    }

    @Override
    public IBinder onBind(@NonNull Intent intent)
    {
        // TODO: Return the communication channel to the service.
        super.onBind(intent);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        smsManager = SmsManager.getDefault();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        phoneNumbers = intent.getStringArrayExtra(PHONE_NUMBERS); //name then phone nos.

        Intent notificationIntent = new Intent(this, PatientMain.class)
                .putExtra(SERVICE_START, true)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this,
                UUID.randomUUID().hashCode(),
                notificationIntent.putExtra(SERVICE_START, true),
                PendingIntent.FLAG_UPDATE_CURRENT);

        mCurrentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = firebaseAuth.getCurrentUser();

                if (firebaseAuth.getCurrentUser() != null) {
                    firebaseAuth.getCurrentUser().reload();

                    displayServiceNotification("Started observing the patient's data.");
                } else {

                    displayServiceNotification("User is not online. Only SMS feature works.");
                }
                startForeground(1, notification);
            }
        });

        startWatching();

        return START_STICKY;
    }

    private void displayServiceNotification(String contentText)
    {
        notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
        .setContentTitle("My.Heart Portal")
        .setContentText(contentText)
        .setSmallIcon(R.mipmap.myheart_logo_round)
        .setContentIntent(pendingIntent)
        .build();
    }

    private void startWatching()
    {
        watchWorkInfoLiveData();

        fileCreateObserver = new FileObserver(pathToWatch) {
            @Override
            public void onEvent(int event, @Nullable String path) {

                if (event == FileObserver.MODIFY || event == FileObserver.CLOSE_WRITE)
                {
                    if (path != null)
                    {
                        fileName = path;
                        Data infoData;

                        if (fileName.toLowerCase().startsWith("ecg"))
                        {
                            if (mCurrentUser != null)
                            {
                                infoData = new Data.Builder().putStringArray(INFO_DATA,
                                        new String[]{pathToWatch, fileName, mCurrentUser.getUid()}).build();
                            } else {
                                infoData = new Data.Builder().putStringArray(INFO_DATA,
                                        new String[]{pathToWatch, fileName, ""}).build();
                            }

                            ecgWorkRequest = new OneTimeWorkRequest.Builder(PatientUploadECG.class)
                                    .setInputData(infoData).build();

                            WorkManager.getInstance(getApplicationContext())
                                    .enqueueUniqueWork("ECGUniqueWork",
                                            ExistingWorkPolicy.REPLACE,
                                            ecgWorkRequest);
                        }
                        else if (fileName.toLowerCase().startsWith("hrm"))
                        {
                            if (mCurrentUser != null)
                            {
                                infoData = new Data.Builder().putStringArray(INFO_DATA,
                                        new String[]{pathToWatch, fileName, mCurrentUser.getUid()}).build();
                            } else {
                                infoData = new Data.Builder().putStringArray(INFO_DATA,
                                        new String[]{pathToWatch, fileName, ""}).build();
                            }

                            hrmWorkRequest = new OneTimeWorkRequest.Builder(PatientUploadHRM.class)
                                    .setInputData(infoData).build();

                            WorkManager.getInstance(getApplicationContext())
                                    .enqueueUniqueWork("HRMUniqueWork",
                                            ExistingWorkPolicy.REPLACE,
                                            hrmWorkRequest);
                        }
                        else if (fileName.toLowerCase().startsWith("tem"))
                        {
                            if (mCurrentUser != null)
                            {
                                infoData = new Data.Builder().putStringArray(INFO_DATA,
                                        new String[]{pathToWatch, fileName, mCurrentUser.getUid()}).build();
                            } else {
                                infoData = new Data.Builder().putStringArray(INFO_DATA,
                                        new String[]{pathToWatch, fileName, ""}).build();
                            }

                            tempWorkRequest = new OneTimeWorkRequest.Builder(PatientUploadTEMP.class)
                                    .setInputData(infoData).build();

                            WorkManager.getInstance(getApplicationContext())
                                    .enqueueUniqueWork("TEMPUniqueWork",
                                            ExistingWorkPolicy.REPLACE,
                                            tempWorkRequest);
                        }
                    }
                }
            }
        };
        fileCreateObserver.startWatching();
    }

    private void watchWorkInfoLiveData()
    {
        WorkManager.getInstance(getBaseContext())
        .getWorkInfosForUniqueWorkLiveData("ECGUniqueWork")
        .observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {

                for (WorkInfo workInfo : workInfos)
                {
                    if (workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.SUCCEEDED)
                    {
                        if (workInfo.getOutputData().getBoolean(TRIGGER_SMS, false))
                        {
                            WorkManager.getInstance(getBaseContext()).pruneWork();

                            if (phoneNumbers != null) //EMERGENCY SMS ALERT
                            {
                                for (int i = 1; i < phoneNumbers.length; i++)
                                {
                                    String message = "The average R-to-R of patient " + phoneNumbers[0] +
                                            " has exceeded the threshold value.";
                                    smsManager.sendTextMessage(phoneNumbers[i], null, message,
                                            null, null);
                                }
                            }
                        }
                    }
                }
            }
        });

        WorkManager.getInstance(getBaseContext())
        .getWorkInfosForUniqueWorkLiveData("HRMUniqueWork")
        .observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {

                for (WorkInfo workInfo : workInfos)
                {
                    if (workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.SUCCEEDED)
                    {
                        if (workInfo.getOutputData().getBoolean(TRIGGER_SMS, false))
                        {
                            WorkManager.getInstance(getBaseContext()).pruneWork();

                            if (phoneNumbers != null) //EMERGENCY SMS ALERT
                            {
                                for (int i = 1; i < phoneNumbers.length; i++)
                                {
                                    String message = "The heart-rate of patient " + phoneNumbers[0] +
                                            " has exceeded the threshold value.";
                                    smsManager.sendTextMessage(phoneNumbers[i], null, message,
                                            null, null);
                                }
                            }
                        }
                    }
                }
            }
        });

        WorkManager.getInstance(getBaseContext())
        .getWorkInfosForUniqueWorkLiveData("TEMPUniqueWork")
        .observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {

                for (WorkInfo workInfo : workInfos)
                {
                    if (workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.SUCCEEDED)
                    {
                        if (workInfo.getOutputData().getBoolean(TRIGGER_SMS, false))
                        {
                            WorkManager.getInstance(getBaseContext()).pruneWork();

                            if (phoneNumbers != null) //EMERGENCY SMS ALERT
                            {
                                for (int i = 1; i < phoneNumbers.length; i++)
                                {
                                    String message = "The temperature of patient " + phoneNumbers[0] +
                                            " has exceeded the threshold value.";
                                    smsManager.sendTextMessage(phoneNumbers[i], null, message,
                                            null, null);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        fileCreateObserver.stopWatching();
    }
}
