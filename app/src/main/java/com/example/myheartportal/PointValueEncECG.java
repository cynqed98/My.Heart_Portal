package com.example.myheartportal;

public class PointValueEncECG {

    private String xTime; //index 0
    private String encryptedFilter; //index 2
    private String encryptedRaw; //index 3
    private String encryptedAve; //index 6
    private String encryptedCurrent; //index 7

    public PointValueEncECG() {
    }

    public PointValueEncECG(String xTime, String enc_raw_ecg, String enc_filtered_ecg, String enc_average_rr, String enc_current_rr) {
        this.xTime = xTime;
        this.encryptedRaw = enc_raw_ecg;
        this.encryptedFilter = enc_filtered_ecg;
        this.encryptedAve = enc_average_rr;
        this.encryptedCurrent = enc_current_rr;
    }

    public String getEncryptedRaw() {
        return encryptedRaw;
    }

    public String getEncryptedFilter() {
        return encryptedFilter;
    }

    public String getEncryptedAve() {
        return encryptedAve;
    }

    public String getEncryptedCurrent() {
        return encryptedCurrent;
    }

    public String getxTime() {
        return xTime;
    }
}
