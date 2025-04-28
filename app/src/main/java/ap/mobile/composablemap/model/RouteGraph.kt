
package ap.mobile.composablemap.model
import com.google.android.gms.maps.model.LatLng
import java.util.PriorityQueue

class RouteGraph(private val points: List<RoutePoint>) {

    private val adjacencyList: MutableMap<Int, MutableList<Edge>> = mutableMapOf()

    init {
        buildGraph()
    }

    data class Edge(
        val to: Int,
        val distanceMeters: Double,
        val estimatedTimeSeconds: Double
    )

    private fun buildGraph() {
        for (i in 0 until points.size - 1) {
            val from = i
            val to = i + 1
            val distance = haversineDistance(points[from], points[to])
            val speed = speedToMetersPerSecond(points[from].speed)
            val time = distance / speed

            adjacencyList.getOrPut(from) { mutableListOf() }
                .add(Edge(to, distance, time))

            adjacencyList.getOrPut(to) { mutableListOf() }
                .add(Edge(from, distance, time))
        }
    }

    private fun haversineDistance(p1: RoutePoint, p2: RoutePoint): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(p2.lat - p1.lat)
        val dLon = Math.toRadians(p2.lng - p1.lng)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(p1.lat)) * kotlin.math.cos(Math.toRadians(p2.lat)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }

    private fun speedToMetersPerSecond(speed: String): Double {
        return when (speed.lowercase()) {
            "low" -> 10 * (1000.0 / 3600.0)
            "normal" -> 30 * (1000.0 / 3600.0)
            "fast" -> 50 * (1000.0 / 3600.0)
            else -> 30 * (1000.0 / 3600.0)
        }
    }

    fun findShortestPath(start: LatLng, end: LatLng): List<LatLng> {
        val startIndex = findNearestPointIndex(start)
        val endIndex = findNearestPointIndex(end)

        val distances = MutableList(points.size) { Double.MAX_VALUE }
        val previous = MutableList<Int?>(points.size) { null }
        val visited = MutableList(points.size) { false }

        val queue = PriorityQueue(compareBy<Pair<Int, Double>> { it.second })

        distances[startIndex] = 0.0
        queue.add(startIndex to 0.0)

        while (queue.isNotEmpty()) {
            val (current, _) = queue.poll()
            if (visited[current]) continue
            visited[current] = true

            adjacencyList[current]?.forEach { edge ->
                val alt = distances[current] + edge.distanceMeters
                if (alt < distances[edge.to]) {
                    distances[edge.to] = alt
                    previous[edge.to] = current
                    queue.add(edge.to to alt)
                }
            }
        }

        val path = mutableListOf<LatLng>()
        var current = endIndex
        while (current != startIndex) {
            path.add(LatLng(points[current].lat, points[current].lng))
            current = previous[current] ?: break
        }
        path.add(LatLng(points[startIndex].lat, points[startIndex].lng))
        return path.reversed()
    }

    private fun findNearestPointIndex(location: LatLng): Int {
        var minDistance = Double.MAX_VALUE
        var nearestIndex = 0
        for ((index, point) in points.withIndex()) {
            val distance = haversineDistance(
                RoutePoint("", 0, null, location.latitude, location.longitude, "normal"),
                point
            )
            if (distance < minDistance) {
                minDistance = distance
                nearestIndex = index
            }
        }
        return nearestIndex
    }
    fun getEdges(from: Int): List<Edge> {
        return adjacencyList[from] ?: emptyList()
    }
    fun getPoints(): List<RoutePoint> = points
}
