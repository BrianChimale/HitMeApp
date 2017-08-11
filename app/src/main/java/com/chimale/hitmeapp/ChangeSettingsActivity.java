package com.chimale.hitmeapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeSettingsActivity extends AppCompatActivity {

    private Toolbar mAppToolbar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //ProgressDialog
    private ProgressDialog mProgressDialog;

    //FirebaseDatabase reference
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_settings);

        //Retrieve passed value from the calling intent: e.g. Status
        String status_value = getIntent().getStringExtra("status_value");

        //Android views objects
        mStatus = (TextInputLayout) findViewById(R.id.change_status_input);
        mStatus.getEditText().setText(status_value);

        //Firebase UID database
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(uid);

        //AppBar setup
        mAppToolbar = (Toolbar) findViewById(R.id.change_settings_appbar);
        setSupportActionBar(mAppToolbar);
        getSupportActionBar().setTitle("Change Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSaveBtn = (Button) findViewById(R.id.changes_save_btn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ProgressDialog object
                mProgressDialog = new ProgressDialog(ChangeSettingsActivity.this);
                mProgressDialog.setTitle("Saving changes");
                mProgressDialog.setMessage("Please wait as your changes are being saved...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                String status = mStatus.getEditText().getText().toString();
                mUserDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            mProgressDialog.dismiss();

                        }else {

                            mProgressDialog.hide();
                            Toast.makeText(ChangeSettingsActivity.this, "There were some errors in saving the changes", Toast.LENGTH_LONG).show();

                        }
                    }
                });

            }
        });
    }
}
