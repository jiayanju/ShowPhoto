package com.jyj.util;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageUtil {
    private static final String TAG = "ImageUtil";
    
    private static final boolean DEBUG = ShowPhotoDebug.DEBUG;
    
    public static final int UNCONSTRAINED = -1;
    
    public static final int PROGRESSIVE_MIN_SIDE_LENGTH = 640;

    public static final int PROGRESSIVE_MAX_NUM_OF_PIXELS = (1024 * 768);
    
    /**
     * get image size
     *
     * @param is - InputStream
     * @param options as output (outWidht and outHeight)
     * @return True if succeeded, False else.
     */
    public static boolean getImageSize(InputStream is, BitmapFactory.Options options) {
	if (DEBUG) {
	    Log.d(TAG, "getImageSize");
	}
	
        boolean succeeded = false;
        if (is != null && options != null) {
            // get size of decoded image, then calculate sample size
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            if (options.outWidth > 0 && options.outHeight > 0) {
                if (DEBUG)
                    Log.d(TAG, "outWidth = [" + options.outWidth + "], outHeight = ["
                            + options.outHeight + "]");
                succeeded = true;
            }
        }
        if (DEBUG) {
            Log.v(TAG, "getImageSize/out - [" + succeeded + "]");
        }
        return succeeded;
    }
    
    /**
     * get Bitmap object
     *
     * @param is - InputStream
     * @param options - options as input
     * @return
     */
    public static Bitmap getBitmap(InputStream is, BitmapFactory.Options options) throws OutOfMemoryError {
        return getBitmap(is, options, PROGRESSIVE_MIN_SIDE_LENGTH, PROGRESSIVE_MAX_NUM_OF_PIXELS);
    }
    
    /**
     * get Bitmap object
     *
     * @param is - InputStream
     * @param options - options as input
     * @param minSideLength - minimum side length for the output bitmap
     * @param maxNumOfPixels - max number of pixels for the output bitmap
     * @return - Returns Bitmap
     */
    public static Bitmap getBitmap(InputStream is, BitmapFactory.Options options, 
	    int minSideLength, int maxNumOfPixels)throws OutOfMemoryError{
        if (DEBUG) {
            Log.d(TAG, "getBitmap/in");
        }
        Bitmap bitmap = null;
        if (options != null && is != null) {
            // reset
            options.inSampleSize = computeSampleSize(options.outWidth, options.outHeight, minSideLength, maxNumOfPixels);
            options.inJustDecodeBounds = false;
            options.outHeight = 0;
            options.outWidth = 0;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            if (DEBUG) {
                Log.d(TAG, "getBitmap: inSampleSize = [" + options.inSampleSize + "], outWidth = ["
                        + options.outWidth + "], outHeight = [" + options.outHeight + "]");
            }
        }
        if (DEBUG) {
            Log.v(TAG, "getBitmap/out");
        }
        return bitmap;
    }
    
    /**
     * calculate the sample size
     * 
     * @param outWidth
     * @param outHeight
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    public static int computeSampleSize(int outWidth, int outHeight, int minSideLength,
            int maxNumOfPixels) {
        if (DEBUG) {
            Log.d(TAG, "calculateSampleSize()" + "<in>" + ": outWidth = " + outWidth
                    + ": outHeight = " + outHeight + ": minSideLength = " + minSideLength
                    + ": maxNumOfPixels = " + maxNumOfPixels);
        }

        int initialSize = computeInitialSampleSize(outWidth, outHeight, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        if (DEBUG) {
            Log.d(TAG, "calculateSampleSize()" + "<out>" + ": roundedSize = " + roundedSize);
        }

        return roundedSize;
    }
    
    /**
     * calculate the initial sample size
     * 
     * @param outWidth
     * @param outHeight
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private static int computeInitialSampleSize(int outWidth, int outHeight, int minSideLength,
            int maxNumOfPixels) {
        if (DEBUG) {
            Log.d(TAG, "computeInitialSampleSize()" + "<in>" + ": outWidth = " + outWidth
                    + ": outHeight = " + outHeight + ": minSideLength = " + minSideLength
                    + ": maxNumOfPixels = " + maxNumOfPixels);
        }

        double w = outWidth;
        double h = outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int)Math.ceil(Math.sqrt(w * h
                / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int)Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }

    }
}
