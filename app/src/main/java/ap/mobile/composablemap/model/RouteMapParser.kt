package ap.mobile.composablemap.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader


/**
 * Data model untuk merepresentasikan satu titik dalam peta jalan.
 */
data class RoutePoint(
    @SerializedName("idline") val idLine: String,
    @SerializedName("idpoint") val idPoint: Int,
    @SerializedName("idinterchange") val idInterchange: Int?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("deadpoint") val deadPoint: Int?, // ➡️ Tambahan
    @SerializedName("speed") val speed: String
)


/**
 * Parser untuk membaca file JSON rute jalan dan mengubahnya menjadi list RoutePoint.
 */
fun parseJson(inputStream: java.io.InputStream): List<RoutePoint> {
    val gson = Gson()
    val type = object : TypeToken<List<RoutePoint>>() {}.type
    return gson.fromJson(InputStreamReader(inputStream), type)
}

object RouteMapParser {

    data class RoutePointJson(
        @SerializedName("idline") val idLine: String,
        @SerializedName("idpoint") val idPoint: Int,
        @SerializedName("idinterchange") val idInterchange: Int?,
        @SerializedName("lat") val lat: Double,
        @SerializedName("lng") val lng: Double,
        @SerializedName("deadpoint") val deadPoint: Int?,
        @SerializedName("speed") val speed: String

    )

    fun parseJson(inputStream: InputStream): List<RoutePoint> {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<RoutePointJson>>() {}.type
        val pointsJson: List<RoutePointJson> = Gson().fromJson(jsonString, type)

        return pointsJson.map {
            RoutePoint(
                idLine = it.idLine,
                idPoint = it.idPoint,
                idInterchange = it.idInterchange,
                lat = it.lat,
                lng = it.lng,
                speed = "normal", // Default speed, adjust if needed
                deadPoint = it.deadPoint
            )
        }
    }
}
