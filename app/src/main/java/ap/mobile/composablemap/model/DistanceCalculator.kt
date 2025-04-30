package ap.mobile.composablemap.model

/**
 * Distance Calculator untuk ACO / ABC agar menggunakan peta RouteGraph
 */
class DistanceCalculator(val routeGraph: RouteGraph) {

    /**
     * Hitung jarak terdekat dari node A ke node B.
     * Ini versi sederhana: diasumsikan ada edge langsung (karena mapping Anda berurutan).
     * Kalau tidak langsung, perlu upgrade ke Dijkstra (nanti kalau mau).
     */
    fun calculateDistance(from: Int, to: Int): Double {
        if (from == to) return 0.0

        // Cari edge dari 'from' ke 'to'
        val edges = routeGraph.getEdges(from)
        val directEdge = edges.find { it.to == to }

        return directEdge?.distanceMeters ?: Double.MAX_VALUE
    }

    /**
     * Hitung estimasi waktu tempuh dari node A ke node B.
     */
    fun calculateTime(from: Int, to: Int): Double {
        if (from == to) return 0.0

        val edges = routeGraph.getEdges(from)
        val directEdge = edges.find { it.to == to }

        return directEdge?.estimatedTimeSeconds ?: Double.MAX_VALUE
    }
}