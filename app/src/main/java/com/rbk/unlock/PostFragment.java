package com.rbk.unlock;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment
{
    private View mView;
    private VideoView UserVideo;
    private EditText DescriptionText;
    private Button Upload;
    private static final int REQUEST_TAKE_GALLERY_VIDEO=1;
    private static final int REQUEST_TAKE_CAMERA_VIDEO=2;
    private StorageReference VideosRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;
    private DatabaseReference UsersRef,PostsRef;
    private Uri selectedImageUri;
    private String videofile,Description;
    private String saveCurrentDate, saveCurrentTime, postRandomName;
    private long countPosts=0;
    private ProgressDialog loadingBar;


    public PostFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView=inflater.inflate(R.layout.fragment_post, container, false);

        mAuth=FirebaseAuth.getInstance();
        CurrentUserId=mAuth.getCurrentUser().getUid();

        loadingBar=new ProgressDialog(getContext());

        VideosRef= FirebaseStorage.getInstance().getReference().child("Videos");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        UserVideo=mView.findViewById(R.id.userVideo);
        DescriptionText=mView.findViewById(R.id.description);
        Upload=mView.findViewById(R.id.buttonPost);

        UserVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                CharSequence options[]=new CharSequence[]{

                        "Open Camera",
                        "Open Gallery"

                };
                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                builder.setTitle("Choose Way");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position)
                    {

                        if (position==0)
                        {
                            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            startActivityForResult(intent, REQUEST_TAKE_CAMERA_VIDEO);
                        }
                        if (position==1)
                        {
                            Intent intent = new Intent();
                            intent.setType("video/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);
                        }

                    }
                });
                builder.show();

            }
        });

        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                loadingBar.setTitle("Uploading Post");
                loadingBar.setMessage("Please Wait");
                loadingBar.setCancelable(false);
                loadingBar.show();

                Description=DescriptionText.getText().toString();

                if (TextUtils.isEmpty(Description))
                {
                    Toast.makeText(getActivity(), "Description", Toast.LENGTH_SHORT).show();
                }
                if (selectedImageUri==null)
                {
                    Toast.makeText(getActivity(), "Please Post video", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    UploadVideos();
                }

            }
        });


        return mView;
    }

    private void UploadVideos()
    {

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName= saveCurrentDate + saveCurrentTime;

        StorageReference filePath=VideosRef.child("Videos").child(postRandomName+".mp4");

        filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {

                    videofile=task.getResult().getDownloadUrl().toString();
                    StoringPost();
                }
                else
                {
                    Toast.makeText(getActivity(), ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void StoringPost()
    {

        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    countPosts=dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts=0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


            UsersRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        String userFullName = dataSnapshot.child("fullname").getValue().toString();
                        String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();


                            HashMap postsMap = new HashMap();
                            postsMap.put("uid", CurrentUserId);
                            postsMap.put("date", saveCurrentDate);
                            postsMap.put("time", saveCurrentTime);
                            postsMap.put("description", Description);
                            postsMap.put("postvideo", videofile);
                            postsMap.put("profileimage", userProfileImage);
                            postsMap.put("fullname", userFullName);
                            postsMap.put("counter", countPosts);
                            String PostKey=CurrentUserId + postRandomName;
                            PostsRef.child(PostKey).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        SendUserToHomeActivity();
                                        loadingBar.dismiss();
                                        //finish();
                                    } else {
                                        Toast.makeText(getActivity(), "Error Occured while updating your post." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    private void SendUserToHomeActivity()
    {
        startActivity(new Intent(getActivity(),HomeActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO)
            {
                selectedImageUri = data.getData();
                UserVideo.setVideoURI(selectedImageUri);

            }
            if (requestCode==REQUEST_TAKE_CAMERA_VIDEO)
            {
                selectedImageUri = data.getData();
                UserVideo.setVideoURI(selectedImageUri);

            }

        }


    }
}
