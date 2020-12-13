package com.example.myheartportal;

public class PointValueEncHRM {

    private String encryptedHeartRate; //index 6 --> get highest
    private String encryptedHRConfidence; //index 7
    private String encryptedActivity; //index 8

    public PointValueEncHRM() {
    }

    public PointValueEncHRM(String encryptedHeartRate, String encryptedHRConfidence, String encryptedActivity) {
        this.encryptedHeartRate = encryptedHeartRate;
        this.encryptedHRConfidence = encryptedHRConfidence;
        this.encryptedActivity = encryptedActivity;
    }

    public String getEncryptedHeartRate() {
        return encryptedHeartRate;
    }

    public String getEncryptedHRConfidence() {
        return encryptedHRConfidence;
    }

    public String getEncryptedActivity() {
        return encryptedActivity;
    }
}
