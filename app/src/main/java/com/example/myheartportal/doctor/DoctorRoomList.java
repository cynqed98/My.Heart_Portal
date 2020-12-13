package com.example.myheartportal.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myheartportal.MainActivity;
import com.example.myheartportal.R;
import com.example.myheartportal.ModelRooms;
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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.example.myheartportal.Constants.DOCTOR_ID;
import static com.example.myheartportal.Constants.PATIENT_ID;
import static com.example.myheartportal.Constants.ROOM_CODE;
import static com.example.myheartportal.Constants.ROOM_NAME;
import static com.example.myheartportal.Constants.USER_TYPE;

public class DoctorRoomList extends AppCompatActivity {

    //***LAYOUTS
    private RecyclerView rvDocRoom;
    //***CUSTOMDIALOG
    private AlertDialog addRoomDialog;
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
        setContentView(R.layout.activity_doctor_room_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = firebaseAuth.getCurrentUser();
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        rvDocRoom = findViewById(R.id.rvDocRoom);

        rvDocRoom.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvDocRoom.setLayoutManager(linearLayoutManager);
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

    public void startFirebaseRecycler()
    {
        roomOptions = new FirebaseRecyclerOptions.Builder<ModelRooms>().setQuery(mDatabase.child("Rooms")
                .child(mCurrentUser.getUid()), ModelRooms.class).build();

        roomRecyclerAdapter = new FirebaseRecyclerAdapter<ModelRooms, RoomViewHolder>(roomOptions)
        {
            @Override
            protected void onBindViewHolder(@NonNull final RoomViewHolder holder, final int position, @NonNull final ModelRooms model)
            {
                final String patient_id = model.getPatient_id();
                if (!TextUtils.isEmpty(patient_id))
                {
                    holder.SetPatient(patient_id);
                }
                holder.SetRoomName(model.getRoom_name());
                holder.SetChar(model.getRoom_name());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String room_code = getRef(position).getKey();
                        String room_name = model.getRoom_name();

                        Intent roomPortalIntent = new Intent(DoctorRoomList.this, RoomportalMain.class);
                        roomPortalIntent.putExtra(USER_TYPE, "doctor");
                        roomPortalIntent.putExtra(ROOM_CODE, room_code);
                        roomPortalIntent.putExtra(ROOM_NAME, room_name);
                        roomPortalIntent.putExtra(DOCTOR_ID, mCurrentUser.getUid());
                        roomPortalIntent.putExtra(PATIENT_ID, patient_id);
                        roomPortalIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(roomPortalIntent);
                    }
                });
            }

            @NonNull
            @Override
            public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_room, parent, false);

                return new RoomViewHolder(mView);
            }
        };
        rvDocRoom.setAdapter(roomRecyclerAdapter);
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

        void SetPatient(final String patient_id)
        {
            final TextView tvPatName = mView.findViewById(R.id.tvPersonName);
            mDatabase.child("Users").child(patient_id).child("patient_details")
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
                    tvPatName.setText(fullName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabase.child("Users").child(patient_id).child("patient_details").removeEventListener(this);
                }
            });
        }
    }

    private void roomDialogBuild()
    {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.room_create_dialog, null);
        addRoomDialog = new AlertDialog.Builder(this).create();
        addRoomDialog.setView(view);
        addRoomDialog.setTitle("Create Room");
        addRoomDialog.setIcon(R.mipmap.myheart_logo_round);
        addRoomDialog.setCancelable(true);
        addRoomDialog.setMessage("Please create a unique room code. Note that it is case sensitive. " +
                "For the room name, you can set it to anything.");

        addRoomDialog.setButton(DialogInterface.BUTTON_POSITIVE, "CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing here, only instantiating the button
            }
        });

        addRoomDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(DoctorRoomList.this, "Cancelled.", Toast.LENGTH_SHORT).show();
            }
        });

        addRoomDialog.show();

        addRoomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Boolean wantToCloseDialog = false; //set false if you want the dialog to stay open when button is clicked.

                EditText etCreateCode = addRoomDialog.findViewById(R.id.etCreateCode);
                EditText etCreateName = addRoomDialog.findViewById(R.id.etCreateName);
                if (!etCreateCode.getText().toString().isEmpty() && !etCreateName.getText().toString().isEmpty())
                {
                    String room_code = etCreateCode.getText().toString().trim();
                    String room_name = etCreateName.getText().toString().trim();

                    checkIfRoomExistsToCreate(room_code, room_name, etCreateCode);

                } else {
                    Toast.makeText(DoctorRoomList.this, "Please fill up all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkIfRoomExistsToCreate(final String room_code, final String room_name, final EditText etCreateCode)
    {
        mDatabase.child("Rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean roomExists = false;

                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    if (data.child(room_code).exists())
                    {
                        roomExists = true;
                    }
                }

                if (!roomExists)
                {
                    DatabaseReference doctorRoom = mDatabase.child("Rooms").child(mCurrentUser.getUid()).child(room_code);
                    Map<String, Object> roomCreate = new HashMap<>();

                    roomCreate.put("full", false);
                    roomCreate.put("doctor_id", mCurrentUser.getUid());
                    roomCreate.put("room_code", room_code);
                    roomCreate.put("room_name", room_name);
                    doctorRoom.setValue(roomCreate);

                    addRoomDialog.dismiss();
                    Toast.makeText(DoctorRoomList.this, "Room created.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(DoctorRoomList.this, "The room already exists.", Toast.LENGTH_SHORT).show();
                    etCreateCode.setError("The code is already used.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(DoctorRoomList.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logOutDoctor()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton("Yes, let me go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();

                Intent i = new Intent(DoctorRoomList.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                Toast.makeText(DoctorRoomList.this, "Sign out successful.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle("Are you sure?");
        builder.setMessage("You will be signed out from My.Heart Portal.");
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.doctor_room_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if (item.getItemId() == R.id.action_create_room)
        {
            roomDialogBuild();
        }

        if (item.getItemId() == R.id.action_view_profile)
        {
            if (mCurrentUser != null && mCurrentUser.isEmailVerified())
            {
                Intent viewIntent = new Intent(DoctorRoomList.this, DoctorProfileView.class);
                viewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(viewIntent);
            }
        }

        if (item.getItemId() == R.id.action_logout)
        {
            logOutDoctor();
        }

        return super.onOptionsItemSelected(item);
    }
}
