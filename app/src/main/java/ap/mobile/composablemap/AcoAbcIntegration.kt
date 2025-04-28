package ap.mobile.composablemap

import ap.mobile.composablemap.model.DistanceCalculator

/**
 * Ant Colony Optimization class
 */
class ACO(private val distanceCalculator: DistanceCalculator) {

    fun calculateRoute(route: List<Int>): Double {
        var totalDistance = 0.0
        for (i in 0 until route.size - 1) {
            totalDistance += distanceCalculator.calculateDistance(route[i], route[i + 1])
        }
        return totalDistance
    }
}

/**
 * Artificial Bee Colony class
 */
class ABC(private val distanceCalculator: DistanceCalculator) {

    fun calculateRoute(route: List<Int>): Double {
        var totalDistance = 0.0
        for (i in 0 until route.size - 1) {
            totalDistance += distanceCalculator.calculateDistance(route[i], route[i + 1])
        }
        return totalDistance
    }
}
