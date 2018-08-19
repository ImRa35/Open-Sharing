package com.rbk.unlock;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment
{

    private CircleImageView UserImage;
    private View mView;
    private TextView NameUser;
    private FirebaseAuth mAuth;
    private String CurrentUserId;
    private DatabaseReference UsersRef;
    private Button LogoutButton;


    public ProfileFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView=inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth=FirebaseAuth.getInstance();
        CurrentUserId=mAuth.getCurrentUser().getUid();

        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);

        UserImage=mView.findViewById(R.id.profile_image);
        NameUser=mView.findViewById(R.id.userName);

        LogoutButton=mView.findViewById(R.id.buttonLogout);

        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                SendUserToLoginActivity();
            }
        });


        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.exists())
                {
                    String uname=dataSnapshot.child("fullname").getValue().toString();
                    String pimage=dataSnapshot.child("profileimage").getValue().toString();

                    NameUser.setText(uname);
                    Picasso.with(getContext()).load(pimage).into(UserImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        return mView;
    }

    private void SendUserToLoginActivity()
    {
        startActivity(new Intent(getActivity(),LoginActivity.class));
    }

}
