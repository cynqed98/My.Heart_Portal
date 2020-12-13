package com.example.myheartportal.room_ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myheartportal.ModelGuardians;
import com.example.myheartportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.myheartportal.Constants.ROOM_INFO;

public class TabProfile extends Fragment {

    //***LIFECYCLE
    private TabProfileViewModel tabProfileViewModel;
    private LiveData<String> docInfo, docName, patInfo, patName;
    //***FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseRecyclerOptions<ModelGuardians> guardianOptions;
    private FirebaseRecyclerAdapter<ModelGuardians, GuardiansListViewHolder> guardiansRecyclerAdapter;
    //***LAYOUTS
    private TextView tvRoomName, tvRoomCode, tvDocName, tvDocInfo, tvPatName, tvPatInfo;
    private RecyclerView rvGuardians;
    //***VARIABLES
    private final String tag = "TabProfile";
    private String [] room_info;
    private boolean hide = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mProfileView = inflater.inflate(R.layout.tab_profile, container, false);

        tvRoomName = mProfileView.findViewById(R.id.tvRoomName);
        tvRoomCode = mProfileView.findViewById(R.id.tvRoomCode);
        tvDocName = mProfileView.findViewById(R.id.tvDocName);
        tvDocInfo = mProfileView.findViewById(R.id.tvDocInfo);
        tvPatName = mProfileView.findViewById(R.id.tvPatName);
        tvPatInfo = mProfileView.findViewById(R.id.tvPatInfo);
        rvGuardians = mProfileView.findViewById(R.id.rvGuardians);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        if (getArguments() != null)
        {
            room_info = getArguments().getStringArray(ROOM_INFO);
            if (room_info != null)
            {
                tvRoomName.setText(room_info[3]);
            }
        } else {
            room_info = new String[4];
        }

        tabProfileViewModel = new ViewModelProvider(this).get(TabProfileViewModel.class);
        tabProfileViewModel.setDoctor_id(room_info[1]);
        tabProfileViewModel.setRoom_code(room_info[2]);
        tabProfileViewModel.init();
        docName = tabProfileViewModel.getDocName();
        docName.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvDocName.setText(s);
            }
        });
        docInfo = tabProfileViewModel.getDocInfo();
        docInfo.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvDocInfo.setText(s);
            }
        });
        patName = tabProfileViewModel.getPatName();
        patName.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvPatName.setText(s);
            }
        });
        patInfo = tabProfileViewModel.getPatInfo();
        patInfo.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvPatInfo.setText(s);
            }
        });

        tvRoomCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hide)
                {
                    tvRoomCode.setText(room_info[2]);
                    tvRoomCode.setTextColor(Color.BLACK);
                    hide = false;
                } else {
                    tvRoomCode.setText("Get Room Code");
                    tvRoomCode.setTextColor(Color.GRAY);
                    hide = true;
                }
            }
        });

        return mProfileView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        rvGuardians.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvGuardians.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        startFirebaseRecycler();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        guardiansRecyclerAdapter.stopListening();
    }

    private void startFirebaseRecycler()
    {
        guardianOptions = new FirebaseRecyclerOptions.Builder<ModelGuardians>().setQuery(mDatabase.child("Rooms")
                .child(room_info[1]).child(room_info[2]).child("guardians"), ModelGuardians.class).build();

        guardiansRecyclerAdapter = new FirebaseRecyclerAdapter<ModelGuardians, GuardiansListViewHolder>(guardianOptions) {
            @Override
            protected void onBindViewHolder(@NonNull GuardiansListViewHolder holder, int position, @NonNull ModelGuardians model)
            {
                if (!TextUtils.isEmpty(model.getGuardian_id())) {
                    holder.SetGuardiansNameInfo(model.getGuardian_id());
                }
            }

            @NonNull
            @Override
            public GuardiansListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_guardians, parent, false);

                return new GuardiansListViewHolder(mView);
            }
        };
        rvGuardians.setAdapter(guardiansRecyclerAdapter);
        guardiansRecyclerAdapter.startListening();
    }

    public class GuardiansListViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        MutableLiveData<String> guaName;
        MutableLiveData<String> guaInfo;

        GuardiansListViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        void SetGuardiansNameInfo (String guardian_id)
        {
            final TextView tvGuaName = mView.findViewById(R.id.tvGuaName);
            final TextView tvGuaInfo = mView.findViewById(R.id.tvGuaInfo);
            guaName = new MutableLiveData<>("Guardian's Name");
            guaInfo = new MutableLiveData<>("Guardian's Info");

            guaName.observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    tvGuaName.setText(s);
                }
            });
            guaInfo.observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    tvGuaInfo.setText(s);
                }
            });

            getGuardianInfo (guaName, guaInfo, guardian_id);
        }
    }

    private void getGuardianInfo(final MutableLiveData<String> guaName, final MutableLiveData<String> guaInfo,
                                 final String guardian_id)
    {
        mDatabase.child("Users").child(guardian_id).child("guardian_details")
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String fName = "", mName = "", lName = "";
                if (dataSnapshot.hasChild("last_name"))
                {
                    lName = (String) dataSnapshot.child("last_name").getValue();
                }
                if (dataSnapshot.hasChild("first_name"))
                {
                    fName = (String) dataSnapshot.child("first_name").getValue();
                }
                if (dataSnapshot.hasChild("middle_name"))
                {
                    mName = (String) dataSnapshot.child("middle_name").getValue();
                }
                String fullName = lName + ", " + fName + " " + mName;
                guaName.setValue(fullName);

                String ownPhone = "", mail = "";
                if (dataSnapshot.hasChild("own_phone"))
                {
                    ownPhone = (String) dataSnapshot.child("own_phone").getValue();
                }
                if (dataSnapshot.hasChild("mail"))
                {
                    mail = (String) dataSnapshot.child("mail").getValue();
                }
                String guasInfo = ownPhone + "\n" + mail;
                guaInfo.setValue(guasInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mDatabase.child("Users").child(guardian_id).child("guardian_details").removeEventListener(this);
            }
        });
    }
}
