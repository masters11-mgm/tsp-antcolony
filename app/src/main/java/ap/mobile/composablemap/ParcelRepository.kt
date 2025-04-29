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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject



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

    parcels.add(Parcel(1,lat=-8.0181887,lng=112.6294548,type="Regular",recipientName="Nizar Zulfikar",address="Gg. 21 C No.7, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(2,lat=-8.0190274,lng=112.6294635,type="Regular",recipientName="Ibrahim Eka",address="Gg. 21 C Perum Sakinah No.Kav. 8, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(3,lat=-8.0189785,lng=112.6307654,type="Regular",recipientName="Halim Yunus",address="Gg. 21 C No.12, RT.06/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(4,lat=-8.019102,lng=112.6307078,type="Regular",recipientName="Yusuf Ilham",address="Gg. 3 C No.14, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(5,lat=-8.0196546,lng=112.6311165,type="Regular",recipientName="Nissa Afifah",address="Jl. Gadang No. 9, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(6,lat=-8.0204785,lng=112.6317369,type="Priority",recipientName="Idris Muhamad",address="Gg. 21 C No.48, RT.006/RW.004, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(7,lat=-8.0185884,lng=112.6312024,type="Regular",recipientName="Wati Indra",address="Jl.Kolonel Sugiono XXIC/22, RT.05/RW.04, Gadang, Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(8,lat=-8.0184558,lng=112.6311879,type="Regular",recipientName="Buana Sri Wahyuni",address="Gg. 21 C No.78, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(9,lat=-8.0174645,lng=112.6296958,type="Regular",recipientName="Amin Adam",address="Gg. 21 B No.48, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(10,lat=-8.0173447,lng=112.6309199,type="Regular",recipientName="Sri Wahyuni Harun",address="Jl. Kolonel Sugiono Gg. 21 B No.28, RT.03/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(11,lat=-8.0175556,lng=112.6312245,type="Regular",recipientName="Rizky Sultan",address="Gg. 21 B 54, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(12,lat=-8.0170451,lng=112.6294896,type="Priority",recipientName="Jamil Yusuf",address="Jl. Gadang Gg. 21 B No.12, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(13,lat=-8.016768,lng=112.6298428,type="Regular",recipientName="Jamilah Fikri",address="Gg. 21 A No.24, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(14,lat=-8.0162835,lng=112.629753,type="Regular",recipientName="Alya Batari",address="Gg. 21 A No.27, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(15,lat=-8.0158953,lng=112.6301291,type="Regular",recipientName="Yahya Fatimah",address="Gadang Gg. 19 No.12, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(16,lat=-8.0147974,lng=112.6301172,type="Regular",recipientName="Putra Faisal",address="Gg. 17 B No.60, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(17,lat=-8.0149939,lng=112.6304942,type="Priority",recipientName="Jamilah Akhmad",address="Gg. 17 B No.58, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(18,lat=-8.0141898,lng=112.6299291,type="Regular",recipientName="Imam Zulfikar",address="Jl. Raya Gadang Gg. 17 B No. 22, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(19,lat=-8.013848,lng=112.6297585,type="Regular",recipientName="Wati Joko",address="Gg. 17 A No.27, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(20,lat=-8.0138573,lng=112.6297996,type="Regular",recipientName="Rustam Faris",address="Gg. 17 A No.20, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(21,lat=-8.0138463,lng=112.6302928,type="Regular",recipientName="Jamilah Farida",address="Jl. Jupri Gg. 17 A No.23, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(22,lat=-8.0135588,lng=112.6303386,type="Regular",recipientName="Salma Rizki",address="Jl. Gadang XV 26-22, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(23,lat=-8.0130428,lng=112.6304577,type="Priority",recipientName="Indra Cinta",address="Jl. Gadang Gg. XV No.33, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(24,lat=-8.012694,lng=112.630062,type="Regular",recipientName="Sri Purnama",address="Agung Timur 4 Blok O No. 2 Kav. 18-19, Sunter Podomoro, North Jakarta"))
    parcels.add(Parcel(25,lat=-8.0150544,lng=112.6300651,type="Regular",recipientName="Yuda Burhanuddin",address="Jl. Gadang Gg. XV No.34, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(26,lat=-8.0169949,lng=112.6284021,type="Regular",recipientName="Cahya Kusuma",address="Jl. Kolonel Sugiono Gadang Gg. 19 No.21 B, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(27,lat=-8.022149,lng=112.6287823,type="Priority",recipientName="Sitti Ruslan",address="Jl. Raya Gadang No.497, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(28,lat=-8.0225267,lng=112.6300509,type="Regular",recipientName="Fatimah Haris",address="Jl. Raya Gadang No.7A No.26, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(29,lat=-8.0228945,lng=112.6302376,type="Regular",recipientName="Kasih Said",address="Jl. Ps. Gadang, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(30,lat=-8.0233266,lng=112.6301127,type="Regular",recipientName="Sutrisno Malik",address="Jl. Ps. Induk Gadang 1-14, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))

  }


  fun getAllParcels(): List<Parcel> {
    return parcels.toList()
  }

//  suspend fun computeDelivery(
//    progress: (Float) -> Unit,
//    parcel: Parcel?,
//    optimizer: String = "ACO"
//  ): Result<Pair<Delivery, List<LatLng>>> {
//    return withContext(Dispatchers.IO) {
//      val allParcelsForAlgorithm = listOf(startParcel) + parcels
//      val opt: IOptimizer = if (optimizer.equals("ABC", ignoreCase = true)) {
//        BeeColony(allParcelsForAlgorithm, progress = progress, startAtParcel = parcel, distanceCalculator = distanceCalculator)
//      } else {
//        AntColony(allParcelsForAlgorithm, progress = progress, startAtParcel = startParcel, distanceCalculator = distanceCalculator)
//      }
//      val delivery = opt.compute()
//
//      val client = OkHttpClient()
//      val fullDeliveryRoute = mutableListOf<LatLng>()
//
//      val deliveryParcels = delivery.parcels
//      for (i in 0 until deliveryParcels.size - 1) {
//        val start = deliveryParcels[i].position
//        val end = deliveryParcels[i + 1].position
//
//        val url = "https://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson"
//
//        val request = Request.Builder()
//          .url(url)
//          .build()
//
//        try {
//          val response = client.newCall(request).execute()
//          val responseBody = response.body?.string()
//
//          if (response.isSuccessful && responseBody != null) {
//            val jsonObject = JSONObject(responseBody)
//            val coordinates = jsonObject
//              .getJSONArray("routes")
//              .getJSONObject(0)
//              .getJSONObject("geometry")
//              .getJSONArray("coordinates")
//
//            for (j in 0 until coordinates.length()) {
//              val coord = coordinates.getJSONArray(j)
//              val lng = coord.getDouble(0)
//              val lat = coord.getDouble(1)
//              fullDeliveryRoute.add(LatLng(lat, lng))
//            }
//          }
//        } catch (e: Exception) {
//          e.printStackTrace()
//        }
//      }
//
//      Result.Success(Pair(delivery, fullDeliveryRoute))
//    }
//  }

//  suspend fun computeDelivery(
//    progress: (Float) -> Unit,
//    parcel: Parcel?,
//    optimizer: String = "ACO"
//  ) : Result<Pair<Delivery, List<LatLng>>> {
//    return withContext(Dispatchers.IO) {
//      val allParcelsForAlgorithm = listOf(startParcel) + parcels
//      val opt: IOptimizer = if (optimizer.equals("ABC", ignoreCase = true)) {
//        BeeColony(allParcelsForAlgorithm, progress = progress, startAtParcel = parcel, distanceCalculator = distanceCalculator)
//      } else {
//        AntColony(allParcelsForAlgorithm, progress = progress, startAtParcel = startParcel, distanceCalculator = distanceCalculator)
//      }
//      val delivery = opt.compute()
//
//      val deliveryParcels = delivery.parcels
//
//      val fullDeliveryRoute = mutableListOf<LatLng>()
//
//      for (i in 0 until deliveryParcels.size - 1) {
//        val startParcelPos = deliveryParcels[i].position
//        val endParcelPos = deliveryParcels[i + 1].position
//
//        val startPoint = routeGraph.findNearestRoutePoint(startParcelPos)
//        val endPoint = routeGraph.findNearestRoutePoint(endParcelPos)
//
//        val path = routeGraph.findShortestPath(
//          LatLng(startPoint.lat, startPoint.lng),
//          LatLng(endPoint.lat, endPoint.lng)
//        )
//
//        fullDeliveryRoute.add(startParcelPos)
//        if (path.isNotEmpty()) {
//          fullDeliveryRoute.addAll(path.drop(1)) // hindari duplikat titik
//        }
//      }
//      Result.Success(Pair(delivery, fullDeliveryRoute))
//    }
//  }

  suspend fun computeDelivery(
    progress: (Float) -> Unit,
    parcel: Parcel?,
    optimizer: String = "ACO"
  ): Result<Pair<Delivery, List<LatLng>>> {
    return withContext(Dispatchers.IO) {
      val allParcelsForAlgorithm = listOf(startParcel) + parcels
      val opt: IOptimizer = if (optimizer.equals("ABC", ignoreCase = true)) {
        BeeColony(allParcelsForAlgorithm, progress = progress, startAtParcel = parcel, distanceCalculator = distanceCalculator)
      } else {
        AntColony(allParcelsForAlgorithm, progress = progress, startAtParcel = startParcel, distanceCalculator = distanceCalculator)
      }
      val delivery = opt.compute()

      // Bangun graf eksplisit berdasarkan mapRouteEdges
      val edgeGraph = mutableMapOf<LatLng, MutableList<LatLng>>()
      routeGraph.getEdgesFromMapRoute().forEach { (from, to) ->
        edgeGraph.getOrPut(from) { mutableListOf() }.add(to)
        edgeGraph.getOrPut(to) { mutableListOf() }.add(from)
      }

      val fullDeliveryRoute = mutableListOf<LatLng>()
      val deliveryParcels = delivery.parcels
      for (i in 0 until deliveryParcels.size - 1) {
        val start = deliveryParcels[i].position
        val end = deliveryParcels[i + 1].position

        val nearestStart = routeGraph.findNearestInEdgeGraph(start, edgeGraph.keys)
        val nearestEnd = routeGraph.findNearestInEdgeGraph(end, edgeGraph.keys)

        val segmentPath = routeGraph.findPathUsingEdges(nearestStart, nearestEnd, edgeGraph)
        if (segmentPath.isNotEmpty()) fullDeliveryRoute.addAll(segmentPath)
      }

      Result.Success(Pair(delivery, fullDeliveryRoute))
    }
  }

  fun getAllMapRoutePoints(): List<RoutePoint> {
    return routeGraph.getPoints()
  }
  fun getAllMapRouteEdges(): List<Pair<LatLng, LatLng>> {
    return routeGraph.getAllEdgesAsLatLng()
  }


}