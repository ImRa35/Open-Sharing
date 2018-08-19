package com.rbk.unlock;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity
{

    private FirebaseAuth mAuth;
    private FirebaseUser CurrentUser;

    private BottomNavigationView mMainView;
    private FrameLayout MainFrame;
    private HomeFragment homeFragment;
    private PostFragment postFragment;
    private ProfileFragment profileFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth=FirebaseAuth.getInstance();
        CurrentUser=mAuth.getCurrentUser();

        homeFragment=new HomeFragment();
        postFragment=new PostFragment();
        profileFragment=new ProfileFragment();

        setFragment(homeFragment);


        mMainView=findViewById(R.id.main_nav);
        MainFrame=findViewById(R.id.main_frame);

        mMainView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_home:
                        mMainView.setItemBackgroundResource(R.color.colorPrimary);
                        setFragment(homeFragment);
                        return true;

                    case R.id.nav_add:
                        mMainView.setItemBackgroundResource(R.color.colorAccent);
                        setFragment(postFragment);
                        return true;

                    case R.id.nav_profile:
                        mMainView.setItemBackgroundResource(R.color.colorPrimaryDark);
                        setFragment(profileFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });


    }

    private void setFragment(Fragment fragment)
    {
         FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
         fragmentTransaction.replace(R.id.main_frame,fragment);
         fragmentTransaction.commit();

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (CurrentUser==null)
        {
            SendUserToLoginAcivity();
        }


    }

    private void SendUserToLoginAcivity()
    {
        startActivity(new Intent(HomeActivity.this,LoginActivity.class));
        finish();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}
