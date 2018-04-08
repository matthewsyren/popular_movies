package com.matthewsyren.popularmovies.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Provides functions associated with Bitmaps
 */

public class BitmapUtilities {
    /* Converts a Bitmap to a byte array and returns the array
     * Adapted from https://stackoverflow.com/questions/13758560/android-bitmap-to-byte-array-and-back-skimagedecoderfactory-returned-null?lq=1
     */
    public static byte[] getByteArrayFromBitmap(Bitmap bitmap){
        if(bitmap != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

    /* Converts a byte array to a Bitmap
     * Adapted from https://stackoverflow.com/questions/13854742/byte-array-of-image-into-imageview
     */
    public static Bitmap getBitmapFromByteArray(byte[] poster){
        return BitmapFactory.decodeByteArray(poster, 0, poster.length);
    }
}