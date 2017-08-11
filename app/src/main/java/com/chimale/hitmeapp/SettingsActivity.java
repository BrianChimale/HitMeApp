package com.chimale.hitmeapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.R.attr.bitmap;

@SuppressWarnings("VisibleForTests")
public class SettingsActivity extends AppCompatActivity {

    //Used to define the max length of strings in nameGenerator() method, useless now
    private static final int MAX_LENGTH = 50;

    //Database reference
    private DatabaseReference mUserDatabase;

    //Firebase current user
    private FirebaseUser currentUser;

    //Firebase Storage
    private StorageReference mImageStorage;

    //Android views
    private CircleImageView mDisplayImage;
    private TextView mDisplayName;
    private TextView mStatus;

    private Button mChangeSettingsBtn;
    private Button mChangeProfileBtn;

    //ProgressDialog
    private ProgressDialog mProgress;

    //Integer constant for gallery intent request code, useless now
//    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //FirebaseStorage reference object
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Initialize android components objects
        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mDisplayName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);

        //Button to change the current settings
        mChangeSettingsBtn = (Button) findViewById(R.id.settings_status_btn);
        mChangeSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();
                Intent changeSettingsIntent = new Intent(SettingsActivity.this, ChangeSettingsActivity.class);
                changeSettingsIntent.putExtra("status_value", status_value);

                startActivity(changeSettingsIntent);

            }
        });

        //Button to change the profile picture
        mChangeProfileBtn = (Button) findViewById(R.id.settings_image_btn);
        mChangeProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

/*      Alternative intent to access local image files and return the selected image

                //Intent to get image from the gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
*/
            // start picker to get image for cropping and then use the image in cropping activity
                //This method covers both image selection and cropping with the additional option of using the device camera
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
                }
        });

        //Get the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Get user UID as a String from currentUser variable
        String current_uid = currentUser.getUid();

        //Current user UID database reference: accesses root/User/UID
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(current_uid);
        //Keeps the whole mUserDatabase query synced.
        //Remember if only stores the database queries, not storage, i.e. photos
        mUserDatabase.keepSynced(true);

        //Retrieve current user's database value:   name, status, image, thumbnail
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                //the database values are then assigned to the TextViews
                mDisplayName.setText(name);
                mStatus.setText(status);

                //Check if an image exists in storage, otherwise leave the default image in the ImageView
                if (!image.equals("default")) {
                    //Fetches image from storage and loads it to the circle image viewer
                    //The placeholder method makes sure the image viewer always has an image as the new image is being rendered/loaded.
                    // Original: Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_male_avatar).into(mDisplayImage);
                    //The networkPolicy(NetworkPolicy.OFFLINE) method inplements the Picasso offline feature
                    //This checks for the image offline first using the new Callback() meethod. If it fails, it then checks online
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_male_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            //If successful, do nothing since image is found offline
                        }

                        @Override
                        public void onError() {
                            //If the image is not available or cannot be retrieved offline, do it online
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_male_avatar).into(mDisplayImage);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

/*If the alternative intent to retrieve image is used, it returns the image to this activity which then resends it with the new intent to be cropped out

        //After image is retrieved, it is sent to be cropped to the CropImage library
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            // Start cropping activity for pre-acquired image saved on the device
            // The setAspectRatio method makes sure the cropped image is square
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(SettingsActivity.this);

        }
*/
        // The now cropped image is then saved in Firebase Storage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                // Start the ProgressDialog
                mProgress = new ProgressDialog(SettingsActivity.this);
                mProgress.setTitle("Uploading Profile Picture");
                mProgress.setMessage("Please wait as we upload and process your profile picture");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                //The URI of the cropped image
                final Uri resultUri = result.getUri();

                //Actual image file
                File thumb_file = new File(resultUri.getPath());

                //Get uid of the current user
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


                //Use try-catch since the compress() method has to defined within to catch exceptions
                try {

                    //Compress the actual image file to a bitmap thumbnail
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);


                    //Transfer the compressed thumbnail bitmap to the Firebase storage
                    //This is done byte by byte
                    //Define within try-catch since scope of thumb_bitmap is within it
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();



                    //The actual storage files within FirebaseStorage
                    //File path that stores the actual image.
                    StorageReference filePath = mImageStorage.child("profile_images").child(uid + ".jpg");
                    //File path that stores the bitmap thumbnail.
                    final StorageReference thumb_filePath = mImageStorage.child("profile_images").child("thumbs").child(uid+".jpg");


                    //Store/upload the main image into FirebaseStorage
                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()){

                                //Get the download url of the main image file location within Firebase storage
                                final String download_link = task.getResult().getDownloadUrl().toString();

                                //Create upload task to upload thumbnail bitmap to the Firebase storage
                                UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        //Get the download url of the THUMBNAIL bitmap file location within Firebase storage
                                        String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()) {

                                            //Map to hold both main image and thumbnail urls
                                            Map update_Url = new HashMap<>();
                                            update_Url.put("image", download_link);
                                            update_Url.put("thumbnail", thumb_download_url);

                                            //Save the file location within the Firebase database
                                            mUserDatabase.updateChildren(update_Url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        mProgress.dismiss();
                                                        Toast.makeText(SettingsActivity.this, "Success uploading", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                        }else {

                                            mProgress.dismiss();
                                            Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail", Toast.LENGTH_LONG).show();

                                        }

                                    }
                                });

                            } else {
                                mProgress.dismiss();
                                Toast.makeText(SettingsActivity.this, "Error in uploading", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                } catch (IOException e) {
                    Toast.makeText(SettingsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    // Method to generate a string of random alphanumeric characters
    public static String nameGenerator(){
        String letters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789";
        Random random = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = random.nextInt(MAX_LENGTH);

        for (int i=0; i < randomLength; i++){
            randomStringBuilder.append(letters.charAt(random.nextInt(letters.length())));
        }

        return randomStringBuilder.toString();
    }


}
