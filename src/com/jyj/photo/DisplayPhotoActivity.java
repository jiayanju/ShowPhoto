package com.jyj.photo;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.jyj.cache.ImageDiskCache;
import com.jyj.util.CommonIntent;
import com.jyj.util.HttpUtil;
import com.jyj.util.ShowPhotoDebug;

public class DisplayPhotoActivity extends Activity {
    private static final String TAG = "ShowPhotoActivity";
    
    private static final boolean DEBUG = ShowPhotoDebug.DEBUG;

    /** the message id to mark download and show message */
    private static final int DOWNLOAD_IMAGE_AND_SHOW = 1;

    /** The view to show image */
    private ImageView mImageView;
    
    /** The progress bar */
    private ProgressBar mProgressBar;

    /** The Disk Cache */
    private ImageDiskCache mImageDiskCache;
    
    /** The thread for ViewHandler */
    private HandlerThread mViewHandlerThread;
    
    /** The object of ViewHandler */
    private ViewHandler mViewHandler;
    
    /** The bitmap object of current image. */
    private Bitmap mCurrentImageBitmap = null;

    /** A map to cache bitmap data */
    private ConcurrentHashMap<String, SoftReference<Bitmap>> mImageMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.display_photo);

	setUpView();

	mImageDiskCache = new ImageDiskCache(this, "ShowPhoto");
	
	mViewHandlerThread = new HandlerThread(ViewHandler.class.getSimpleName());
	mViewHandlerThread.start();
	
	mViewHandler = new ViewHandler(mViewHandlerThread.getLooper());
	
	Intent intent = getIntent();
	
	String url = intent.getStringExtra(CommonIntent.URL_EXTRA_VALUE);

	sendMessageToShowImage(url);
    }

    /**
     * begin to download image and show the image
     * 
     * @param url
     */
    private void sendMessageToShowImage(String url) {
	if (mViewHandler != null) {
	    try {
		Message msg = mViewHandler.obtainMessage(
			DOWNLOAD_IMAGE_AND_SHOW, url);
		msg.sendToTarget();
	    } catch (RuntimeException e) {
		Log.e(TAG,
			"error in sending the message DOWNLOAD_IMAGE_AND_SHOW to ViewHandler.");
	    }
	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onStart() {
	super.onStart();
    }

    @Override
    protected void onResume() {
	super.onResume();
    }

    @Override
    protected void onPause() {
	super.onPause();
    }

    @Override
    protected void onStop() {
	super.onStop();
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	if (mViewHandler != null) {
	    mViewHandler.removeMessages(DOWNLOAD_IMAGE_AND_SHOW);
	}
	mViewHandler = null;
	
	mViewHandler = null;
        if (mViewHandlerThread != null) {
            mViewHandlerThread.getLooper().quit();
            try {
                mViewHandlerThread.join(500);
            } catch (InterruptedException e) {
            }
        }
        mViewHandlerThread = null;
        
        // recycle bitmaps
        if (mImageMap != null) {
            Iterator<SoftReference<Bitmap>> iterator = mImageMap.values().iterator();
            while (iterator.hasNext()) {
        	SoftReference<Bitmap> bm = iterator.next();
                if (bm != null) {
                    Bitmap bitmap = bm.get();
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            }
            mImageMap.clear();
            mImageMap = null;
        }
        
        if (mCurrentImageBitmap != null && !mCurrentImageBitmap.isRecycled()) {
            mCurrentImageBitmap.recycle();
            mCurrentImageBitmap = null;
        }
    }

    /**
     * initial view
     */
    private void setUpView() {
	mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
	mImageView = (ImageView) findViewById(R.id.imageview);
    }

    /**
     * download image from the internet if there is no cache for the image.
     * 
     * @param url
     */
    private void downloadImage(String url) {
	if (!mImageMap.containsKey(url) || mImageMap.get(url) == null
		|| mImageMap.get(url).get() == null) {
	    if (!mImageDiskCache.hasCache(url)) {
		HttpUtil.downloadFile(url, mImageDiskCache);
	    }
	    
	    if (mImageDiskCache.hasCache(url)) {
		Bitmap b = mImageDiskCache.getBitmapFromCache(url);
		if (b != null) {
		    if (!isFinishing() && mImageMap != null) {
			mImageMap.put(url, new SoftReference<Bitmap>(b));
			postUpdateImage(b);
		    } else {
			b.recycle();
			b = null;
		    }
		}
	    }
	}
    }
    
    /**
     * display the image to the imageview.
     * 
     * @param imageBitmap
     */
    private void postUpdateImage(Bitmap imageBitmap) {
	if (imageBitmap == null) {
	    imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
	}
	ImageUpdator imageUpdator = new ImageUpdator(imageBitmap);
	runOnUiThread(imageUpdator);
    }
    
    /**
     * The class for updating image.
     * 
     * @author Administrator
     *
     */
    private class ImageUpdator implements Runnable {
	private final Bitmap mImageBitmap;
	
	public ImageUpdator(Bitmap imageBitmap) {
	    mImageBitmap = imageBitmap;
	}
	
	@Override
	public void run() {
	    updateProgressBar(false);
	    if (mImageBitmap != null && mImageView != null && !isFinishing()) {
		mImageView.setImageBitmap(mImageBitmap);
	    }
	    
	    if (mCurrentImageBitmap != null) {
		mCurrentImageBitmap.recycle();
		mCurrentImageBitmap = null;
	    }
	    
	    mCurrentImageBitmap = mImageBitmap;
	}
	
    }
    
    /**
     * The handler to download image and show image.
     * 
     * @author Administrator
     *
     */
    private class ViewHandler extends Handler {
	public ViewHandler(Looper looper) {
	    super(looper);
	}

	@Override
	public void handleMessage(Message msg) {
	    int what = msg.what;
	    switch (what) {
	    case DOWNLOAD_IMAGE_AND_SHOW:
		updateProgressBar(true);
		String url = (String) msg.obj;
		if (mImageMap.contains(url)) {
		    Bitmap imageBitmap = null;
		    SoftReference<Bitmap> imageBitmapSoftReference = mImageMap.get(url);
		    if (imageBitmapSoftReference != null) {
			imageBitmap = imageBitmapSoftReference.get();
		    }
		    
		    if (imageBitmap != null) {
			postUpdateImage(imageBitmap);
		    } else {
			downloadImage(url);
		    }
		} else {
		    downloadImage(url);
		}
		
		break;

	    default:
		break;
	    }
	}
    }
    
    /** the ProgressBar updator which is runnable on ui thread. */
    private class ProgressBarUpdator implements Runnable {
        /** indicates if make it visible/invisible. */
        private boolean mVisible;

        /**
         * constructor
         *
         * @param visible true - make it visible, false else.
         */
        public ProgressBarUpdator(boolean visible) {
            mVisible = visible;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {
            if (mVisible) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
            }
        }

    }

    /**
     * make the progress bar visible/invisible(gone)
     *
     * @param visible visible if true, false invisible
     */
    private void updateProgressBar(boolean visible) {
        ProgressBarUpdator updator = new ProgressBarUpdator(visible);
        runOnUiThread(updator);
    }
}