package com.jyj.photo;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class MultiTouchListener implements OnTouchListener {

    /** the mode */
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    
    /** the matrix to process image */
    private Matrix mMatrix = new Matrix();
    
    /** used to save the original matrix, before scale the matrix should be set to the original */
    private Matrix mSavedMatrix = new Matrix();
    
    /** the current mode */
    private int mMode = NONE;
    
    /** the start point */
    private PointF mStartPointF = new PointF();
    
    /** the middle point when multitouch */
    private PointF mMidPointF = new PointF();
    
    /** the distance */
    private float mOldDist = 1;
    
    
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
	ImageView imageView = (ImageView) v;
	
	switch (event.getAction() & MotionEvent.ACTION_MASK) {
	case MotionEvent.ACTION_DOWN:
	    mMatrix.set(imageView.getImageMatrix());
	    mSavedMatrix.set(mMatrix);
	    mStartPointF.set(event.getX(), event.getY());
	    mMode = DRAG;
	    break;
	    
	case MotionEvent.ACTION_POINTER_DOWN:
	    mOldDist = pointSpacing(event);
	    if (mOldDist > 10f) {
		mSavedMatrix.set(mMatrix);
		middlePoint(mMidPointF, event);
		mMode = ZOOM;
	    }
	    break;
	    
	case MotionEvent.ACTION_UP:
	case MotionEvent.ACTION_POINTER_UP:
	    mMode = NONE;
	    break;
	    
	case MotionEvent.ACTION_MOVE:
	    if (mMode == DRAG) {
		mMatrix.set(mSavedMatrix);
		mMatrix.postTranslate(event.getX() - mStartPointF.x, event.getY() - mStartPointF.y);
	    } else if (mMode == ZOOM) {
		float newDist = pointSpacing(event);
		if (newDist > 10f) {
		    mMatrix.set(mSavedMatrix);
		    float scale = newDist / mOldDist;
		    mMatrix.postScale(scale, scale, mMidPointF.x, mMidPointF.y);
		}
	    }
	    break;

	default:
	    break;
	}
	
	
	imageView.setImageMatrix(mMatrix);
	return true;
    }

    /**
     * Used to get the space between the two point.
     * 
     * @param event
     * @return
     */
    private float pointSpacing(MotionEvent event) {
	float x = event.getX(0) - event.getX(1);
	float y = event.getY(0) - event.getY(1);
	return FloatMath.sqrt(x*x + y*y);
    }
    
    /**
     * Used to set the middle point between the two point
     * @param middlePointF
     * @param event
     */
    private void middlePoint(PointF middlePointF, MotionEvent event) {
	float x = event.getX(0) + event.getX(0);
	float y = event.getY(0) + event.getY(1);
	middlePointF.set(x / 2, y / 2);
    }
    
}
