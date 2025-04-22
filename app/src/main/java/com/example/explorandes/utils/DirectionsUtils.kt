// app/src/main/java/com/example/explorandes/utils/DirectionsUtils.kt
package com.example.explorandes.utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

object DirectionsUtils {

    // Función que utiliza la biblioteca oficial de Google para obtener direcciones
    suspend fun getDirections(
        context: Context,
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val urlStr = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=walking" +
                    "&key=$apiKey"

            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            return@withContext JSONObject(response.toString())
        } catch (e: Exception) {
            Log.e("DirectionsUtils", "Error getting directions", e)
            return@withContext null
        }
    }

    // Función para dibujar ruta en el mapa a partir de un JSON de la API de Directions
    fun drawRouteFromJson(map: GoogleMap, directionsJson: JSONObject?): Boolean {
        if (directionsJson == null) return false
        
        try {
            val routes = directionsJson.getJSONArray("routes")
            if (routes.length() == 0) return false

            // Obtener la primera ruta
            val route = routes.getJSONObject(0)
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val encodedPolyline = overviewPolyline.getString("points")

            // Decodificar el polyline
            val points = PolyUtil.decode(encodedPolyline)

            // Dibujar polyline
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(Color.BLUE)
                .geodesic(true)

            map.addPolyline(polylineOptions)
            return true
        } catch (e: Exception) {
            Log.e("DirectionsUtils", "Error drawing route from JSON", e)
            return false
        }
    }

    // Función para extraer duración y distancia desde JSON de la API
    fun getRouteInfoFromJson(directionsJson: JSONObject?): Pair<String, String> {
        if (directionsJson == null) return Pair("Desconocido", "Desconocido")
        
        try {
            val routes = directionsJson.getJSONArray("routes")
            if (routes.length() == 0) return Pair("Desconocido", "Desconocido")

            val legs = routes.getJSONObject(0).getJSONArray("legs")
            if (legs.length() == 0) return Pair("Desconocido", "Desconocido")

            val leg = legs.getJSONObject(0)
            val distance = leg.getJSONObject("distance").getString("text")
            val duration = leg.getJSONObject("duration").getString("text")

            return Pair(distance, duration)
        } catch (e: Exception) {
            Log.e("DirectionsUtils", "Error extracting route info from JSON", e)
            return Pair("Desconocido", "Desconocido")
        }
    }
    
    // Función para obtener los bounds (límites) de la ruta para hacer zoom
    fun getRouteBoundsFromJson(directionsJson: JSONObject?): LatLngBoundsBuilder? {
        if (directionsJson == null) return null
        
        try {
            val routes = directionsJson.getJSONArray("routes")
            if (routes.length() == 0) return null

            val bounds = routes.getJSONObject(0).getJSONObject("bounds")
            val northeast = bounds.getJSONObject("northeast")
            val southwest = bounds.getJSONObject("southwest")
            
            val ne = LatLng(northeast.getDouble("lat"), northeast.getDouble("lng"))
            val sw = LatLng(southwest.getDouble("lat"), southwest.getDouble("lng"))
            
            return LatLngBoundsBuilder().include(ne).include(sw)
        } catch (e: Exception) {
            Log.e("DirectionsUtils", "Error extracting bounds from JSON", e)
            return null
        }
    }
    
    // Clase auxiliar para construir los límites
    class LatLngBoundsBuilder {
        private var minLat = 90.0
        private var maxLat = -90.0
        private var minLng = 180.0
        private var maxLng = -180.0
        
        fun include(point: LatLng): LatLngBoundsBuilder {
            minLat = minOf(minLat, point.latitude)
            maxLat = maxOf(maxLat, point.latitude)
            minLng = minOf(minLng, point.longitude)
            maxLng = maxOf(maxLng, point.longitude)
            return this
        }
        
        fun build(): com.google.android.gms.maps.model.LatLngBounds {
            val southwest = com.google.android.gms.maps.model.LatLng(minLat, minLng)
            val northeast = com.google.android.gms.maps.model.LatLng(maxLat, maxLng)
            return com.google.android.gms.maps.model.LatLngBounds(southwest, northeast)
        }
    }
}