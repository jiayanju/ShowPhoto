package com.jyj.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface DiskCache {

    /**
     * to check if the cache file is exist according to
     * the key
     * 
     * @param key
     * @return
     */
    boolean hasCache(String key);
    
    /**
     * Return the File instance according to the key.
     * 
     * @param key
     * @return
     */
    File get(String key);
    
    /**
     * Return the InputStream of the cache file
     * 
     * @param key
     * @return
     * @throws IOException
     */
    InputStream getInputStream(String key) throws IOException;
    
    /**
     * save the cache file
     * @param key
     * @param inputStream
     */
    void cacheFile(String key, InputStream inputStream);
    
}
