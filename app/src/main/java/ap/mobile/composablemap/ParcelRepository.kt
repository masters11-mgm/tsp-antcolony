package ap.mobile.composablemap

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import ap.mobile.composablemap.abc.BeeColony
import ap.mobile.composablemap.aco.AntColony
import ap.mobile.composablemap.model.DistanceCalculator
import ap.mobile.composablemap.model.Parcel
import ap.mobile.composablemap.model.RouteGraph
import ap.mobile.composablemap.model.RouteMapParser
import ap.mobile.composablemap.model.RoutePoint
import ap.mobile.composablemap.optimizer.Delivery
import ap.mobile.composablemap.optimizer.IOptimizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out R> {
  data class Success<out T>(val data: T) : Result<T>()
  data class Error(val exception: Exception) : Result<Nothing>()
}

class ParcelRepository(context: Context) {

  val startParcel = Parcel(
    id = 31,
    lat = -8.02002,
    lng = 112.61918,
    type = "Reguler",
    recipientName = "Starting Point",
    address = "Lokasi Awal"
  )

  private val parcels = mutableListOf<Parcel>()

  private val routeGraph: RouteGraph
  private val distanceCalculator: DistanceCalculator

  init {
    val inputStream = context.assets.open("map-route.json")
    val points = RouteMapParser.parseJson(inputStream)
    routeGraph = RouteGraph(points)
    distanceCalculator = DistanceCalculator(routeGraph)

    parcels.add(Parcel(1,lat=-8.01815,lng=112.62943,type="Regular",recipientName="Nizar Zulfikar",address="Gg. 21 C No.7, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(2,lat=-8.01895,lng=112.62941,type="Regular",recipientName="Ibrahim Eka",address="Gg. 21 C Perum Sakinah No.Kav. 8, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(3,lat=-8.01896,lng=112.63076,type="Regular",recipientName="Halim Yunus",address="Gg. 21 C No.12, RT.06/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(4,lat=-8.01905,lng=112.63071,type="Regular",recipientName="Yusuf Ilham",address="Gg. 3 C No.14, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(5,lat=-8.01966,lng=112.63113,type="Regular",recipientName="Nissa Afifah",address="Jl. Gadang No. 9, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(6,lat=-8.02048,lng=112.63174,type="Priority",recipientName="Idris Muhamad",address="Gg. 21 C No.48, RT.006/RW.004, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(7,lat=-8.0186,lng=112.63117,type="Regular",recipientName="Wati Indra",address="Jl.Kolonel Sugiono XXIC/22, RT.05/RW.04, Gadang, Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(8,lat=-8.01845,lng=112.63118,type="Regular",recipientName="Buana Sri Wahyuni",address="Gg. 21 C No.78, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(9,lat=-8.01747,lng=112.62969,type="Regular",recipientName="Amin Adam",address="Gg. 21 B No.48, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(10,lat=-8.01732,lng=112.63092,type="Regular",recipientName="Sri Wahyuni Harun",address="Jl. Kolonel Sugiono Gg. 21 B No.28, RT.03/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(11,lat=-8.01756,lng=112.63124,type="Regular",recipientName="Rizky Sultan",address="Gg. 21 B 54, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(12,lat=-8.01705,lng=112.62949,type="Priority",recipientName="Jamil Yusuf",address="Jl. Gadang Gg. 21 B No.12, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(13,lat=-8.01677,lng=112.62984,type="Regular",recipientName="Jamilah Fikri",address="Gg. 21 A No.24, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(14,lat=-8.01632,lng=112.62974,type="Regular",recipientName="Alya Batari",address="Gg. 21 A No.27, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(15,lat=-8.01591,lng=112.63015,type="Regular",recipientName="Yahya Fatimah",address="Gadang Gg. 19 No.12, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(16,lat=-8.0148,lng=112.63011,type="Regular",recipientName="Putra Faisal",address="Gg. 17 B No.60, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(17,lat=-8.015,lng=112.6305,type="Priority",recipientName="Jamilah Akhmad",address="Gg. 17 B No.58, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(18,lat=-8.01419,lng=112.62993,type="Regular",recipientName="Imam Zulfikar",address="Jl. Raya Gadang Gg. 17 B No. 22, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(19,lat=-8.01384,lng=112.62976,type="Regular",recipientName="Wati Joko",address="Gg. 17 A No.27, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(20,lat=-8.01385,lng=112.62981,type="Regular",recipientName="Rustam Faris",address="Gg. 17 A No.20, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(21,lat=-8.01385,lng=112.63029,type="Regular",recipientName="Jamilah Farida",address="Jl. Jupri Gg. 17 A No.23, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(22,lat=-8.01356,lng=112.63035,type="Regular",recipientName="Salma Rizki",address="Jl. Gadang XV 26-22, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(23,lat=-8.01304,lng=112.63045,type="Priority",recipientName="Indra Cinta",address="Jl. Gadang Gg. XV No.33, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(24,lat=-8.01271,lng=112.63007,type="Regular",recipientName="Sri Purnama",address="Agung Timur 4 Blok O No. 2 Kav. 18-19, Sunter Podomoro, North Jakarta"))
    parcels.add(Parcel(25,lat=-8.01505,lng=112.63004,type="Regular",recipientName="Yuda Burhanuddin",address="Jl. Gadang Gg. XV No.34, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(26,lat=-8.01701,lng=112.62846,type="Regular",recipientName="Cahya Kusuma",address="Jl. Kolonel Sugiono Gadang Gg. 19 No.21 B, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(27,lat=-8.02218,lng=112.62878,type="Priority",recipientName="Sitti Ruslan",address="Jl. Raya Gadang No.497, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(28,lat=-8.02255,lng=112.63005,type="Regular",recipientName="Fatimah Haris",address="Jl. Raya Gadang No.7A No.26, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(29,lat=-8.02289,lng=112.63026,type="Regular",recipientName="Kasih Said",address="Jl. Ps. Gadang, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(30,lat=-8.02334,lng=112.63014,type="Regular",recipientName="Sutrisno Malik",address="Jl. Ps. Induk Gadang 1-14, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
  }

  fun getAllParcels(): List<Parcel> {
    return parcels.toList()
  }

  suspend fun computeDelivery(
    progress: (Float) -> Unit,
    parcel: Parcel?,
    optimizer: String = "ACO"
  ) : Result<Pair<Delivery, List<LatLng>>> {
    return withContext(Dispatchers.IO) {
      val allParcelsForAlgorithm = listOf(startParcel) + parcels
      val opt: IOptimizer = if (optimizer.equals("ABC", ignoreCase = true)) {
        BeeColony(allParcelsForAlgorithm, progress = progress, startAtParcel = parcel, distanceCalculator = distanceCalculator)
      } else {
        AntColony(allParcelsForAlgorithm, progress = progress, startAtParcel = startParcel, distanceCalculator = distanceCalculator)
      }
      val delivery = opt.compute()

      // Build real deliveryRoute based on map-route.json
      val deliveryRoute = mutableListOf<LatLng>()
      val deliveryParcels = delivery.parcels
      for (i in 0 until deliveryParcels.size - 1) {
        deliveryRoute.addAll(routeGraph.findShortestPath(
          deliveryParcels[i].position,
          deliveryParcels[i+1].position
        ))
      }

      Result.Success(Pair(delivery, deliveryRoute))
    }
  }
  fun getAllMapRoutePoints(): List<RoutePoint> {
    return routeGraph.getPoints()
  }

}