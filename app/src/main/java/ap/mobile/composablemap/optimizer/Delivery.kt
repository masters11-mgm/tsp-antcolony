package ap.mobile.composablemap.optimizer

import ap.mobile.composablemap.model.Parcel
import com.google.android.gms.maps.model.LatLng

data class Delivery(
  val parcels: List<Parcel>,
  val distance: Float = 0.0f,
  val duration: Float = 0.0f,
  val route: List<LatLng> = emptyList() // ⬅️ Tambahan baru
)