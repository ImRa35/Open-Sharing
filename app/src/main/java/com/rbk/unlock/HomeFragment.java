package com.rbk.unlock;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment
{

    private View myView;
    private RecyclerView postList;
    private DatabaseReference PostsRef,LikesRef;
    private int i=0;
    private String State="pause";
    private Boolean LikeChecker=false;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public HomeFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        myView=inflater.inflate(R.layout.fragment_home, container, false);

        mAuth=FirebaseAuth.getInstance();

        currentUserID=mAuth.getCurrentUser().getUid();


        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");

        postList=myView.findViewById(R.id.postList);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        DisplayAllUsersPost();

        return myView;
    }

    private void DisplayAllUsersPost()
    {

        final Query newPosts=PostsRef.orderByChild("counter");



        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                FirebaseRecyclerAdapter<Posts,PostsViewHolder>firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_users_post_layout,
                                PostsViewHolder.class,
                                newPosts
                        )
                {
                    @Override
                    protected void populateViewHolder(final PostsViewHolder viewHolder, Posts model, int position)
                    {
                        final String PostKey=getRef(position).getKey();
                        final ProgressDialog pd=new ProgressDialog(getContext());
                        pd.setMessage("please wai");
                        pd.show();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getActivity(), model.getProfileimage());
                        viewHolder.setPostvideo(getActivity(),model.getPostvideo());
                        viewHolder.setLikeButtonStatus(PostKey);

                        //viewHolder.username.setText(model.getPostvideo());

                        viewHolder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {

                               pd.dismiss();
                               State="play";
                               viewHolder.videoView.start();
                               viewHolder.videoView.requestFocus();
                               //viewHolder.videoView.setMediaController(new MediaController(getContext()));

                            }
                        });

                        viewHolder.videoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                if (State.equals("pause"))
                                {
                                    viewHolder.videoView.start();
                                    viewHolder.videoView.requestFocus();
                                    State="play";
                                }
                                else if (State.equals("play"))
                                {
                                    viewHolder.videoView.pause();
                                    viewHolder.videoView.requestFocus();
                                    State="pause";
                                }
                                /*i++;

                                Handler handler=new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (i==1)
                                        {
                                            Toast.makeText(getContext(), "single", Toast.LENGTH_SHORT).show();
                                            //i=0;
                                        }
                                        if (i==2)
                                        {
                                            Toast.makeText(getContext(), "double", Toast.LENGTH_SHORT).show();

                                        }
                                        i=0;
                                    }
                                },500);*/

                            }
                        });


                        viewHolder.LikeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                LikeChecker=true;

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (LikeChecker.equals(true))
                                        {

                                            if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                                            {

                                                LikesRef.child(PostKey).child(currentUserID).removeValue();
                                                LikeChecker=false;


                                            }
                                            else
                                            {

                                                LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                                LikeChecker=false;



                                            }

                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });

                        viewHolder.CommentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                startActivity(new Intent(getActivity(),CommentsActivity.class).putExtra("PostKey",PostKey));

                            }
                        });

                        /*viewHolder.videoView.setOnTouchListener(new View.OnTouchListener() {

                            Handler handler = new Handler();

                            int numberOfTaps = 0;
                            long lastTapTimeMs = 0;
                            long touchDownMs = 0;

                            @Override
                            public boolean onTouch(View v, MotionEvent event)
                            {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        touchDownMs = System.currentTimeMillis();
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        handler.removeCallbacksAndMessages(null);

                                        if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                                            //it was not a tap

                                            numberOfTaps = 0;
                                            lastTapTimeMs = 0;
                                            break;
                                        }

                                        if (numberOfTaps > 0
                                                && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                                            numberOfTaps += 1;
                                        } else {
                                            numberOfTaps = 1;
                                        }

                                        lastTapTimeMs = System.currentTimeMillis();

                                        if (numberOfTaps == 3) {
                                            Toast.makeText(getContext(), "triple", Toast.LENGTH_SHORT).show();
                                            //handle triple tap
                                        } else if (numberOfTaps == 2) {
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //handle double tap
                                                    Toast.makeText(getContext(), "double", Toast.LENGTH_SHORT).show();
                                                }
                                            }, ViewConfiguration.getDoubleTapTimeout());
                                        }
                                }
                                return true;
                            }
                        });*/





                    }
                };
                postList.setAdapter(firebaseRecyclerAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageView LikeButton,CommentButton;
        TextView LikesNo;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;
        TextView username,Date,Time;
        VideoView videoView;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;

            username=mView.findViewById(R.id.userName);
            videoView=mView.findViewById(R.id.userVideo);

            LikeButton=mView.findViewById(R.id.likeButton);
            CommentButton=mView.findViewById(R.id.commentButton);
            Date=mView.findViewById(R.id.post_date);
            Time=mView.findViewById(R.id.post_time);

            LikesNo=(TextView) mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();


        }

        public void setFullname(String fullname)
        {

            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.userImage);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {

            Time.setText(""+time+"");

        }

        public void setDate(String date)
        {

            Date.setText(""+date+"");

        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.descrip);
            PostDescription.setText(description);
        }

        public void setPostvideo(Context ctx,String postvideo)
        {

            if (postvideo!=null)
            {
                videoView.setVideoURI(Uri.parse(postvideo));

            }
            else
            {
                //username.setText("nothinfg");
            }

           // videoView.start();
           // videoView.requestFocus();
            //videoView.start();
        }

        public void setLikeButtonStatus(final String PostKey)
        {


            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {

                        countLikes=(int)  dataSnapshot.child(PostKey).getChildrenCount();

                        LikeButton.setImageResource(R.drawable.likes);

                        LikesNo.setText(Integer.toString(countLikes)+(" "));

                    }else
                    {

                        countLikes=(int)  dataSnapshot.child(PostKey).getChildrenCount();

                        LikeButton.setImageResource(R.drawable.like);

                        LikesNo.setText(Integer.toString(countLikes)+(" "));


                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }


}
