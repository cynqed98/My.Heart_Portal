package com.example.myheartportal.room_ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myheartportal.ModelRequests;
import com.example.myheartportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.example.myheartportal.Constants.DOCTOR_ID;
import static com.example.myheartportal.Constants.PATIENT_ID;
import static com.example.myheartportal.Constants.REQUEST_TYPE;
import static com.example.myheartportal.Constants.ROOM_CODE;
import static com.example.myheartportal.Constants.ROOM_NAME;
import static com.example.myheartportal.Constants.USER_TYPE;

public class RoomRequests extends AppCompatActivity {

    //***LAYOUTS
    private RecyclerView rvDocRequest;
    private TextView tvRequests;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabasePatientRequests;
    private DatabaseReference mDatabaseGuardianRequests;
    private FirebaseRecyclerOptions<ModelRequests> requestOptions;
    private FirebaseRecyclerAdapter<ModelRequests, RequestViewHolder> requestRecyclerAdapter;
    //***VARIABLES
    private String tag = this.getClass().getSimpleName();
    private String request_type;
    private String user_type;
    private String doctor_id;
    private String room_code;
    private String room_name;
    private String patient_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_enter);

        request_type = getIntent().getStringExtra(REQUEST_TYPE);
        user_type = getIntent().getStringExtra(USER_TYPE);
        doctor_id = getIntent().getStringExtra(DOCTOR_ID);
        room_code = getIntent().getStringExtra(ROOM_CODE);
        room_name = getIntent().getStringExtra(ROOM_NAME);
        patient_id = getIntent().getStringExtra(PATIENT_ID);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mDatabasePatientRequests = mDatabase.child("Rooms").child(doctor_id)
                .child(room_code).child("patient_requests");
        mDatabaseGuardianRequests = mDatabase.child("Rooms").child(doctor_id)
                .child(room_code).child("guardian_requests");

        rvDocRequest = findViewById(R.id.rvDocRequest);
        tvRequests = findViewById(R.id.tvRequests);

        Log.d(tag, String.valueOf(patient_id));

        if (request_type.equals("guardian_request"))
        {
            tvRequests.setText("Guardian Requests");
        }
        else if (request_type.equals("patient_request"))
        {
            tvRequests.setText("Patient Requests");
        }

        rvDocRequest.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvDocRequest.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (user_type.equals("doctor"))
        {
            if (request_type.equals("patient_request"))
            {
                startFirebaseRecyclerPatient();
            }
            else if (request_type.equals("guardian_request"))
            {
                startFirebaseRecyclerGuardian();
            }
        }
        else if (user_type.equals("patient"))
        {
            startFirebaseRecyclerGuardian();
        }

    }

    @Override
    protected void onStop()
    {
        super.onStop();

        requestRecyclerAdapter.stopListening();
    }

    private void startFirebaseRecyclerGuardian()
    {
        requestOptions = new FirebaseRecyclerOptions.Builder<ModelRequests>().setQuery(mDatabaseGuardianRequests.limitToLast(30),
                ModelRequests.class)
                .build();

        requestRecyclerAdapter = new FirebaseRecyclerAdapter<ModelRequests, RequestViewHolder>(requestOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, final int position,
                                            @NonNull final ModelRequests model) {

                holder.SetPersonName(model.getGuardian());
                holder.SetChar(model.getGuardian());

                holder.tvPatAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showAcceptDialog(getRef(position).getKey(), patient_id); //Guardian ID, Patient ID
                    }
                });

                holder.tvPatReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showRejectDialog(getRef(position), getRef(position).getKey());
                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_request_guardian, parent, false);

                return new RequestViewHolder(mView);
            }
        };
        rvDocRequest.setAdapter(requestRecyclerAdapter);
        requestRecyclerAdapter.startListening();
    }

    private void startFirebaseRecyclerPatient()
    {
        requestOptions = new FirebaseRecyclerOptions.Builder<ModelRequests>().setQuery(mDatabasePatientRequests.limitToLast(30),
                ModelRequests.class)
                .build();

        requestRecyclerAdapter = new FirebaseRecyclerAdapter<ModelRequests, RequestViewHolder>(requestOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, final int position,
                                            @NonNull final ModelRequests model) {

                holder.SetPersonName(model.getPatient());
                holder.SetDateOfBirth(model.getDate_of_birth());
                holder.SetChar(model.getPatient());

                holder.tvPatAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showAcceptDialog(getRef(position).getKey(), patient_id); //Patients ID, null id
                    }
                });

                holder.tvPatReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showRejectDialog(getRef(position), getRef(position).getKey());
                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_request_patient,
                        parent, false);

                return new RequestViewHolder(mView);
            }
        };
        rvDocRequest.setAdapter(requestRecyclerAdapter);
        requestRecyclerAdapter.startListening();
    }

    private void showAcceptDialog(final String requesterKey, final String patient_id)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton("Accept request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new AcceptRequest(RoomRequests.this)
                        .execute(requesterKey, request_type, user_type, doctor_id, room_code, room_name, patient_id);
            }
        });
        builder.setTitle("Are you sure?");
        builder.setMessage("The request will be accepted.");
        builder.show();
    }

    private static class AcceptRequest extends AsyncTask<String, Void, String[]>
    {
        private WeakReference<RoomRequests> roomRequestsWeakReference;
        private DatabaseReference mDatabase;

        AcceptRequest (RoomRequests roomRequests)
        {
            roomRequestsWeakReference = new WeakReference<>(roomRequests);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            RoomRequests roomRequests = roomRequestsWeakReference.get();
            if (roomRequests == null || roomRequests.isFinishing())
            {
                return;
            }

            this.mDatabase = roomRequests.mDatabase;
        }

        @Override
        protected String[] doInBackground(String... strings) {
            //requesterKey, request_type, user_type, doctor_id, room_code, room_name, patient_id

            String[] returnString = null;
            if (TextUtils.equals(strings[1], "patient_request"))
            {
                mDatabase.child("Rooms").child(strings[3]).child(strings[4])
                        .child("full").setValue(true);
                mDatabase.child("Rooms").child(strings[3]).child(strings[4])
                        .child("patient_id").setValue(strings[0]);

                Map<String, Object> userPatRoom = new HashMap<>();

                userPatRoom.put("doctor_id", strings[3]);
                userPatRoom.put("room_name", strings[5]);
                mDatabase.child("Users").child(strings[0]).child("rooms_patient").child(strings[4])
                        .setValue(userPatRoom);

                mDatabase.child("Rooms").child(strings[3]).child(strings[4])
                        .child("patient_requests").removeValue();

                returnString = new String[] {strings[2], strings[3], strings[4], strings[5], strings[0]};
            }
            else if (TextUtils.equals(strings[1], "guardian_request"))
            {
                mDatabase.child("Rooms").child(strings[3]).child(strings[4]).child("guardians")
                        .child(strings[0]).child("guardian_id").setValue(strings[0]);

                Map<String, Object> userGuaRoom = new HashMap<>();

                userGuaRoom.put("doctor_id", strings[3]);
                userGuaRoom.put("room_name", strings[5]);
                mDatabase.child("Users").child(strings[0]).child("rooms_guardian").child(strings[4])
                        .setValue(userGuaRoom);

                mDatabase.child("ReadingsAccess").child(strings[6]).child(strings[0]).setValue(true);

                mDatabase.child("Rooms").child(strings[3]).child(strings[4]).child("guardian_requests")
                        .child(strings[0]).removeValue();

                returnString = new String[]{strings[2], strings[3], strings[4], strings[5], strings[6]};
            }

            return returnString;
        }

        @Override
        protected void onPostExecute(String[] infos) {
            super.onPostExecute(infos);
            //user_type, doctor_id, room_code, room_name, patient_id

            RoomRequests roomRequests = roomRequestsWeakReference.get();
            if (roomRequests == null || roomRequests.isFinishing())
            {
                return;
            }

            if (infos != null)
            {
                Toast.makeText(roomRequests, "Request accepted.", Toast.LENGTH_SHORT).show();

                Intent portalIntent = new Intent(roomRequests, RoomportalMain.class);
                portalIntent.putExtra(USER_TYPE, infos[0]);
                portalIntent.putExtra(DOCTOR_ID, infos[1]);
                portalIntent.putExtra(ROOM_CODE, infos[2]);
                portalIntent.putExtra(ROOM_NAME, infos[3]);
                portalIntent.putExtra(PATIENT_ID, infos[4]);
                portalIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                roomRequests.startActivity(portalIntent);
                roomRequests.finish();

            } else {
                Toast.makeText(roomRequests, "Failed to accept request.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showRejectDialog(final DatabaseReference ref, final String requesterKey)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.equals(request_type, "patient_request"))
                {
                    new RejectRequest(ref).execute(requesterKey, doctor_id);
                }
                else if (TextUtils.equals(request_type, "guardian_request"))
                {
                    new RejectRequest(ref).execute(null, null);
                }

                Toast.makeText(RoomRequests.this, "The request is rejected.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setTitle("Are you sure?");
        builder.setMessage("The request will be rejected.");
        builder.show();
    }

    private static class RejectRequest extends AsyncTask<String, Void, Void>
    {
        private DatabaseReference ref;
        RejectRequest(DatabaseReference ref)
        {
            this.ref = ref;
        }

        @Override
        protected Void doInBackground(String... strings) {
            //patient_id, doctor_id

            if (!TextUtils.isEmpty(strings[0]) && !TextUtils.isEmpty(strings[1]))
            {
                ref.getRoot().child("ReadingsAccess").child(strings[0]).child(strings[1]).removeValue();
            }

            ref.removeValue();

            return null;
        }
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView tvPatAccept, tvPatReject;
        TextView tvDob;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            tvPatAccept = mView.findViewById(R.id.tvPatAccept);
            tvPatReject = mView.findViewById(R.id.tvPatReject);
        }

        void SetChar(String charAt)
        {
            TextView tvCharRequest = mView.findViewById(R.id.tvCharRequest);
            tvCharRequest.setText(String.valueOf(charAt.toUpperCase().charAt(0)));
        }

        void SetPersonName(String personName)
        {
            TextView tvNameRequest = mView.findViewById(R.id.tvNameRequest);
            tvNameRequest.setText(personName);
        }

        void SetDateOfBirth(String dateOfBirth)
        {
            tvDob = mView.findViewById(R.id.tvDob);
            tvDob.setText(dateOfBirth);
        }
    }
}
