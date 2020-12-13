/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myheartportal;

public final class Constants {

    public static final String REQUEST_TYPE = "requestType";
    public static final String ROOM_INFO = "roomInfo"; //{user_type, doctor_id, room_code, room_name}
    public static final String HISTORY_FILE = "historyFile";
    public static final String PATIENT_ID = "patientId";

    public static final String USER_TYPE = "userType";
    public static final String ROOM_CODE = "roomCode";
    public static final String ROOM_NAME = "roomName";
    public static final String DOCTOR_ID = "doctorId";

    public static final String PHONE_NUMBERS = "emergencyPhoneNumbers";
    public static final String SERVICE_START = "serviceStarted";

    public static final String INFO_DATA = "infoData";
    public static final String TRIGGER_SMS = "triggerSMS";

    public static final String CHART_WORKER = "chartWorker";

    private Constants() {} // Ensures this class is never instantiated
}