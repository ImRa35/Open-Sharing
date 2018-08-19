package com.rbk.unlock;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{

    private EditText EmailText,PasswordText;
    private Button CreateButton,LoginButton;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        EmailText=findViewById(R.id.editTextEmail);
        PasswordText=findViewById(R.id.editTextPassword);
        CreateButton=findViewById(R.id.buttonCreate);
        LoginButton=findViewById(R.id.buttonLogin);

        loadingBar=new ProgressDialog(this);


        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailText.getText().toString();
                String password=PasswordText.getText().toString();

                if (TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this, "Email", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(LoginActivity.this, "Password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateAccount(email,password);
                }

            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email=EmailText.getText().toString();
                String password=PasswordText.getText().toString();

                if (TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this, "Email", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(LoginActivity.this, "Password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Login(email,password);
                }


            }
        });



    }

    private void Login(String email, String password)
    {
        loadingBar.setTitle("Login");
        loadingBar.setMessage("Please Wait");
        loadingBar.show();
        loadingBar.setCancelable(false);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {

                if (task.isSuccessful())
                {
                    loadingBar.dismiss();
                    SendUserToHomeActivity();
                }
                else
                {
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void CreateAccount(final String email, final String password)
    {

        loadingBar.setTitle("Creating Account");
        loadingBar.setMessage("Please Wait");
        loadingBar.show();
        loadingBar.setCancelable(false);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {

                if (task.isSuccessful())
                {
                    loadingBar.dismiss();
                    SendUserToSetupActivity(email,password);
                }
                else
                {
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void SendUserToSetupActivity(String email, String password)
    {
        startActivity(new Intent(LoginActivity.this,SetupActivity.class).putExtra("email",email).putExtra("password",password));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            SendUserToHomeActivity();
        }

    }

    private void SendUserToHomeActivity()
    {
        startActivity(new Intent(LoginActivity.this,HomeActivity.class));
    }
}
