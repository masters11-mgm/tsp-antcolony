package ap.mobile.composablemap

import android.content.Context
import android.util.Log
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
import kotlin.system.measureTimeMillis


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

    parcels.add(Parcel(1,lat=-8.0196038,lng=112.6288014,type="Regular",recipientName="Dewi Andriani",address="Jl. Gadang Gg. 21 C No. 23 Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(2,lat=-8.0192504,lng=112.6288474,type="Regular",recipientName="Budi Rahmawati",address="Jl. Gadang Gg. 21 C No. 18 Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(3,lat=-8.0190685,lng=112.6294651,type="Regular",recipientName="Rudi Santoso",address="Gg. 21 C Perum Sakinah No.Kav. 8, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(4,lat=-8.0182647,lng=112.6296342,type="Regular",recipientName="Rudi Saputra",address="Gang No.21 C No. 33, Gadang, Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(5,lat=-8.0198806,lng=112.6307611,type="Regular",recipientName="Dwi Susilo",address="Perumahan Gadang Nirwana Regency No.24, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(6,lat=-8.020274,lng=112.6307425,type="Regular",recipientName="Slamet Wahyudi",address="Perum Gadang Nirwana Regency (GNR 20/31, RT.RW/RW.06/04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(7,lat=-8.0200007,lng=112.6310368,type="Regular",recipientName="Eko Mulyani",address="Jl. Gadang Qinta Permai No. 9, Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(8,lat=-8.0203976,lng=112.6310182,type="Regular",recipientName="Nanik Purwanto",address="Perum Gadang Qinta Permai No.14, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(9,lat=-8.0197849,lng=112.6315809,type="Regular",recipientName="Ani Widyaningsih",address="Gg. 21 C No.65, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(10,lat=-8.019187,lng=112.6317432,type="Regular",recipientName="Budi Astuti",address="Gg. 21 C No.54, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(11,lat=-8.0190612,lng=112.6315382,type="Regular",recipientName="Teguh Susanti",address="Gg. 21 C No.48, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(12,lat=-8.0189712,lng=112.631328,type="Regular",recipientName="Wahyu Susanti",address="Gg. 21 C No.41, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(13,lat=-8.0183285,lng=112.6308323,type="Regular",recipientName="Sri Purwanto",address="Jl. Kolonel Sugiono Gg. 21 C, RT.4/RW5/RW.no 16, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(14,lat=-8.0182668,lng=112.6306142,type="Regular",recipientName="Yuli Haryanto",address="Gg. 21 C No. 4A RT05/RW04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur "))
    parcels.add(Parcel(15,lat=-8.0181727,lng=112.6303077,type="Regular",recipientName="Rini Santoso",address="Gg. 21 C No.11, RT.04/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(16,lat=-8.0181196,lng=112.6301383,type="Regular",recipientName="Wahyu Andriani",address="Gg. 21 C No.13, RT.04/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(17,lat=-8.0181072,lng=112.6301002,type="Regular",recipientName="Yuli Santoso",address="Gg. 21 C No.16, RT.04/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(18,lat=-8.017995,lng=112.6297089,type="Regular",recipientName="Teguh Handayani",address="Gg. 21 C No.22, RT.04/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(19,lat=-8.0175403,lng=112.6302043,type="Regular",recipientName="Lilis Prasetyo",address="Gg. 21 B No.62, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(20,lat=-8.0170653,lng=112.6295839,type="Regular",recipientName="Rina Sari",address="Jl. Gadang Gg. 21 B No.10, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(21,lat=-8.0170909,lng=112.6296967,type="Regular",recipientName="Siti Sari",address="Jl. Gadang Gg. 21 B No.16, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(22,lat=-8.0167552,lng=112.6299319,type="Regular",recipientName="Agus Prasetyo",address="Jl. Gadang Gg. 21 B No.37, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(23,lat=-8.0164261,lng=112.6290661,type="Regular",recipientName="Endang Santoso",address="Gg. 21 A No.11, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(24,lat=-8.0165448,lng=112.6295673,type="Regular",recipientName="Rina Astuti",address="Gg. 21 A No.43, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(25,lat=-8.0150459,lng=112.63052,type="Regular",recipientName="Dwi Handayani",address="Gg. 17 B No.61, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(26,lat=-8.0143899,lng=112.6307378,type="Regular",recipientName="Eko Rahmawati",address="Gg. 17 B No.66, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(27,lat=-8.0143665,lng=112.6306423,type="Regular",recipientName="Dwi Sari",address="Gadang Gg. 17 No.81, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(28,lat=-8.0142115,lng=112.6300184,type="Regular",recipientName="Heru Mulyani",address="Gg. 17 B 35, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(29,lat=-8.0142028,lng=112.6302335,type="Regular",recipientName="Yuli Setiawan",address="Gg. 17 B No.45, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(30,lat=-8.0138573,lng=112.6297996,type="Regular",recipientName="Wahyu Sari",address="Gg. 17 A No.20, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(31,lat=-8.0135458,lng=112.6307571,type="Regular",recipientName="Sri Wijaya",address="Jl. Gadang XV Gang Laos No.9, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(32,lat=-8.0135655,lng=112.6308488,type="Regular",recipientName="Teguh Rahmawati",address="Jl. Gadang Gg. XV No.5, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(33,lat=-8.0136213,lng=112.6310912,type="Regular",recipientName="Yuli Handayani",address="Jl. Gadang Gg. XV No.53 RT02/RW03, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(34,lat=-8.0136497,lng=112.6312169,type="Regular",recipientName="Rudi Andriani",address="Jl. Gadang Gg. XV No. 58, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(35,lat=-8.0136311,lng=112.631141,type="Regular",recipientName="Agus Santoso",address="Jl. Gadang Gg. XV No.55 RT02/RW03, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(36,lat=-8.013253,lng=112.6300171,type="Regular",recipientName="Heru Rahmawati",address="Jl. Gadang Gg. XV No.29, RT.08/RW.02, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(37,lat=-8.0130097,lng=112.6299802,type="Regular",recipientName="Budi Susilo",address="Jl. Gadang XV No.26, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(38,lat=-8.0129798,lng=112.6299108,type="Regular",recipientName="Slamet Susilo",address="Jl. Gadang XV No.33, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(39,lat=-8.0129244,lng=112.6297786,type="Regular",recipientName="Lilis Santoso",address="Jl. Gadang Gg. XV No. 36, RT.05/RW.04, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(40,lat=-8.0129574,lng=112.6298572,type="Regular",recipientName="Dewi Handayani",address="Jl. Gadang Gg. XV No.42, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(41,lat=-8.0129941,lng=112.6299455,type="Regular",recipientName="Ani Prasetyo",address="Jl. Gadang XV No.17, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(42,lat=-8.013057,lng=112.6311071,type="Regular",recipientName="Nanik Astuti",address="Jl. Gadang Gg. 15 Rt 01/03 no 15, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(43,lat=-8.0129816,lng=112.6307546,type="Regular",recipientName="Dwi Fauziah",address="Jl. Gadang Gg. XV No.39, RT.01/RW.03, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(44,lat=-8.0175085,lng=112.6296855,type="Regular",recipientName="Heru Andriani",address="Jl. Gadang Gg. 21 B No.49, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(45,lat=-8.021826,lng=112.6313448,type="Regular",recipientName="Rina Widyaningsih",address="Jl. Ps. Induk Gadang No.11, RW.05, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(46,lat=-8.02133,lng=112.6314315,type="Regular",recipientName="Yuli Sari",address="Jl. Ps. Induk Gadang No.22, RW.05, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(47,lat=-8.0227484,lng=112.6307595,type="Regular",recipientName="Rina Mulyani",address="Jl. Ps. Induk Gadang No.7, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(48,lat=-8.0233612,lng=112.6299637,type="Regular",recipientName="Endang Purwanto",address="Pasar Induk Gadang, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(49,lat=-8.0235648,lng=112.6307179,type="Regular",recipientName="Dewi Setiawan",address="Jl. Ps. Induk Gadang, Bumiayu, Kec. Sukun, Kota Malang, Jawa Timur"))
    parcels.add(Parcel(50,lat=-8.0234378,lng=112.6302712,type="Regular",recipientName="Agus Wahyudi",address="Jl. Ps. Gadang, Gadang, Kec. Sukun, Kota Malang, Jawa Timur"))


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
      val runtime = Runtime.getRuntime()
      val usedMemBefore = runtime.totalMemory() - runtime.freeMemory()

      var result: Result<Pair<Delivery, List<LatLng>>>? = null

      val executionTime = measureTimeMillis {
        val allParcelsForAlgorithm = listOf(startParcel) + parcels
        val opt: IOptimizer = if (optimizer.equals("ABC", ignoreCase = true)) {
          BeeColony(allParcelsForAlgorithm, progress = progress, startAtParcel = startParcel, distanceCalculator = distanceCalculator)
        } else {
          AntColony(allParcelsForAlgorithm, progress = progress, startAtParcel = startParcel, distanceCalculator = distanceCalculator)
        }
        val delivery = opt.compute()

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

        result = Result.Success(Pair(delivery, fullDeliveryRoute))
      }

      val usedMemAfter = runtime.totalMemory() - runtime.freeMemory()
      val usedMemMB = (usedMemAfter - usedMemBefore).toDouble() / (1024 * 1024)

      Log.d("Performance", "Execution time: $executionTime ms")
      Log.d("Performance", "Memory used: %.2f MB".format(usedMemMB))

      result!!
    }
  }

  fun getAllMapRoutePoints(): List<RoutePoint> {
    return routeGraph.getPoints()
  }
  fun getAllMapRouteEdges(): List<Pair<LatLng, LatLng>> {
    return routeGraph.getAllEdgesAsLatLng()
  }


}