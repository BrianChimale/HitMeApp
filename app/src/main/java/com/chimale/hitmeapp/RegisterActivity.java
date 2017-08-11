package com.chimale.hitmeapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //Firebase auth
    private FirebaseAuth mAuth;

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateButton;

    //ProgressDialog
    private ProgressDialog mRegisterDialog;

    //Register activity toolbar
    private Toolbar mToolbar;

    //Firebase database
    private DatabaseReference mDatabase;

    //Error handling
    String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ProgressDialog object initialization
        mRegisterDialog = new ProgressDialog(this);

        //Toolbar set
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase auth
        mAuth = FirebaseAuth.getInstance();

        //Android fields
        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateButton = (Button) findViewById(R.id.reg_create_btn);

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                    mRegisterDialog.setTitle("Registering User");
                    mRegisterDialog.setMessage("Please wait while we register your account !");
                    mRegisterDialog.setCanceledOnTouchOutside(false);
                    mRegisterDialog.show();

                    register_user(display_name, email, password);

                }

            }
        });

    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    //Get instance of the current user
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    //Get the current user UID as a String
                    String uid = current_user.getUid();

                    //Create a database reference instance from the root, through a path to current reference
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(uid);

                    //To add more than one value to the current reference, use a key, value Hashmap to add these values
                    HashMap<String, String> userMap = new HashMap<>();

                    //Add the values to the Hashmap.
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi there, I'm using HitMeApp");
                    userMap.put("image", "default");
                    userMap.put("thumbnail", "default");

                    //Add the Hashmap object to the database reference.
                    //Add the on complete listener that switches to the main activity if registration is successful.
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                mRegisterDialog.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }
                        }
                    });

                }else{
                    mRegisterDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot sign in. Please check the form and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
