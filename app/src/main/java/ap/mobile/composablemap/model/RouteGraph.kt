// File: RouteGraph.kt
package ap.mobile.composablemap.model

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.util.PriorityQueue
import kotlin.math.pow

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
            val current = points[i]
            val next = points[i + 1]

            if (current.idLine == next.idLine && current.deadPoint != 1) {
                val from = i
                val to = i + 1
                val distance = haversineDistance(current, next)
                val speed = speedToMetersPerSecond(current.speed)
                val time = distance / speed

                adjacencyList.getOrPut(from) { mutableListOf() }
                    .add(Edge(to, distance, time))

                adjacencyList.getOrPut(to) { mutableListOf() }
                    .add(Edge(from, distance, time)) // jalan dua arah
            }
        }
//        for ((from, edges) in adjacencyList) {
//            Log.d("RouteGraph", "Node $from: ")
//            for (edge in edges) {
//                Log.d(
//                    "RouteGraph",
//                    "  ➔ to=${edge.to} distance=${edge.distanceMeters}m time=${edge.estimatedTimeSeconds}s"
//                )
//            }
//        }
    }


    private fun haversineDistance(p1: RoutePoint, p2: RoutePoint): Double {
        return haversineDistance(LatLng(p1.lat, p1.lng), LatLng(p2.lat, p2.lng))
    }

    private fun haversineDistance(p1: LatLng, p2: LatLng): Double {
        val R = 6371000.0 // radius bumi dalam meter
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = kotlin.math.sin(dLat / 2).pow(2.0) +
                kotlin.math.cos(Math.toRadians(p1.latitude)) *
                kotlin.math.cos(Math.toRadians(p2.latitude)) *
                kotlin.math.sin(dLng / 2).pow(2.0)
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

//    fun findShortestPath(start: LatLng, end: LatLng): List<LatLng> {
//        val startIndex = findNearestPointIndex(start)
//        val endIndex = findNearestPointIndex(end)
//
//        val distances = MutableList(points.size) { Double.MAX_VALUE }
//        val previous = MutableList<Int?>(points.size) { null }
//        val visited = MutableList(points.size) { false }
//
//        val queue = PriorityQueue(compareBy<Pair<Int, Double>> { it.second })
//
//        distances[startIndex] = 0.0
//        queue.add(startIndex to 0.0)
//
//        while (queue.isNotEmpty()) {
//            val (current, _) = queue.poll()
//            if (visited[current]) continue
//            visited[current] = true
//
//            adjacencyList[current]?.forEach { edge ->
//                val alt = distances[current] + edge.distanceMeters
//                if (alt < distances[edge.to]) {
//                    distances[edge.to] = alt
//                    previous[edge.to] = current
//                    queue.add(edge.to to alt)
//                }
//            }
//        }
//
//        val path = mutableListOf<LatLng>()
//        var current = endIndex
//        while (current != startIndex) {
//            path.add(LatLng(points[current].lat, points[current].lng))
//            current = previous[current] ?: break
//        }
//        path.add(LatLng(points[startIndex].lat, points[startIndex].lng))
//        return path.reversed()
//    }
fun buildExplicitEdgeGraph(edges: List<Pair<LatLng, LatLng>>): Map<LatLng, List<LatLng>> {
    val graph = mutableMapOf<LatLng, MutableList<LatLng>>()

    for ((from, to) in edges) {
        graph.getOrPut(from) { mutableListOf() }.add(to)
        graph.getOrPut(to) { mutableListOf() }.add(from) // asumsi dua arah
    }

    return graph
}
    fun shortestPathOnEdges(
        start: LatLng,
        end: LatLng,
        edgeGraph: Map<LatLng, List<LatLng>>
    ): List<LatLng> {
        val queue = ArrayDeque<List<LatLng>>()
        val visited = mutableSetOf<LatLng>()

        val nearestStart = findClosestPoint(start, edgeGraph.keys)
        val nearestEnd = findClosestPoint(end, edgeGraph.keys)

        queue.add(listOf(nearestStart))

        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val current = path.last()
            if (current == nearestEnd) return path

            visited.add(current)
            for (neighbor in edgeGraph[current] ?: emptyList()) {
                if (neighbor !in visited) {
                    queue.add(path + neighbor)
                }
            }
        }

        return emptyList()
    }

    fun getEdgesFromMapRoute(): List<Pair<LatLng, LatLng>> {
        return points.zipWithNext()
            .filter { it.first.idLine == it.second.idLine && it.first.deadPoint != 1 }
            .map { Pair(LatLng(it.first.lat, it.first.lng), LatLng(it.second.lat, it.second.lng)) }
    }

    fun findNearestInEdgeGraph(coord: LatLng, nodes: Set<LatLng>): LatLng {
        return nodes.minByOrNull { haversineDistance(coord, it) }!!
    }

    fun findPathUsingEdges(
        start: LatLng,
        end: LatLng,
        graph: Map<LatLng, List<LatLng>>
    ): List<LatLng> {
        val queue = ArrayDeque<List<LatLng>>()
        val visited = mutableSetOf<LatLng>()
        queue.add(listOf(start))

        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val current = path.last()
            if (current == end) return path
            if (current in visited) continue
            visited.add(current)
            graph[current]?.forEach { neighbor ->
                queue.add(path + neighbor)
            }
        }
        return emptyList()
    }

    fun getSpeedBetween(from: LatLng, to: LatLng): Double {
        val nearestFrom = findNearestPointIndex(from)
        val nearestTo = findNearestPointIndex(to)

        val fromPoint = points[nearestFrom]
        val toPoint = points[nearestTo]

        // Check jika ada edge langsung
        val edges = adjacencyList[nearestFrom]
        val isConnected = edges?.any { it.to == nearestTo } == true

        val speedCategory = if (isConnected) {
            fromPoint.speed.lowercase()
        } else {
            "normal" // fallback
        }

        return when (speedCategory) {
            "low" -> 10.0 * (1000.0 / 3600.0)  // 2.777 m/s
            "normal" -> 30.0 * (1000.0 / 3600.0)  // 8.333 m/s
            "fast" -> 50.0 * (1000.0 / 3600.0)  // 13.888 m/s
            else -> 30.0 * (1000.0 / 3600.0)
        }
    }
    fun getTimeBetween(from: LatLng, to: LatLng): Double {
        val fromIndex = findNearestPointIndex(from)
        val toIndex = findNearestPointIndex(to)

        val edge = adjacencyList[fromIndex]?.find { points[it.to] == points[toIndex] }
        return edge?.estimatedTimeSeconds ?: 0.0
    }

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2).pow(2.0) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2).pow(2.0)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }



    private fun findClosestPoint(target: LatLng, points: Collection<LatLng>): LatLng {
        return points.minByOrNull { haversineDistance(target, it) } ?: target
    }

    fun findShortestPath(start: LatLng, end: LatLng): List<LatLng> {
        val pointsInGraph = points.map { LatLng(it.lat, it.lng) }

        val snappedStart = pointsInGraph.minByOrNull { haversineDistance(it, start) } ?: start
        val snappedEnd = pointsInGraph.minByOrNull { haversineDistance(it, end) } ?: end

        val startIndex = points.indexOfFirst { it.lat == snappedStart.latitude && it.lng == snappedStart.longitude }
        val endIndex = points.indexOfFirst { it.lat == snappedEnd.latitude && it.lng == snappedEnd.longitude }

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
        while (current != startIndex && current != -1) {
            path.add(LatLng(points[current].lat, points[current].lng))
            current = previous[current] ?: break
        }
        path.add(LatLng(points[startIndex].lat, points[startIndex].lng))
        return path.reversed()
    }


    fun findNearestRoutePoint(location: LatLng): RoutePoint {
        var minDistance = Double.MAX_VALUE
        var nearestPoint = points.first()
        for (point in points) {
            val distance = haversineDistance(
                RoutePoint(
                    idLine = "",
                    idPoint = 0,
                    idInterchange = null,
                    lat = location.latitude,
                    lng = location.longitude,
                    speed = "normal",
                    deadPoint = null
                ),
                point
            )
            if (distance < minDistance) {
                minDistance = distance
                nearestPoint = point
            }
        }
        return nearestPoint
    }


    private fun findNearestPointIndex(location: LatLng): Int {
        var minDistance = Double.MAX_VALUE
        var nearestIndex = -1

        for ((index, point) in points.withIndex()) {
            // Skip deadpoint saat mencari titik terdekat
            if (point.deadPoint == 1) continue

            val distance = haversineDistance(
                RoutePoint(
                    idLine = "",
                    idPoint = 0,
                    idInterchange = null,
                    lat = location.latitude,
                    lng = location.longitude,
                    speed = "normal",
                    deadPoint = null
                ),
                point
            )

            if (distance < minDistance) {
                minDistance = distance
                nearestIndex = index
            }
        }

        if (nearestIndex == -1) {
            throw IllegalStateException("No valid point found near location: $location")
        }

        return nearestIndex
    }

    private fun findNearestConnectedPointIndex(location: LatLng): Int {
        var minDistance = Double.MAX_VALUE
        var nearestIndex = -1

        for ((index, point) in points.withIndex()) {
            if (point.deadPoint == 1) continue
            if (!adjacencyList.containsKey(index)) continue // ⬅️ hanya titik dengan koneksi

            val distance = haversineDistance(
                RoutePoint(
                    idLine = "",
                    idPoint = 0,
                    idInterchange = null,
                    lat = location.latitude,
                    lng = location.longitude,
                    speed = "normal",
                    deadPoint = null
                ),
                point
            )

            if (distance < minDistance) {
                minDistance = distance
                nearestIndex = index
            }
        }

        if (nearestIndex == -1) {
            throw IllegalStateException("No connected route point found near $location")
        }

        return nearestIndex
    }


    fun getEdges(from: Int): List<Edge> {
        return adjacencyList[from] ?: emptyList()
    }

    fun getPoints(): List<RoutePoint> = points

    fun getAllEdgesAsLatLng(): List<Pair<LatLng, LatLng>> {
        val edges = mutableListOf<Pair<LatLng, LatLng>>()
        adjacencyList.forEach { (fromIndex, edgeList) ->
            val fromPoint = points[fromIndex]
            for (edge in edgeList) {
                val toPoint = points[edge.to]
                edges.add(
                    Pair(
                        LatLng(fromPoint.lat, fromPoint.lng),
                        LatLng(toPoint.lat, toPoint.lng)
                    )
                )
            }
        }
        return edges
    }

}
