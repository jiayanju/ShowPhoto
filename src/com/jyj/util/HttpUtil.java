package com.jyj.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.jyj.cache.ImageDiskCache;

public class HttpUtil {
    private static final String TAG = "HttpUtil";

    /** The client connection manager instance */
    private static ClientConnectionManager mClientConnectionManager;

    /**
     * download file and cache the downloaded file to disk.
     * 
     * @param url
     * @param imageDiskCache
     */
    public static void downloadFile(String url, ImageDiskCache imageDiskCache) {
	try {
	    InputStream is = getContent(url);
	    if (is != null) {
		imageDiskCache.cacheFile(url, is);
	    }
	} finally {
	    mClientConnectionManager.shutdown();
	}
    }

    /**
     * Download file
     * 
     * @param url
     * @return
     */
    private static InputStream getContent(String url) {
	InputStream is = null;

	HttpParams httpParams = createHttpParams();
	HttpClient httpClient = createHttpClient(httpParams);
	HttpGet httpGet = createHttpGet(url);
	if (httpClient != null && httpGet != null) {
	    HttpResponse httpResponse = null;

	    try {
		httpResponse = httpClient.execute(httpGet);
	    } catch (ClientProtocolException e) {
		Log.e(TAG, e.getMessage());
		mClientConnectionManager.shutdown();
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
		mClientConnectionManager.shutdown();
	    }

	    if (httpResponse != null) {
		if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
		    HttpEntity httpEntity = httpResponse.getEntity();
		    if (httpEntity != null) {
			try {
			    is = httpEntity.getContent();
			} catch (Exception e) {
			    Log.e(TAG, e.getMessage());
			}
		    }
		}
	    }
	}

	return is;
    }

    /**
     * Create HttpParams
     * 
     * @return
     */
    private static HttpParams createHttpParams() {
	HttpParams httpParams = new BasicHttpParams();
	HttpConnectionParams.setConnectionTimeout(httpParams, 60 * 1000);
	HttpConnectionParams.setSoTimeout(httpParams, 60 * 1000);
	HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
	return httpParams;
    }

    /**
     * Create HttpClient
     * 
     * @param httpParams
     * @return
     */
    private static HttpClient createHttpClient(HttpParams httpParams) {
	HttpClient httpClient = null;
	if (httpParams != null) {
	    SchemeRegistry schemeRegistry = new SchemeRegistry();
	    try {
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
			.getSocketFactory(), 80));
	    } catch (IllegalArgumentException e) {
		throw e;
	    }
	    mClientConnectionManager = new ThreadSafeClientConnManager(
		    httpParams, schemeRegistry);
	    httpClient = new DefaultHttpClient(mClientConnectionManager,
		    httpParams);
	}
	return httpClient;
    }

    /**
     * create HttpGet
     * 
     * @param url
     * @return
     */
    private static HttpGet createHttpGet(String url) {
	if (url == null || !url.startsWith("http://")) {
	    return null;
	}

	return new HttpGet(url);
    }
}
