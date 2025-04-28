package ap.mobile.composablemap.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.InputStream

/**
 * Data model untuk merepresentasikan satu titik dalam peta jalan.
 */
data class RoutePoint(
    @SerializedName("idline") val idLine: String,
    @SerializedName("idpoint") val idPoint: Int,
    @SerializedName("idinterchange") val idInterchange: Int?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("speed") val speed: String
)

/**
 * Parser untuk membaca file JSON rute jalan dan mengubahnya menjadi list RoutePoint.
 */
object RouteMapParser {

    /**
     * Parse file InputStream menjadi List<RoutePoint>
     */
    fun parseJson(inputStream: InputStream): List<RoutePoint> {
        val json = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<RoutePoint>>() {}.type
        return Gson().fromJson(json, type)
    }
}