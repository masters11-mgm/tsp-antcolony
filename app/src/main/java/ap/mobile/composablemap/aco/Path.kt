package ap.mobile.composablemap.aco

import ap.mobile.composablemap.model.Parcel
import ap.mobile.composablemap.model.RouteGraph
import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow
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

  // fun getParcelIds() : List<Int> {
  //   return route.map { it.id }
  // }

  fun getDuration(routeGraph: RouteGraph): Double {
    var totalSeconds = 0.0

    for (i in 0 until route.size - 1) {
      val from = route[i].position
      val to = route[i + 1].position

      // Temukan path dari graph
      val segmentPath = routeGraph.findShortestPath(from, to)

      // Hitung total waktu tempuh berdasarkan semua edge di path
      for (j in 0 until segmentPath.size - 1) {
        val fromNode = segmentPath[j]
        val toNode = segmentPath[j + 1]

        val timeSec = routeGraph.getSpeedBetween(fromNode, toNode)
        totalSeconds += timeSec
      }
    }

//    // Tambahkan waktu serah terima: 1 menit per parcel
    val handoverSeconds = route.size * 1 * 60
    totalSeconds += handoverSeconds

    return (totalSeconds / 3600f) // convert to hours
  }




  companion object {
    fun distance(parcel1: Parcel, parcel2: Parcel): Double {
      val distance = sqrt((parcel1.lat - parcel2.lat).pow(2) + (parcel1.lng - parcel2.lng).pow(2))
      return distance
    }
  }
}