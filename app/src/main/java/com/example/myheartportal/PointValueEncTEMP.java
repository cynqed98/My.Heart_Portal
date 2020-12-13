package com.example.myheartportal;

public class PointValueEncTEMP {

    private String xTime; //index 0
    private String encryptedTemperature; //index 2

    public PointValueEncTEMP() {
    }

    public PointValueEncTEMP(String xTime, String encryptedTemperatrue) {
        this.xTime = xTime;
        this.encryptedTemperature = encryptedTemperatrue;
    }

    public String getEncryptedTemperature() {
        return encryptedTemperature;
    }

    public String getxTime() {
        return xTime;
    }
}
