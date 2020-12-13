package com.example.myheartportal.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myheartportal.ModelRooms;
import com.example.myheartportal.R;
import com.example.myheartportal.room_ui.RoomportalMain;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.myheartportal.Constants.DOCTOR_ID;
import static com.example.myheartportal.Constants.PATIENT_ID;
import static com.example.myheartportal.Constants.ROOM_NAME;
import static com.example.myheartportal.Constants.ROOM_CODE;
import static com.example.myheartportal.Constants.USER_TYPE;

public class PatientRoomList extends AppCompatActivity {

    //***LAYOUTS
    private RecyclerView rvPatRoom;
    //***FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerOptions<ModelRooms> roomOptions;
    private FirebaseRecyclerAdapter<ModelRooms, RoomViewHolder> roomRecyclerAdapter;
    //***VARIABLES
    private String tag = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_room_list);

        rvPatRoom = findViewById(R.id.rvPatRoom);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = mAuth.getCurrentUser();
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        rvPatRoom.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvPatRoom.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        mCurrentUser = mAuth.getCurrentUser();

        startFirebaseRecycler();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        roomRecyclerAdapter.stopListening();
    }

    private void startFirebaseRecycler()
    {
        roomOptions = new FirebaseRecyclerOptions.Builder<ModelRooms>().setQuery(mDatabase.child("Users")
                .child(mCurrentUser.getUid()).child("rooms_patient").limitToLast(30), ModelRooms.class).build();

        roomRecyclerAdapter = new FirebaseRecyclerAdapter<ModelRooms, RoomViewHolder>(roomOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final RoomViewHolder holder, final int position, @NonNull final ModelRooms model) {

                holder.SetChar(model.getRoom_name());
                holder.SetRoomName(model.getRoom_name());
                holder.SetDoctor(model.getDoctor_id());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String room_code = getRef(position).getKey();
                        String doctor_id = model.getDoctor_id();
                        String room_name = model.getRoom_name();

                        Intent patientRoomIntent = new Intent(PatientRoomList.this, RoomportalMain.class);
                        patientRoomIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        patientRoomIntent.putExtra(USER_TYPE, "patient");
                        patientRoomIntent.putExtra(DOCTOR_ID, doctor_id);
                        patientRoomIntent.putExtra(ROOM_CODE, room_code);
                        patientRoomIntent.putExtra(ROOM_NAME, room_name);
                        patientRoomIntent.putExtra(PATIENT_ID, mCurrentUser.getUid());
                        startActivity(patientRoomIntent);
                    }
                });
            }

            @NonNull
            @Override
            public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_room, parent, false);

                return new RoomViewHolder(mView);
            }
        };
        rvPatRoom.setAdapter(roomRecyclerAdapter);
        roomRecyclerAdapter.startListening();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        void SetChar(String charAt)
        {
            TextView tvCharRoom = mView.findViewById(R.id.tvCharRoom);
            if (charAt != null)
            {
                tvCharRoom.setText(String.valueOf(charAt.charAt(0)));
            }
        }

        void SetRoomName(String roomName)
        {
            TextView tvRoomName = mView.findViewById(R.id.tvRoomName);
            tvRoomName.setText(roomName);
        }

        void SetDoctor(final String doctor_id)
        {
            final TextView tvDocName = mView.findViewById(R.id.tvPersonName);
            mDatabase.child("Users").child(doctor_id).child("doctor_details")
                .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String first_name = "", middle_name = "", last_name = "";
                    if (dataSnapshot.hasChild("last_name"))
                    {
                        last_name = (String) dataSnapshot.child("last_name").getValue();
                    }
                    if (dataSnapshot.hasChild("first_name"))
                    {
                        first_name = (String) dataSnapshot.child("first_name").getValue();
                    }
                    if (dataSnapshot.hasChild("middle_name"))
                    {
                        middle_name = (String) dataSnapshot.child("middle_name").getValue();
                    }

                    String fullName = last_name + ", " + first_name + " " + middle_name;
                    tvDocName.setText(fullName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabase.child("Users").child(doctor_id).child("doctor_details").removeEventListener(this);
                }
            });
        }
    }
}
