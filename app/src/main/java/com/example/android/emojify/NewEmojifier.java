package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class NewEmojifier  {



    static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap foregroundBitmap) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());



        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = 1500 ;
        int newEmojiHeight = 1500 ;


        // Scale the emoji
        foregroundBitmap = Bitmap.createScaledBitmap(foregroundBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =100;

        float emojiPositionY =100;


        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        // https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
        paint.setXfermode(new PorterDuffXfermode ( PorterDuff.Mode.ADD));
        canvas.drawBitmap(foregroundBitmap, emojiPositionX, emojiPositionY, null);


        return resultBitmap;
    }




}
