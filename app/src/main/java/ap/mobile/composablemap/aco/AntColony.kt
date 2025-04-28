package ap.mobile.composablemap.aco
import ap.mobile.composablemap.model.DistanceCalculator
import ap.mobile.composablemap.model.Parcel
import ap.mobile.composablemap.optimizer.Delivery
import ap.mobile.composablemap.optimizer.IOptimizer

class AntColony(
  val parcels: List<Parcel>,
  private val distanceCalculator: DistanceCalculator,
  numAnts: Int = 10,
  val cycleLimit: Int = 30,
  val rho: Float = .5f,
  val progress: (progress: Float) -> Unit,
  val startAtParcel: Parcel? = null,
) : IOptimizer {

  companion object {
    private val ants = mutableListOf<Ant>()
    private val distances = mutableMapOf<Int, MutableMap<Int, Double>>()
    private val pheromones = mutableMapOf<Int, MutableMap<Int, Double>>()
  }

  init {
    repeat(numAnts) {
      ants.add(Ant(parcels = parcels, distances = distances))
    }
    for (pa in parcels) {
      val destMap = mutableMapOf<Int, Double>()
      val pheromoneMap = mutableMapOf<Int, Double>()
      for (pb in parcels) {
        val d = distanceCalculator.calculateDistance(pa.id, pb.id)
        destMap[pb.id] = d
        pheromoneMap[pb.id] = 1 / d
      }
      distances[pa.id] = destMap
      pheromones[pa.id] = pheromoneMap
    }
  }

  override suspend fun compute(): Delivery {
    var bestPath: Path? = null
    var bestCycle = 0
    for (cycle in 1..cycleLimit) {
      if (cycle > 1) {
        pheromones.values.map {
          it.entries.map { e ->
            e.setValue(e.value * (1 - rho))
          }
        }
      }
      for (ant in ants) {
        val path = ant.moves(pheromones, startAtParcel)
        if (bestPath == null || path.sugar < bestPath.sugar) {
          bestPath = path
          bestCycle = cycle
        }
      }
      progress(cycle.toFloat() / cycleLimit.toFloat())
    }
    val delivery = Delivery(
      parcels = bestPath?.getParcels() ?: emptyList(),
      distance = bestPath?.sugar?.times(110.574)?.toFloat() ?: 0.0f,
      duration = bestPath?.getDuration() ?: 0.0f
    )
    return delivery
  }
}
