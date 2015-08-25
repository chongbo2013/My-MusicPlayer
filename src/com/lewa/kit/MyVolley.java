/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lewa.kit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.lewa.Lewa;
import com.lewa.util.StringUtils;

import java.io.File;


/**
 * Helper class that is used to provide references to initialized RequestQueue(s) and ImageLoader(s)
 *
 * @author Ognyan Bankov
 */
public class MyVolley {
    private static final String mRootDir = "/LEWA/music/image";
    private static final int mCacheSize = 100 * 1024 * 1024;//100M
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private static BitmapLruCache mBitmapLruCache;


    private MyVolley() {
        // no instances
    }


    public static void init(Context context, BitmapLruCache cache) {
        mRequestQueue = newRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue, cache);
    }


    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static void queue(Request request) {
        getRequestQueue().add(request);
    }


    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     *
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue() {
        File cacheDir = new File(Environment.getExternalStorageDirectory() + mRootDir);

        Network network = new BasicNetwork(new HurlStack());
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir, mCacheSize), network);
        queue.start();

        return queue;
    }

    public static void getImage(String path, ImageLoader.ImageListener listener) {
        if (StringUtils.isBlank(path)) {
            return;
        } else {
            if (path.startsWith("http")) {
                mImageLoader.get(path, listener);
            } else {
                Bitmap cachedBitmap = Lewa.getLocalImage(path);
            }
        }
    }
}
