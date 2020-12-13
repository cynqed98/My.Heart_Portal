package com.example.myheartportal;

public class ModelHistoryECG
{
    private String fileName;

    public ModelHistoryECG() {
    }

    public ModelHistoryECG(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
