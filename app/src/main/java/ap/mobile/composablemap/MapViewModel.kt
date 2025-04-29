package ap.mobile.composablemap

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.mobile.composablemap.model.DeliveryUiState
import ap.mobile.composablemap.model.MapUiState
import ap.mobile.composablemap.model.Parcel
import ap.mobile.composablemap.model.ParcelState
import ap.mobile.composablemap.optimizer.Delivery
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MapViewModel(private val context: Context) : ViewModel() {

  private val parcelRepository: ParcelRepository = ParcelRepository(context)

  private val _mapUiState = MutableStateFlow(MapUiState())
  val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()

  private val _deliveryUiState = MutableStateFlow(DeliveryUiState())
  val deliveryUiState: StateFlow<DeliveryUiState> = _deliveryUiState.asStateFlow()

  private val _parcelState = MutableStateFlow(ParcelState())
  val parcelState: StateFlow<ParcelState> = _parcelState.asStateFlow()

  init {
    getParcels()
  }

  fun moveToSingapore() {
    moveToLocation(LatLng(1.35, 103.87))
  }

  fun moveToLocation(location: LatLng) {
    _mapUiState.update { currentState ->
      currentState.copy(
        mapRouteEdges = parcelRepository.getAllMapRouteEdges()
      )
    }
  }

  fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      try {
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY,
          object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) =
              CancellationTokenSource().token
            override fun isCancellationRequested() = false
          })
          .addOnSuccessListener { location: Location? ->
            if (location != null)
              moveToLocation(LatLng(location.latitude, location.longitude))
          }
      } catch (e: SecurityException) {
        Timber.e("Permission for location access was revoked: ${e.localizedMessage}")
      }
    } else {
      Timber.e("Location permission is not granted.")
    }
  }

  fun setCameraPosition(cameraPosition: LatLng) {
    _mapUiState.update { currentState ->
      currentState.copy(cameraPosition = cameraPosition)
    }
  }

  fun setZoomLevel(zoom: Float) {
    _mapUiState.update { currentState ->
      currentState.copy(zoom = zoom)
    }
  }

  fun getParcels() {
    viewModelScope.launch {
      val parcels = parcelRepository.getAllParcels()
      _parcelState.update { currentState ->
        currentState.copy(parcels = parcels)
      }
      _deliveryUiState.update { currentState ->
        currentState.copy(deliveryRoute = parcels)
      }
      _mapUiState.update { currentState ->
        currentState.copy(
          parcels = parcels,
          mapRoute = parcelRepository.getAllMapRoutePoints().map { LatLng(it.lat, it.lng) }
        )
      }
    }
  }

  fun getDeliveryRecommendation(context: Context, parcel: Parcel? = null) {
    _deliveryUiState.update { currentState ->
      currentState.copy(isComputing = true)
    }
    _parcelState.update { currentState ->
      currentState.copy(isComputing = true)
    }

    val preferenceRepository = PreferenceRepository(context)

    viewModelScope.launch {
      val result = parcelRepository.computeDelivery(::setProgress, parcel,
        optimizer = preferenceRepository.getString(PreferencesKeys.OPTIMIZER).toString()
      )

      when (result) {
        is Result.Success<Pair<Delivery, List<LatLng>>> -> {
          val (delivery, fullDeliveryRoute) = result.data

          _deliveryUiState.update { currentState ->
            currentState.copy(
              deliveryRoute = delivery.parcels,
              deliveryDistance = delivery.distance,
              deliveryDuration = delivery.duration,
              isComputing = false
            )
          }
          _parcelState.update { currentState ->
            currentState.copy(
              isComputing = false,
              deliveries = delivery.parcels,
              deliveryDistance = delivery.distance,
              deliveryDuration = delivery.duration,
            )
          }
          _mapUiState.update { currentState ->
            currentState.copy(deliveryRoute = fullDeliveryRoute)
          }
        }
        else -> {
          // Handle error
        }
      }

    }
  }

  fun setProgress(progress: Float): Float {
    _deliveryUiState.update { currentState ->
      currentState.copy(computingProgress = progress)
    }
    return progress
  }

  fun selectParcel(parcel: Parcel) {
    _parcelState.update { currentState ->
      currentState.copy(parcel = parcel)
    }
  }

  fun parcelSheet(shouldShow: Boolean = true) {
    _parcelState.update { currentState ->
      currentState.copy(showParcelSheet = shouldShow)
    }
  }
}
