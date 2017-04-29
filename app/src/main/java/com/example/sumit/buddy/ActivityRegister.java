package com.example.sumit.buddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;


public class ActivityRegister extends AppCompatActivity {

    private static final String TAG = "ActivityRegister";

    private TextInputLayout mLayoutName;
    private EditText mNameField;
    private TextInputLayout mLayoutEmail;
    private EditText mEmailField;
    private TextInputLayout mLayoutPassword;
    private EditText mPasswordField;
    private TextInputLayout mLayoutRepassword;
    private EditText mRepasswordField;
    private Button mBtnRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;

    private static final String URL_EMPTY_PROFILE = "https://firebasestorage.googleapis.com/v0/b/buddy-85260.appspot.com/o/profile_images%2Fempty_profile_image.png?alt=media&token=753d451f-70d9-4c6e-9a6d-83ada757116d";

    private ProgressDialog mProgressDialog;

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgressDialog = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    OneSignal
                            .startInit(getApplicationContext())
                            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.None)
                            .init();

                    OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                        @Override
                        public void idsAvailable(String userId, String registrationId) {

                            DatabaseReference databaseReference = mDatabase.getReference().child("users").child(user.getUid());
                            databaseReference.setValue(new UserDetail(
                                    mEmailField.getText().toString().trim(),
                                    mNameField.getText().toString().trim(),
                                    URL_EMPTY_PROFILE,
                                    null,
                                    userId
                            ));

                            mProgressDialog.dismiss();
                            startActivity(new Intent(ActivityRegister.this, MainActivity.class));
                        }
                    });
                }
            }
        };

        mLayoutName = (TextInputLayout) findViewById(R.id.register_layout_name);
        mLayoutEmail = (TextInputLayout) findViewById(R.id.register_layout_email);
        mLayoutPassword = (TextInputLayout) findViewById(R.id.register_layout_password);
        mLayoutRepassword = (TextInputLayout) findViewById(R.id.register_layout_repassword);
        mBtnRegister = (Button) findViewById(R.id.btn_register);

        mNameField = (EditText) findViewById(R.id.et_register_name);
        mEmailField = (EditText) findViewById(R.id.et_register_email);
        mPasswordField = (EditText) findViewById(R.id.et_register_password);
        mRepasswordField = (EditText) findViewById(R.id.et_register_repassword);

        mNameField.requestFocus();

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameField.getText().toString().trim();
                String email = mEmailField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();
                String repassword = mRepasswordField.getText().toString().trim();

                if (validate(name, email, password, repassword)) {
                    mProgressDialog.setTitle("Signing Up");
                    mProgressDialog.setMessage("Please wait...");
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                mProgressDialog.cancel();
                                Toast.makeText(ActivityRegister.this, "Some error happened. Please try later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


    }

    private boolean validate(String name, String email, String password, String repassword) {
        if (TextUtils.isEmpty(name)) {
            mLayoutName.setError("Name is required");
            mNameField.requestFocus();
            return false;
        }
        mLayoutName.setErrorEnabled(false);
        if (TextUtils.isEmpty(email)) {
            mLayoutEmail.setError("Email ID is required");
            mEmailField.requestFocus();
            return false;

        }
        mLayoutEmail.setErrorEnabled(false);
        if (TextUtils.isEmpty(password)) {
            mLayoutPassword.setError("Password is required");
            mPasswordField.requestFocus();
            return false;

        }
        mLayoutPassword.setErrorEnabled(false);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mLayoutEmail.setError("Please enter valid email");
            mEmailField.requestFocus();
            return false;

        }
        mLayoutEmail.setErrorEnabled(false);

        if (!password.equals(repassword)) {
            mLayoutRepassword.setError("Password does not match");
            mRepasswordField.requestFocus();
            return false;

        }
        mLayoutRepassword.setErrorEnabled(false);

        return true;

    }

}
