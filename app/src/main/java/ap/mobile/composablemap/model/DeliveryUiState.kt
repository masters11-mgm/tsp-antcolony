package ap.mobile.composablemap.model

data class DeliveryUiState(
  val isComputing: Boolean = false,
  val computingProgress: Float = 0f,
  val deliveryRoute: List<Parcel> = emptyList(),
  val deliveryDuration: Float = 0f,
  val deliveryDistance: Float = 0f
)
