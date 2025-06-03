package ap.mobile.composablemap.aco

import ap.mobile.composablemap.model.Parcel
import ap.mobile.composablemap.model.RouteGraph
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Path (
  val parcelIds: List<Int>,
  parcels: List<Parcel> = listOf<Parcel>()
) {

  val route : MutableList<Parcel> = mutableListOf<Parcel>()
  var sugar = 0.0

  init {
    for (parcelId in parcelIds) {
      val parcel = parcels.first { it.id == parcelId }
      route.add(parcel)
    }
    computeSugar(route)
  }

  private fun computeSugar(parcels: List<Parcel>): Double {
    var distance = 0.0
    for (i in 1..(parcels.size-1)) {
      val d = distance(parcels[i], parcels[i - 1])
      distance += d
    }
    sugar = distance
    return sugar
  }

  fun leaveTrail(pheromones: MutableMap<Int, MutableMap<Int, Double>>) : Path {
    val trailPheromone = 1 / sugar
    for (i in 0..parcelIds.size - 2) {
      var level = pheromones[parcelIds[i]]?.get(parcelIds[i+1]) ?: 1.0
      pheromones[parcelIds[i]]?.set(parcelIds[i+1], level + trailPheromone)
    }
    return this
  }


  fun getParcels() : List<Parcel> {
    return route
  }

  companion object {
    lateinit var routeGraph: RouteGraph  // Tambahkan ini di dalam companion object

    fun distance(parcel1: Parcel, parcel2: Parcel): Double {
      val from = LatLng(parcel1.lat, parcel1.lng)
      val to = LatLng(parcel2.lat, parcel2.lng)
      val distance = routeGraph.findPathDistance(from, to)
      return distance

    }

    fun getDuration(totalDistanceInKm: Number, parcelCount: Int): Float {
      val averageSpeedKph = 16f // Normal speed
      val drivingTimeHours = totalDistanceInKm.toFloat() / averageSpeedKph

      val handoverMinutes = parcelCount.toFloat() * 1 // 1 menit per parcel
      val handoverTimeHours = handoverMinutes / 60f

      val durationFinal = drivingTimeHours.toFloat() + handoverTimeHours.toFloat()

      return durationFinal
    }
  }


}