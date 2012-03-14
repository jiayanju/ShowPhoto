package com.jyj.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.jyj.util.ImageUtil;

public class ImageDiskCache implements DiskCache {
    private static final String TAG = "ImageDiskCache";
    
    /** The cache root directory */
    private File mStorageDirectory;
    
    public ImageDiskCache(Context context, String name) {
	mStorageDirectory = CreateStorageDirectory(context, name);
    }

    @Override
    public boolean hasCache(String key) {
	return get(key).exists();
    }

    @Override
    public File get(String key) {
	return new File(mStorageDirectory, getFileNameForKey(key));
    }

    @Override
    public InputStream getInputStream(String key) throws FileNotFoundException {
	return (InputStream) new FileInputStream(get(key));
    }
    
    public Bitmap getBitmapFromCache(String key) {
	InputStream is = null;
        Bitmap b = null;
        boolean succeeded = false;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            is = getInputStream(key);
            succeeded = ImageUtil.getImageSize(is, options);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception ocurred. " + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
                is = null;
            }
        }
        if (succeeded) {
            try {
                is = getInputStream(key);
                try {
                    b = ImageUtil.getBitmap(is, options);
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "OutOfMemoryError when docoding image");
                }
            } catch (FileNotFoundException e) {
        	Log.e(TAG, "Exception ocurred. " + e.getMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                    is = null;
                }
            }
        }
        return b;
    }

    @Override
    public void cacheFile(String key, InputStream inputStream){
	BufferedInputStream bis = new BufferedInputStream(inputStream);
	BufferedOutputStream bos = null;
	
	try {
	    bos = new BufferedOutputStream(new FileOutputStream(get(key)));
	    
	    byte [] bytes = new byte [1024];
	    int count = 0;
	    while ((count = bis.read(bytes)) != -1) {
		bos.write(bytes, 0, count);
	    }
	    
	} catch (FileNotFoundException e) {
	    Log.v(TAG, "File not found exception when store " + key, e);
	} catch (IOException e) {
	    Log.v(TAG, "error occured when store the " + key, e);
	} finally {
	    try {
		if (bis != null) {
		    bis.close();
		}
		if (bos !=  null) {
		    bos.close();
		}
	    } catch (IOException e) {
		Log.v(TAG, "error occured when close the stream", e);
	    }
	}
	
    }
    
    /**
     * create the cache directory
     * 
     * @param context
     * @param name
     * @return
     */
    private File CreateStorageDirectory(Context context, String name) {
	
	File storageDirectory = null;
	if (Environment.MEDIA_MOUNTED.equals(Environment
		.getExternalStorageState())) {
	    String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
		    + File.separator + name;
	    storageDirectory = new File(rootDir);
	} else {
	    storageDirectory = new File(context.getCacheDir().getAbsolutePath());
	}
	
	if (!storageDirectory.exists() && !storageDirectory.mkdirs()) {
	    Log.v(TAG, "cann not create the directory " + storageDirectory.getAbsolutePath());
	    throw new RuntimeException("cann not create the directory " + storageDirectory.getAbsolutePath());
	}
	
	return storageDirectory;
    }

    /**
     * get the file name from the url which has special character
     * 
     * @param key
     * @return
     */
    private String getFileNameForKey(String key) {
	return CacheHelper.getFileNameFromUrl(key);
    }
    
}
