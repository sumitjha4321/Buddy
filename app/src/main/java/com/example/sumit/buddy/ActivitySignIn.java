package com.example.sumit.buddy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ActivitySignIn extends AppCompatActivity {

    private EditText et_email;
    private EditText et_password;
    private Button mBtnLogin;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // TODO: on back pressed, without logging in..end the application and show app drawer...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    // user signed in...
                    mProgressDialog.dismiss();
                    Intent intent = new Intent(ActivitySignIn.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    //user signed out...
                }

            }
        };

        et_email = (EditText) findViewById(R.id.sign_in_email);
        et_password = (EditText) findViewById(R.id.sign_in_password);
        mBtnLogin = (Button) findViewById(R.id.sign_in_btn);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.setTitle("Signing in");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();

                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                // TODO VALIDATE EMAIL

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    mProgressDialog.dismiss();
                                    new AlertDialog.Builder(ActivitySignIn.this)
                                            .setMessage("Invalid credentials")
                                            .setMessage("Incorrect email/password. Please try again")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }
}
