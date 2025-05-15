package com.example.explorandes.utils

import android.graphics.Bitmap
import android.util.LruCache

object CustomImageCache {

    private val cacheSize = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()

    private val lruCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun getBitmapFromCache(key: String): Bitmap? {
        return lruCache.get(key)
    }

    fun putBitmapInCache(key: String, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }
}
