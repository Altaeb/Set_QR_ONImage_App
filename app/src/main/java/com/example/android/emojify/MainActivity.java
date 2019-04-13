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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener  {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    private ImageView mImageView;

    private Button mEmojifyButton;
    private FloatingActionButton mShareFab;
    private FloatingActionButton mSaveFab;
    private FloatingActionButton mClearFab;

    private TextView mTitleTextView;

    private String mTempPhotoPath;

    private Bitmap mResultsBitmap;
    private Bitmap backgroundBitmap;
    private Bitmap QR_CodeBitmap;
    Canvas canvas;
    Matrix matrix;
    Paint paint ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the views
        mImageView = (ImageView) findViewById(R.id.image_view);
        mEmojifyButton = (Button) findViewById(R.id.emojify_button);
        mShareFab = (FloatingActionButton) findViewById(R.id.share_button);
        mSaveFab = (FloatingActionButton) findViewById(R.id.save_button);
        mClearFab = (FloatingActionButton) findViewById(R.id.clear_button);
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);

        mImageView.setOnTouchListener(this);
    }

    /**
     * OnClick method for "Emojify Me!" Button. Launches the camera app.
     *
     * @param view The emojify me button.
     */
    public void emojifyMe(View view) {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
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
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, intent);
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        if (resultCode == RESULT_OK) {

            // Toggle Visibility of the views
            mEmojifyButton.setVisibility(View.GONE);
            mTitleTextView.setVisibility(View.GONE);
            mSaveFab.setVisibility(View.VISIBLE);
            mShareFab.setVisibility(View.VISIBLE);
            mClearFab.setVisibility(View.VISIBLE);

            // Resample the saved image to fit the ImageView
            backgroundBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);
            String text2Qr = "abdalfattah";
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try{
                BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,200,200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                QR_CodeBitmap=bitmap;

            }
            catch (WriterException e){
                e.printStackTrace();
            }

            try {

                mResultsBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap
                        .getHeight(), backgroundBitmap.getConfig());
                    Log.d ("Width of bakground :","i ="+ backgroundBitmap.getWidth()  );
                Log.d ("height of bakground :","i ="+ backgroundBitmap.getHeight ()  );
               // Determine the size of the emoji to match the width of the face and preserve aspect ratio
                   int newEmojiWidth = 1500 ;
                   int newEmojiHeight = 1500 ;
                // Scale the emoji
                QR_CodeBitmap = Bitmap.createScaledBitmap (QR_CodeBitmap, newEmojiWidth,newEmojiHeight, false);

                canvas = new Canvas(mResultsBitmap);

                matrix = new Matrix ();
                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(5);
                canvas.drawBitmap(backgroundBitmap, 0, 0, null);
                // https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
                paint.setXfermode(new PorterDuffXfermode ( PorterDuff.Mode.XOR));
                canvas.drawBitmap (QR_CodeBitmap, upx,upy, null);

                // Set the new bitmap to the ImageView
                mImageView.setImageBitmap(mResultsBitmap);
            } catch (Exception e) {
                Log.v("ERROR", e.toString());
            }
        }

        } else {

            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }
    }
    @Override
    public void onClick(View v) {

    }
    float downx = 0;
    float downy = 0;
    float upx =3840;
    float upy =2160;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d ( "X", "x : "+ event.getX () );
                downx = event.getX();
                downy = event.getY();
                Log.d ( "Y", "y : "+ event.getY () );
                break;
            case MotionEvent.ACTION_MOVE:
                upx = event.getX();
                Log.d ( "X","x" +  upx );
                upy = event.getY();
                Log.d ( "Y","y" +  upy );
                canvas.drawBitmap(backgroundBitmap, 0, 0, null);
                canvas.drawBitmap (QR_CodeBitmap, upx,upy, null);
//                canvas.drawLine(downx, downy, upx, upy, paint);
//                mImageView.invalidate();
                mImageView.setImageBitmap(mResultsBitmap);
                break;
//            case MotionEvent.ACTION_UP:
//                upx = event.getX();
//                upy = event.getY()> 300? 1500: event.getY ();
//                canvas.drawBitmap(backgroundBitmap, 0, 0, null);
//                canvas.drawBitmap (QR_CodeBitmap, upx,upy, null);
////                canvas.drawLine(downx, downy, upx, upy, paint);
////                mImageView.invalidate();
//                mImageView.setImageBitmap(mResultsBitmap);
//                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }

        return true;
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
        BitmapUtils.saveImage(this, mResultsBitmap);
    }

    /**
     * OnClick method for the share button, saves and shares the new bitmap.
     *
     * @param view The share button.
     */
    public void shareMe(View view) {
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);

        // Save the image
        BitmapUtils.saveImage(this, mResultsBitmap);

        // Share the image
        BitmapUtils.shareImage(this, mTempPhotoPath);
    }

    /**
     * OnClick for the clear button, resets the app to original state.
     *
     * @param view The clear button.
     */
    public void clearImage(View view) {
        // Clear the image and toggle the view visibility
        mImageView.setImageResource(0);
        mEmojifyButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.GONE);
        mClearFab.setVisibility(View.GONE);

        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
    }


    /**
     * Method for processing the captured image and setting it to the TextView.
     */
//    private void processAndSetImage() {
//
//        // Toggle Visibility of the views
//        mEmojifyButton.setVisibility(View.GONE);
//        mTitleTextView.setVisibility(View.GONE);
//        mSaveFab.setVisibility(View.VISIBLE);
//        mShareFab.setVisibility(View.VISIBLE);
//        mClearFab.setVisibility(View.VISIBLE);
//
//        // Resample the saved image to fit the ImageView
//        mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);
//        String text2Qr = "abdalfattah";
//        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
//        try{
//            BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,200,200);
//            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
//            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
//            // Detect the faces and overlay the appropriate emoji
//            mResultsBitmap =addBitmapToFace(mResultsBitmap,bitmap);
//        }
//        catch (WriterException e){
//            e.printStackTrace();
//        }
//
//        // Set the new bitmap to the ImageView
//        mImageView.setImageBitmap(mResultsBitmap);
//    }
//
//    static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap foregroundBitmap) {
//
//        // Initialize the results bitmap to be a mutable copy of the original image
//        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
//                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());
//
//
//
//        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
//        int newEmojiWidth = 1500 ;
//        int newEmojiHeight = 1500 ;
//
//
//        // Scale the emoji
//        foregroundBitmap = Bitmap.createScaledBitmap(foregroundBitmap, newEmojiWidth, newEmojiHeight, false);
//
//        // Determine the emoji position so it best lines up with the face
//        float emojiPositionX =100;
//
//        float emojiPositionY =100;
//
//
//        // Create the canvas and draw the bitmaps to it
//        Canvas canvas = new Canvas(resultBitmap);
//        Paint paint = new Paint();
//        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
//        // https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
//        paint.setXfermode(new PorterDuffXfermode ( PorterDuff.Mode.ADD));
//        canvas.drawBitmap(foregroundBitmap, emojiPositionX, emojiPositionY, null);
//
//
//
//        return resultBitmap;
//    }
}
