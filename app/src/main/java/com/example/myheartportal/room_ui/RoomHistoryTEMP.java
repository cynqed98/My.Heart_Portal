package com.example.myheartportal.room_ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myheartportal.ModelHistoryTEMP;
import com.example.myheartportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.myheartportal.Constants.DOCTOR_ID;
import static com.example.myheartportal.Constants.HISTORY_FILE;
import static com.example.myheartportal.Constants.ROOM_CODE;
import static com.example.myheartportal.Constants.ROOM_NAME;
import static com.example.myheartportal.Constants.USER_TYPE;

public class RoomHistoryTEMP extends AppCompatActivity {

    //***LAYOUTS
    private RecyclerView rvHistory;
    private TextView tvHistory;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseTEMP;
    private FirebaseRecyclerOptions<ModelHistoryTEMP> historyOptions;
    private FirebaseRecyclerAdapter<ModelHistoryTEMP, HistoryViewHolder> historyRecyclerAdapter;
    //***LIFECYCLE
    private MutableLiveData<String> patientIDLive;
    //***VARIABLES
    private final String tag = this.getClass().getSimpleName();
    private String user_type;
    private String doctor_id;
    private String room_code;
    private String room_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_history);

        user_type = getIntent().getStringExtra(USER_TYPE);
        doctor_id = getIntent().getStringExtra(DOCTOR_ID);
        room_code = getIntent().getStringExtra(ROOM_CODE);
        room_name = getIntent().getStringExtra(ROOM_NAME);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        patientIDLive = new MutableLiveData<>("");
        patientIDLive.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String patient_id) {

                if (!TextUtils.isEmpty(patient_id))
                {
                    startFirebaseRecycler(patient_id);
                }
            }
        });

        tvHistory = findViewById(R.id.tvHistory);
        tvHistory.setText("Saved TEMP Data");
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvHistory.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        getPatientID ();
    }

    private void getPatientID()
    {
        mDatabase.child("Rooms").child(doctor_id).child(room_code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("patient_id"))
                        {
                            String patient_id = (String) dataSnapshot.child("patient_id").getValue();
                            patientIDLive.setValue(patient_id);
                        }
                        else {
                            patientIDLive.setValue("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mDatabase.child("Rooms").child(doctor_id).child(room_code).removeEventListener(this);
                    }
                });
    }

    private void startFirebaseRecycler(String patient_id)
    {
        mDatabaseTEMP = mDatabase.child("Readings").child(patient_id).child("TEMP");

        historyOptions = new FirebaseRecyclerOptions.Builder<ModelHistoryTEMP>().setQuery(mDatabaseTEMP,
                ModelHistoryTEMP.class).build();

        historyRecyclerAdapter = new FirebaseRecyclerAdapter<ModelHistoryTEMP, HistoryViewHolder>(historyOptions) {
            @Override
            protected void onBindViewHolder(@NonNull HistoryViewHolder holder, final int position, @NonNull ModelHistoryTEMP model) {

                holder.SetHistName(getRef(position).getKey());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent historyIntent = new Intent(RoomHistoryTEMP.this, RoomportalMain.class);
                        historyIntent.putExtra(USER_TYPE, user_type);
                        historyIntent.putExtra(DOCTOR_ID, doctor_id);
                        historyIntent.putExtra(ROOM_CODE, room_code);
                        historyIntent.putExtra(ROOM_NAME, room_name);
                        historyIntent.putExtra(HISTORY_FILE, getRef(position).getKey());
                        historyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(historyIntent);
                        finish();
                    }
                });
            }

            @NonNull
            @Override
            public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_history,
                        parent, false);

                return new HistoryViewHolder(mView);
            }
        };

        rvHistory.setAdapter(historyRecyclerAdapter);
        historyRecyclerAdapter.startListening();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView tvHistName;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        void SetHistName (String histName)
        {
            tvHistName = mView.findViewById(R.id.tvHistName);
            tvHistName.setText(histName);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        historyRecyclerAdapter.stopListening();
    }
}
