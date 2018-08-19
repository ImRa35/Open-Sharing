package com.rbk.unlock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{

    private FirebaseAuth mAuth;
    private String CurrentUserId;
    private DatabaseReference UsersRef;
    private EditText NameText;
    private CircleImageView ProfileImage;
    private Button SaveButton;
    private static final int Gallery_Pick=1;
    private StorageReference UserProfileImageRef;
    private String downloadUrl;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth=FirebaseAuth.getInstance();
        CurrentUserId=mAuth.getCurrentUser().getUid();

        loadingBar=new ProgressDialog(this);

        String Email=getIntent().getExtras().get("email").toString();
        String Pass=getIntent().getExtras().get("password").toString();

        NameText=findViewById(R.id.editTextName);
        ProfileImage=findViewById(R.id.setup_profile_image);
        SaveButton=findViewById(R.id.buttonSave);


        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);
        UserProfileImageRef=FirebaseStorage.getInstance().getReference().child("Profile Images");


        HashMap userMap=new HashMap();
        userMap.put("email",Email);
        userMap.put("password",Pass);

        UsersRef.updateChildren(userMap);

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username=NameText.getText().toString();
                if(TextUtils.isEmpty(username))
                {
                    Toast.makeText(SetupActivity.this, "Name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    UpdateInformation(username);
                }
            }
        });





    }

    private void UpdateInformation(String username)
    {
        UsersRef.child("fullname").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                SendUserToHomeActivity();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        try {


            if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null)
            {
                Uri ImageUri = data.getData();

                CropImage.activity(ImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }


            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK)
                {
                    loadingBar.setTitle("Profile Image");
                    loadingBar.setMessage("Please wait");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);

                    Uri resultUri = result.getUri();

                    StorageReference filePath = UserProfileImageRef.child(CurrentUserId + ".jpg");

                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {


                                downloadUrl = task.getResult().getDownloadUrl().toString();

                                UsersRef.child("profileimage").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Picasso.with(SetupActivity.this).load(downloadUrl).into(ProfileImage);
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void SendUserToHomeActivity()
    {
        startActivity(new Intent(SetupActivity.this,HomeActivity.class));
    }
}
