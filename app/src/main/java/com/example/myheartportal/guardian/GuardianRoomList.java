package com.example.myheartportal.guardian;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import static com.example.myheartportal.Constants.ROOM_CODE;
import static com.example.myheartportal.Constants.ROOM_NAME;
import static com.example.myheartportal.Constants.USER_TYPE;

public class GuardianRoomList extends AppCompatActivity {

    //***LAYOUTS
    private RecyclerView rvGuaRoom;
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
        setContentView(R.layout.activity_guardian_room_list);

        rvGuaRoom = findViewById(R.id.rvGuaRoom);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentUser = mAuth.getCurrentUser();
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvGuaRoom.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvGuaRoom.setLayoutManager(linearLayoutManager);
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
                .child(mCurrentUser.getUid()).child("rooms_guardian").limitToLast(30), ModelRooms.class).build();

        roomRecyclerAdapter = new FirebaseRecyclerAdapter<ModelRooms, RoomViewHolder>(roomOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RoomViewHolder holder, final int position, @NonNull final ModelRooms model) {

                holder.SetChar(model.getRoom_name());
                holder.SetRoomName(model.getRoom_name());
                holder.SetDoctor(model.getDoctor_id());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String room_code = getRef(position).getKey();
                        String doctor_id = model.getDoctor_id();
                        String room_name = model.getRoom_name();
                        String patient_id = model.getPatient_id();

                        Intent guardianRoomIntent = new Intent(GuardianRoomList.this, RoomportalMain.class);
                        guardianRoomIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        guardianRoomIntent.putExtra(USER_TYPE, "guardian");
                        guardianRoomIntent.putExtra(DOCTOR_ID, doctor_id);
                        guardianRoomIntent.putExtra(ROOM_CODE, room_code);
                        guardianRoomIntent.putExtra(ROOM_NAME, room_name);
                        guardianRoomIntent.putExtra(PATIENT_ID, patient_id);
                        startActivity(guardianRoomIntent);
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
        rvGuaRoom.setAdapter(roomRecyclerAdapter);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.guardian_room_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if (item.getItemId() == R.id.action_find_room)
        {
            if (mCurrentUser != null && mCurrentUser.isEmailVerified())
            {
                roomDialogBuild();

            } else {
                Toast.makeText(this, "Please register as patient or verify your email.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (item.getItemId() == R.id.action_view_profile)
        {
            if (mCurrentUser != null && mCurrentUser.isEmailVerified())
            {
                Intent viewIntent = new Intent(GuardianRoomList.this, GuardianProfileView.class);
                viewIntent.putExtra(USER_TYPE, "guardian");
                viewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(viewIntent);
            }
        }

        if (item.getItemId() == R.id.action_logout)
        {
            logOutGuardian();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOutGuardian()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton("Yes, let me go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();

                Intent i = new Intent(GuardianRoomList.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                Toast.makeText(GuardianRoomList.this, "Sign out successful.", Toast.LENGTH_SHORT).show();
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

    private void roomDialogBuild()
    {
        View view = getLayoutInflater().inflate(R.layout.room_add_dialog, null);
        addRoomDialog = new AlertDialog.Builder(this).create();
        addRoomDialog.setView(view);
        addRoomDialog.setTitle("Find Room");
        addRoomDialog.setIcon(R.mipmap.myheart_logo_round);
        addRoomDialog.setCancelable(true);
        addRoomDialog.setMessage("Please enter the room code.");

        addRoomDialog.setButton(DialogInterface.BUTTON_POSITIVE, "FIND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing here, only instantiating the button
            }
        });

        addRoomDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(GuardianRoomList.this, "Cancelled.", Toast.LENGTH_SHORT).show();
            }
        });

        addRoomDialog.show();

        addRoomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Boolean wantToCloseDialog = false; //set false if you want the dialog to stay open when button is clicked.

                EditText etRoomCode = addRoomDialog.findViewById(R.id.etRoomCode);
                if (!etRoomCode.getText().toString().isEmpty())
                {
                    String room_code = etRoomCode.getText().toString().trim();

                    checkIfRoomExists(room_code, etRoomCode);
                } else{
                    Toast.makeText(GuardianRoomList.this, "Please enter a code.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkIfRoomExists(final String room_code, final EditText etRoomCode)
    {
        mDatabase.child("Rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean roomExists = false;

                for (final DataSnapshot data : dataSnapshot.getChildren())
                {
                    if (data.hasChild(room_code)) //ROOM EXISTS
                    {
                        if (data.child(room_code).child("patient_id").exists()) //PATIENT EXISTS
                        {
                            if (!data.child(room_code).child("guardians").child(mCurrentUser.getUid()).exists())
                            {
                                mDatabase.child("Users").child(mCurrentUser.getUid()).child("guardian_details")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
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

                                        data.child(room_code).child("guardian_requests").child(mCurrentUser.getUid())
                                                .child("guardian").getRef().setValue(fullName);

                                        addRoomDialog.dismiss();
                                        Toast.makeText(GuardianRoomList.this, "Request to enter room sent.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        mDatabase.child("Users").child(mCurrentUser.getUid()).child("guardian_details")
                                                .removeEventListener(this);
                                    }
                                });
                            } else {
                                Toast.makeText(GuardianRoomList.this, "You are already a guardian in this room.",
                                        Toast.LENGTH_SHORT).show();
                                etRoomCode.setError("You are already a guardian of this room.");
                            }
                        } else {
                            Toast.makeText(GuardianRoomList.this, "There is no patient inside this room.",
                                    Toast.LENGTH_SHORT).show();
                            etRoomCode.setError("There is no patient inside the room.");
                        }
                        roomExists = true;
                    }
                }
                if (!roomExists)
                {
                    Toast.makeText(GuardianRoomList.this, "The room does not exists", Toast.LENGTH_SHORT).show();
                    etRoomCode.setError("The room may not exist. Also note that a room code is case sensitive.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Rooms").removeEventListener(this);
            }
        });
    }
}
