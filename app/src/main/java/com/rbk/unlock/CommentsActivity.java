package com.rbk.unlock;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity
{

    private ImageView PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;

    private String Post_Key , current_user_id;
    private DatabaseReference UsersRef , PostsRef;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth=FirebaseAuth.getInstance();

        current_user_id=mAuth.getCurrentUser().getUid();

        Post_Key=getIntent().getExtras().get("PostKey").toString();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        CommentsList=findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText=findViewById(R.id.comment_input);
        PostCommentButton=findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {

                            String userName=dataSnapshot.child("fullname").getValue().toString();

                            ValidateComment(userName);

                            CommentInputText.setText("  ");

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(Comments.class,R.layout.all_comments_layout,CommentsViewHolder.class,PostsRef) {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position)
            {


                viewHolder.setFullname(model.getFullname());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());


            }
        };

        CommentsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(View itemView)
        {
            super(itemView);

            mView=itemView;
        }

        public void setFullname(String fullname)
        {

            TextView myUserName=mView.findViewById(R.id.comment_username);
            myUserName.setText(fullname+"   ");

        }

        public void setComment(String comment)
        {

            TextView myComment=mView.findViewById(R.id.comment_text);
            myComment.setText(comment);


        }

        public void setDate(String date)
        {

            TextView myDate=mView.findViewById(R.id.comment_date);
            myDate.setText(" Date: "+date);


        }

        public void setTime(String time)
        {

            TextView myTime=mView.findViewById(R.id.comment_time);
            myTime.setText(" Time: "+time);

        }

    }


    private void ValidateComment(String userName)
    {

        String CommentText=CommentInputText.getText().toString();

        if (TextUtils.isEmpty(CommentText))
        {

            Toast.makeText(this, "Please write an comment", Toast.LENGTH_SHORT).show();

        }
        else
        {


            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String RandomKey= current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap=new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",CommentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("fullname",userName);

            PostsRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {

                        Toast.makeText(CommentsActivity.this, "Comments success", Toast.LENGTH_SHORT).show();


                    }
                    else
                    {

                        Toast.makeText(CommentsActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            });


        }

    }
}
