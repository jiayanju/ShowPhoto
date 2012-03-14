package com.jyj.photo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.jyj.util.CommonIntent;

public class ShowPhotoActivity extends Activity {
    
    private static final String DOWNLOAD_URL = "DOWNLOAD_URL_KEY";

    /** The EditText to input the download url */
    private EditText mEditText;
    
    /** The download button */
    private Button mButton;
    
    /** The Dialog */
    private Dialog mDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mEditText = (EditText) findViewById(R.id.download_url);
        
        mButton = (Button) findViewById(R.id.download_btn);
        
	if (savedInstanceState != null) {
	    String text = savedInstanceState.getString(DOWNLOAD_URL);
	    if (text != null && text.length() > 0) {
		mEditText.setText(text);
	    }
	}
	
	String testUrl = "http://www.ylfdc.cn/fyfile/200772239822081.jpg";
	mEditText.setText(testUrl);
        
        mButton.setOnClickListener(mDownloadButtonOnClickListener);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
	outState.putString(DOWNLOAD_URL, mEditText.getEditableText().toString());
        super.onSaveInstanceState(outState);
    }
    
    private OnClickListener mDownloadButtonOnClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
    		String url = mEditText.getEditableText().toString();
    		if (!validateUrl(url)) {
    		    showDialog();
    		    return;
    		}
    		
    		Intent intent = new Intent(ShowPhotoActivity.this, DisplayPhotoActivity.class);
    		intent.putExtra(CommonIntent.URL_EXTRA_VALUE, url);
    		startActivity(intent);
        }
    };
    
    /**
     * validate the url, this method need to add more validation
     * 
     * @param url
     * @return
     */
    private boolean validateUrl(String url) {
	//TODO the validation is too simple
	if (url == null || url.length() == 0 || !url.startsWith("http://")) {
	    return false;
	}
	return true;
    }
    
    /**
     * show the dialog to warn user
     */
    private void showDialog() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle(getString(R.string.dialog_title));
	builder.setMessage(getString(R.string.dialog_message));
	builder.setPositiveButton(android.R.string.ok, mPositiveClickListener);
	mDialog = builder.create();
	if (mDialog != null) {
	    mDialog.show();
	}
    }
    
    private DialogInterface.OnClickListener mPositiveClickListener = new DialogInterface.OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
    		mDialog.dismiss();
    		mDialog = null;
    	
        }
    };
}
