/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.emojify;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.SaveSettings;




public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY_PHOTO = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;

    private Button mEmojifyButton;
    private FloatingActionButton mShareFab;
    private FloatingActionButton mSaveFab;
    private FloatingActionButton mClearFab;

    private TextView mTitleTextView;

    private String mTempPhotoPath;
    private String mPhotoEditorViewPath;

    private Bitmap mResultsBitmap;
    private Bitmap backgroundBitmap;
    private Bitmap QR_CodeBitmap;

    int newEmojiWidth ;
    int newEmojiHeight ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        // Bind the views
        mPhotoEditorView = (PhotoEditorView) findViewById ( R.id.image_view );
        mEmojifyButton = (Button) findViewById ( R.id.emojify_button );
        mShareFab = (FloatingActionButton) findViewById ( R.id.share_button );
        mSaveFab = (FloatingActionButton) findViewById ( R.id.save_button );
        mClearFab = (FloatingActionButton) findViewById ( R.id.clear_button );
        mTitleTextView = (TextView) findViewById ( R.id.title_text_view );

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build(); // build photo editor sdk


    }

    /**
     * OnClick method for "Emojify Me!" Button. Launches the camera app.
     *
     * @param view The emojify me button.
     */
    public void emojifyMe(View view) {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission ( this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )
            != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions ( this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION );
        } else {
            // Launch the camera if the permission exists
            selectImage ();
//            launchCamera();
//          launchGallery();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
//                    launchCamera();
                    launchGallery ();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText ( this, R.string.permission_denied, Toast.LENGTH_SHORT ).show ();
                }
                break;
            }
        }
    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent ( MediaStore.ACTION_IMAGE_CAPTURE );


        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity ( getPackageManager () ) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile ( this );
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace ();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath ();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile ( this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile );


                // Add the URI so the camera can store the image
                takePictureIntent.putExtra ( MediaStore.EXTRA_OUTPUT, photoURI );

                // Launch the camera activity
                startActivityForResult ( takePictureIntent, REQUEST_IMAGE_CAPTURE );
            }
        }
    }

    private void launchGallery() {
        //launch gallery via intent
        Intent takePictureIntent = new Intent ( Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );


        takePictureIntent.addFlags ( Intent.FLAG_GRANT_READ_URI_PERMISSION );

        takePictureIntent.setType ( "image/*" );

        // select picture
        startActivityForResult ( takePictureIntent, REQUEST_GALLERY_PHOTO );


    }


    /**
     * Get real file path from URI
     *
     * @param contentUri
     * @return
     */
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver ().query ( contentUri, filePathColumn, null, null, null );
            assert cursor != null;
           cursor.moveToPosition ( 0 );
            int column_index = cursor.getColumnIndexOrThrow ( filePathColumn[0] );
            cursor.moveToFirst ();
            return cursor.getString ( column_index );
        } finally {
            if (cursor != null) {
                cursor.close ();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == REQUEST_IMAGE_CAPTURE) {


        } else if (requestCode == REQUEST_GALLERY_PHOTO) {

            // Get the content URI for the image file
            Uri photoURI = data.getData ();

            // Get the path of the temporary file
            mTempPhotoPath = getRealPathFromUri ( photoURI );
        }




        // If the image capture activity was called and was successful

        if (resultCode == RESULT_OK) {

            // Toggle Visibility of the views
            mEmojifyButton.setVisibility ( View.GONE );
            mTitleTextView.setVisibility ( View.GONE );
            mSaveFab.setVisibility ( View.VISIBLE );
            mShareFab.setVisibility ( View.VISIBLE );
            mClearFab.setVisibility ( View.VISIBLE );

            // Resample the saved image to fit the ImageView
            backgroundBitmap = BitmapUtils.resamplePic ( this, mTempPhotoPath );
            if (backgroundBitmap == null) {


                Toast.makeText ( this, "Invalid photo Please choose another photo", Toast.LENGTH_LONG ).show ();
//
            }
            String text2Qr = "abdalfattah";
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter ();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode ( text2Qr, BarcodeFormat.QR_CODE, 200, 200 );
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder ();
                Bitmap bitmap = barcodeEncoder.createBitmap ( bitMatrix );
                QR_CodeBitmap = bitmap;

            } catch (WriterException e) {
                e.printStackTrace ();
            }

            try {

                mResultsBitmap = Bitmap.createBitmap ( backgroundBitmap.getWidth (), backgroundBitmap
                        .getHeight (), backgroundBitmap.getConfig () );

                // Determine the size of the emoji to match the width of the face and preserve aspect ratio
                newEmojiWidth = 1000 - (backgroundBitmap.getWidth ())/2;
                newEmojiHeight = 1000 - (backgroundBitmap.getHeight ())/6;



                // Scale the emoji
                QR_CodeBitmap = Bitmap.createScaledBitmap ( QR_CodeBitmap, newEmojiWidth, newEmojiHeight, false );

//                // Set the new bitmap to the ImageView
                mPhotoEditorView.getSource().setImageBitmap(backgroundBitmap);
                mPhotoEditor.addImage (QR_CodeBitmap);
            } catch (Exception e) {
                Log.v ( "ERROR", e.toString () );
            }




    } else

    {

        // Otherwise, delete the temporary image file
        BitmapUtils.deleteImageFile ( this, mTempPhotoPath );
    }

}

    @Override
    public void onClick(View v) {

    }


    /**
     * Alert dialog for capture or select from galley
     */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Take Photo")) {
                launchCamera();

            } else if (items[item].equals("Choose from Library")) {
                launchGallery();
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * OnClick method for the save button.
     *
     * @param view The save button.
     */
    public void saveMe(View view) {

        // Delete the temporary image file
          BitmapUtils.deleteImageFile(this, mTempPhotoPath);

        // Save the image
         BitmapUtils.saveImage(this,mPhotoEditor,mPhotoEditorView);

    }

    /**
     * OnClick method for the share button, saves and shares the new bitmap.
     *
     * @param view The share button.
     */
    public void shareMe(View view) {

        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);

        // Share the image
        BitmapUtils.ShereImage(this,mPhotoEditor);
    }

    /**
     * OnClick for the clear button, resets the app to original state.
     *
     * @param view The clear button.
     */
    public void clearImage(View view) {
        // Clear the image and toggle the view visibility
        mPhotoEditorView.getSource().setImageResource(0);
        mPhotoEditor.clearAllViews ();
        mEmojifyButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.GONE);
        mClearFab.setVisibility(View.GONE);

        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);

    }

}
