package com.example.explorandes.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.explorandes.ExplorAndesApplication
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

/**
 * Clase utilitaria para cachear imágenes en almacenamiento persistente.
 * Complementa el caché en memoria para reducir descargas repetidas.
 */
class ImageDiskCache(private val context: Context) {
    
    // Obtener la instancia de FileStorage desde la aplicación
    private val fileStorage = (context.applicationContext as ExplorAndesApplication).fileStorage
    
    // Directorio base para las imágenes cacheadas
    private val IMAGE_CACHE_DIR = "image_cache"
    
    /**
     * Guarda un bitmap en el almacenamiento local.
     * @param url URL de la imagen como clave
     * @param bitmap Bitmap a guardar
     * @return true si se guardó correctamente, false en caso contrario
     */
    fun saveBitmapToDisk(url: String, bitmap: Bitmap): Boolean {
        val fileName = getFileNameForUrl(url)
        
        return try {
            // Convertir bitmap a bytes
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val imageBytes = outputStream.toByteArray()
            
            // Guardar en el almacenamiento usando FileStorage
            val saved = fileStorage.saveToFile("$IMAGE_CACHE_DIR/$fileName", String(imageBytes))
            Log.d("ImageDiskCache", "Saved image to disk: $url, success: $saved")
            saved
        } catch (e: Exception) {
            Log.e("ImageDiskCache", "Error saving image to disk: $url", e)
            false
        }
    }
    
    /**
     * Carga un bitmap desde el almacenamiento local.
     * @param url URL de la imagen como clave
     * @return Bitmap cargado o null si no existe o hay error
     */
    fun loadBitmapFromDisk(url: String): Bitmap? {
        val fileName = getFileNameForUrl(url)
        
        return try {
            // Cargar desde el almacenamiento usando FileStorage
            val imageData = fileStorage.readFromFile("$IMAGE_CACHE_DIR/$fileName")
            
            if (imageData != null) {
                // Convertir bytes a bitmap
                val imageBytes = imageData.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Log.d("ImageDiskCache", "Loaded image from disk: $url")
                bitmap
            } else {
                Log.d("ImageDiskCache", "Image not found in disk cache: $url")
                null
            }
        } catch (e: Exception) {
            Log.e("ImageDiskCache", "Error loading image from disk: $url", e)
            null
        }
    }
    
    /**
     * Verifica si una imagen existe en la caché de disco.
     * @param url URL de la imagen
     * @return true si existe, false en caso contrario
     */
    fun exists(url: String): Boolean {
        val fileName = getFileNameForUrl(url)
        // Verificar si el archivo existe en FileStorage
        return fileStorage.readFromFile("$IMAGE_CACHE_DIR/$fileName") != null
    }
    
    /**
     * Genera un nombre de archivo único para una URL.
     * Utiliza un hash MD5 para evitar problemas con caracteres especiales en la URL.
     */
    private fun getFileNameForUrl(url: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(url.toByteArray())
            digest.fold("") { str, byte -> str + "%02x".format(byte) }
        } catch (e: Exception) {
            // Fallback en caso de error
            url.replace(Regex("[^a-zA-Z0-9]"), "_")
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ImageDiskCache? = null
        
        fun getInstance(context: Context): ImageDiskCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageDiskCache(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}