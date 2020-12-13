package com.example.myheartportal;

public class ModelRequests
{
    private String date_of_birth;
    private String patient;
    private String guardian;

    public ModelRequests() {
    }

    public ModelRequests(String date_of_birth, String patient, String guardian) {
        this.date_of_birth = date_of_birth;
        this.patient = patient;
        this.guardian = guardian;
    }

    public String getGuardian() {
        return guardian;
    }

    public void setGuardian(String guardian) {
        this.guardian = guardian;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }
}
