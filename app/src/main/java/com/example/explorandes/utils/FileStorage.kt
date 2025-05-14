
package com.example.explorandes.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for storing data in local files
 */
class FileStorage(private val context: Context) {
    
    /**
     * Save data to a local file
     * 
     * @param fileName Name of the file to save data to
     * @param data String data to save
     * @return true if operation was successful, false otherwise
     */
    fun saveToFile(fileName: String, data: String): Boolean {
        return try {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(data.toByteArray())
            }
            Log.d("FileStorage", "Data saved to file: $fileName")
            true
        } catch (e: IOException) {
            Log.e("FileStorage", "Error saving to file: $fileName", e)
            false
        }
    }
    
    /**
     * Read data from a local file
     * 
     * @param fileName Name of the file to read from
     * @return File contents as string or null if file doesn't exist or reading failed
     */
    fun readFromFile(fileName: String): String? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            Log.d("FileStorage", "File doesn't exist: $fileName")
            return null
        }
        
        return try {
            FileInputStream(file).use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                String(buffer)
            }
        } catch (e: IOException) {
            Log.e("FileStorage", "Error reading from file: $fileName", e)
            null
        }
    }
    
    /**
     * Delete a local file
     * 
     * @param fileName Name of the file to delete
     * @return true if file was deleted successfully, false otherwise
     */
    fun deleteFile(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            val result = file.delete()
            Log.d("FileStorage", "File deleted: $fileName, success: $result")
            result
        } else {
            Log.d("FileStorage", "File doesn't exist: $fileName")
            false
        }
    }
    
    /**
     * List all files in the app's files directory
     * 
     * @return List of file names
     */
    fun listFiles(): List<String> {
        val files = context.filesDir.listFiles()
        return files?.map { it.name } ?: emptyList()
    }
}