package com.example.myheartportal;

public class ModelRooms
{
    private String doctor_id;
    private String patient_id;
    private String room_code;
    private String room_name;
    private boolean full;

    public ModelRooms(String room_name, String patient_id, boolean full, String room_code, String doctor_id) {
        this.room_name = room_name;
        this.patient_id = patient_id;
        this.full = full;
        this.room_code = room_code;
        this.doctor_id = doctor_id;
    }

    public ModelRooms() {
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public String getRoom_code() {
        return room_code;
    }

    public void setRoom_code(String room_code) {
        this.room_code = room_code;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }
}
