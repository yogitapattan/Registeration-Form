package com.example.shivamgupta.firebaseapp;


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText Name;
    private EditText Password;
    //private TextView Info;
    private Button Login;
    private int counter = 5;
    private TextView userRegistration,passwordReset;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Name = (EditText) findViewById(R.id.etName);
        Password = (EditText)findViewById(R.id.etUserEmail);
        //Info = (TextView)findViewById(R.id.tvinfo);
        Login = (Button)findViewById(R.id.btnLogin);
        userRegistration = (TextView)findViewById(R.id.etRegister);
        passwordReset = (TextView) findViewById(R.id.etForgot);
        //Info.setText(getString(R.string.attempts,5));
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            finish();
            startActivity(new Intent(MainActivity.this, FormActivity.class));
        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                validate(Name.getText().toString(), Password.getText().toString());
            }
        });

        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        passwordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,PasswordReset.class));
            }
        });

    }

    private void validate(String userName, String userPassword){

        if(userName.isEmpty() || userPassword.isEmpty()){
            Toast.makeText(MainActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        if(userName.equals("Admin") && userPassword.equals("password")){
            startActivity(new Intent(MainActivity.this,AdminActivity.class));
            return ;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(userName,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    checkEmailVerification();
                }else{
                    Toast.makeText(MainActivity.this,"Login Unsuccessful",Toast.LENGTH_SHORT).show();
                    /*counter--;
                    Info.setText(getString(R.string.attempts,counter));
                    if(counter==0){
                        Login.setEnabled(false);
                    }*/
                }
            }
        });
    }

    private void checkEmailVerification() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Boolean emailFlag = firebaseUser.isEmailVerified();

        if(emailFlag) {
            startActivity(new Intent(MainActivity.this, FormActivity.class));
        }else {
            Toast.makeText(this, "Verify Your Email", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }

    }

}