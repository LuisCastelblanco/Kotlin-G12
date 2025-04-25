package com.example.explorandes.utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object DirectionsUtils {

    // Improved function to get directions with better error handling
    suspend fun getDirections(
        context: Context,
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            // Log coordinates for debugging
            Log.d("DirectionsUtils", "Getting directions from ${origin.latitude},${origin.longitude} to ${destination.latitude},${destination.longitude}")

            val urlStr = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=walking" +
                    "&key=$apiKey"

            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val responseJson = JSONObject(response.toString())
                val status = responseJson.getString("status")

                if (status == "OK") {
                    Log.d("DirectionsUtils", "Directions API returned successfully")
                    return@withContext responseJson
                } else {
                    Log.e("DirectionsUtils", "Directions API returned status: $status")
                    return@withContext null
                }
            } else {
                Log.e("DirectionsUtils", "HTTP error: $responseCode")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("DirectionsUtils", "Error getting directions", e)
            return@withContext null
        }
    }

    // Improved function for drawing route
    fun drawRouteFromJson(map: GoogleMap, directionsJson: JSONObject?): Boolean {
        if (directionsJson == null) {
            Log.e("DirectionsUtils", "Directions JSON is null")
            return false
        }

        try {
            val routes = directionsJson.getJSONArray("routes")
            if (routes.length() == 0) {
                Log.e("DirectionsUtils", "No routes found in directions response")
                return false
            }

            // Get the first route
            val route = routes.getJSONObject(0)

            // Check if the route has steps
            val legs = route.getJSONArray("legs")
            if (legs.length() == 0) {
                Log.e("DirectionsUtils", "No legs found in route")
                return false
            }

            // Get the overview polyline
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val encodedPolyline = overviewPolyline.getString("points")

            // Decode the polyline
            val points = PolyUtil.decode(encodedPolyline)

            if (points.isEmpty()) {
                Log.e("DirectionsUtils", "No points in decoded polyline")
                return false
            }

            // Draw polyline with better styling
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(Color.parseColor("#4285F4")) // Google Maps blue
                .geodesic(true)
                .jointType(JointType.ROUND)

            map.addPolyline(polylineOptions)

            // Add origin and destination markers if needed
            // (we're not adding them here since they're already on the map)

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