package com.chimale.hitmeapp;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;


public class ProfileActivity extends AppCompatActivity {

    //Android views
    private ImageView mpProfileImage;
    private TextView mProfileDisplayName, mProfileStatus, mProfileFriendCnt;
    private Button mProfileSendRqstBtn, mProfileDeclineRqstBtn;

    //Firebase Database reference to User data
    private DatabaseReference mDatabase;

    //Firebase Database reference to Friend request data
    private DatabaseReference mFriendReqDatabase;

    //Firebase Database reference to Friends data
    private DatabaseReference mFriendDatabase;

    //Firebase Database reference to Notification data
    private DatabaseReference mNotificationDatabase;

    //Firebase User to get current user's data, i.e uid
    private FirebaseUser mCurrentUser;

    //ProgressDialog
    private ProgressDialog mProgress;

    //Flag to check if the chosen user is the current_user's friend
    //The current user's state is denoted numerically as shown:
    //0 - not friends state
    //1 - friend request state
    //2 - friend request received state
    //3 - friends state
    private int mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Firebase Friend Requests Database reference object initialization
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        //Firebase Friend Database reference object initialization
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        //Firebase Notification Database reference object initialization
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        //Firebase User object instantiation
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Android views object initialization
        mpProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileDisplayName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendCnt = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendRqstBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mProfileDeclineRqstBtn = (Button) findViewById(R.id.profile_decline_req_btn);
        mProfileDeclineRqstBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineRqstBtn.setEnabled(false);

        //Set the default state to be "not friends" denoted by 0
        mCurrent_state = 0;

        //ProgressDialog object initialization
        mProgress = new ProgressDialog(ProfileActivity.this);

        mProgress.setTitle("Loading User Profile");
        mProgress.setMessage("Please wait as the user's profile is being loaded");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();



        //Retrieve the user id from the calling intent
        final String user_id= getIntent().getStringExtra("user_id");

        // Firebase Users Database reference object initialization
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (!image.equals("default")){
                    Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_male_avatar).into(mpProfileImage);
                }
                mProfileDisplayName.setText(name);
                mProfileStatus.setText(status);

                //-----------------FRIENDS LIST/REQUEST FEATURE------------------------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Check if the current user is not friends with the other user
                        if (dataSnapshot.hasChild(user_id)){

                            String request_type =  dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (request_type.equals("received")){

                                //Change the current state to be "friend request received state" denoted by 2
                                mCurrent_state = 2;
                                //Change the text on the button
                                mProfileSendRqstBtn.setText("ACCEPT FRIEND REQUEST");

                                mProfileDeclineRqstBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineRqstBtn.setEnabled(true);

                            } else if (request_type.equals("sent")){

                                //Change the current state to be "friend request state" denoted by 1
                                mCurrent_state = 1;
                                //Change the text on the button
                                mProfileSendRqstBtn.setText("CANCEL FRIEND REQUEST");

                                mProfileDeclineRqstBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineRqstBtn.setEnabled(false);


                            }
                            mProgress.dismiss();

                        }else{

                            //if the other user is already the current user's friend,
                            //change the current user's state and the button's text value correspondingly
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)){

                                        //Current user's state is friends
                                        mCurrent_state = 3;
                                        //Change the text on the button
                                        mDatabase.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String name = dataSnapshot.child("name").getValue().toString();
                                                mProfileSendRqstBtn.setText("UNFRIEND " + name.toUpperCase());

                                                mProfileDeclineRqstBtn.setVisibility(View.INVISIBLE);
                                                mProfileDeclineRqstBtn.setEnabled(false);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                    mProgress.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgress.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendRqstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Disable the button so that the user doesn't send another friend request before the current one is complete
                mProfileSendRqstBtn.setEnabled(false);

                //-------------------------------NOT FRIEND STATE---------------------------------------
                //Check if the other user is not the current user's friend
                if (mCurrent_state == 0){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrentUser.getUid());
                                        notificationData.put("type", "request");

                                        //push() method generates a random number
                                        mNotificationDatabase.child(user_id).push().setValue(notificationData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                //Change the current state to be "friend request state" denoted by 1
                                                mCurrent_state = 1;
                                                //Change the text on the button
                                                mProfileSendRqstBtn.setText("CANCEL FRIEND REQUEST");

                                                mProfileDeclineRqstBtn.setVisibility(View.INVISIBLE);
                                                mProfileDeclineRqstBtn.setEnabled(false);

                                                Toast.makeText(ProfileActivity.this, "Friend request sent successfully", Toast.LENGTH_LONG).show();

                                            }
                                        });


                                    }
                                });

                            }else {

                                Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_LONG).show();

                            }

                            //Reenable the button once the request completes sending
                            mProfileSendRqstBtn.setEnabled(true);
                        }
                    });
                }

                //-------------------------------CANCEL REQUEST STATE----------------------------------------
                if (mCurrent_state == 1){

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //If the friend request is successfully removed from the current user's friend request database,
                            //remove it from the other user's database
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    //Reenable the button once the request completes sending
                                    mProfileSendRqstBtn.setEnabled(true);
                                    //Change the current state to be "friend request state" denoted by 0
                                    mCurrent_state = 0;
                                    //Change the text on the button
                                    mProfileSendRqstBtn.setText("SEND FRIEND REQUEST");

                                    mProfileDeclineRqstBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineRqstBtn.setEnabled(false);

                                    Toast.makeText(ProfileActivity.this, "Friend request successfully cancelled", Toast.LENGTH_LONG).show();

                                }
                            });

                        }
                    });

                }

                //------------------------------------------REQUEST RECEIVED STATE-------------------------------------------------------------
                if (mCurrent_state == 2 ){

                    //If the user clicks this button to ACCEPT the other user's friend request, on the friends database,
                    //add the other other user's id with the value of the current date, all within the current user's id,
                    //i.e.Friends{
                    //              current_uid{
                    //                          other_user_id: current_date
                    //                         }
                    //           }

//                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    //Instead of the current date-time, use Firebase Timestamp
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(ServerValue.TIMESTAMP)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    //Remove the sent friend request from database
                                    mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            //Remove the received friend request from database
                                            mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    //Reenable the button once the request completes sending
                                                    mProfileSendRqstBtn.setEnabled(true);
                                                    //Change the current state to be "friends state" denoted by 3
                                                    mCurrent_state = 3;

                                                    //Toast the name of the current user's new friend and change the text on the button
                                                    mDatabase.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            String name = dataSnapshot.child("name").getValue().toString();
                                                            Toast.makeText(ProfileActivity.this, "Friend request accepted successfully. You are now friends with "+name, Toast.LENGTH_LONG).show();
                                                            //Change the text on the button
                                                            mProfileSendRqstBtn.setText("UNFRIEND " + name.toUpperCase());

                                                            mProfileDeclineRqstBtn.setVisibility(View.INVISIBLE);
                                                            mProfileDeclineRqstBtn.setEnabled(false);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }
                                            });

                                        }
                                    });
                                }
                            });
                        }
                    });

                }


            }
        });
    }
}
