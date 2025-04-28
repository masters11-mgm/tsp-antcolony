package ap.mobile.composablemap.abc

import android.util.Log
import ap.mobile.composablemap.model.Parcel
import kotlin.math.pow
import kotlin.math.sqrt

class Food(private val parcels: MutableList<Parcel>, val startAtParcel: Parcel?) {
  var nectar = 0.0

  init {
    computeNectar(parcels)
    if (startAtParcel != null) {
      parcels.remove(startAtParcel)
      parcels.add(index = 0, startAtParcel)
    }
  }

  fun optimizeShuffle(): Food {
    val before = computeNectar(parcels)
    val index = (1..(parcels.size-1)).shuffled().first()
    val chainA = parcels.slice(0..(index-1))
    val chainB = parcels.slice(index..(parcels.size-1)).shuffled()
    val newParcels: MutableList<Parcel> = mutableListOf()
    newParcels.addAll(chainA + chainB)
    val after = computeNectar(newParcels)
    if (after < before) {
      parcels.clear()
      parcels.addAll(newParcels)
    }
    return this
  }

  fun optimize(): Food {
    val before = computeNectar(parcels)
    val index = (2..(parcels.size-1)).shuffled().first()
    val chainA = parcels.slice(0..(index-2))
    val chainB = parcels.slice(((index+1).takeIf { parcels.size > index+1 } ?: (parcels.size-1))..(parcels.size-1))
    val newParcels: MutableList<Parcel> = mutableListOf()
    newParcels.addAll(chainA + parcels[index] + parcels[index-1] + chainB)
    val after = computeNectar(newParcels)
    if (after < before) {
      parcels.clear()
      parcels.addAll(newParcels)
    }
    return this
  }

  fun lookup(): Food {
    if (parcels.size <= 2) return this

    val before = computeNectar(parcels)
    val index = ((1.takeIf { startAtParcel != null } ?: 2)..(parcels.size - 1)).shuffled().first()
    val chainA = parcels.slice(0..(index - 1)).toMutableList()
    val chainB = parcels.slice(index until parcels.size).toMutableList()
    val newParcels: MutableList<Parcel> = mutableListOf()

    while (chainB.isNotEmpty()) {
      val lastParcel = chainA.last()
      val destMap = BeeColony.distances[lastParcel.id]
      var min = Double.MAX_VALUE
      var nextParcel: Parcel? = null
      for (next in chainB) {
        destMap?.get(next.id)?.let {
          if (it < min) {
            min = it
            nextParcel = next
          }
        }
      }
      if (nextParcel != null) {
        chainA.add(nextParcel!!)
        chainB.remove(nextParcel)
      } else {
        break
      }
    }

    newParcels.addAll(chainA)
    val after = computeNectar(newParcels)
    if (after < before) {
      parcels.clear()
      parcels.addAll(newParcels)
    }
    return this
  }

  fun computeNectar(parcels: List<Parcel>): Double {
    var distance = 0.0
    for (i in 1 until parcels.size) {
      val d = distance(parcels[i], parcels[i-1])
      distance += d
    }
    nectar = distance
    return nectar
  }

  fun getParcels(): List<Parcel> {
    return this.parcels
  }

  fun getDuration(): Float {
    return ((nectar.times(110.574f) / 10f) + (parcels.size * 10f / 60f)).toFloat()
  }

  companion object {
    fun distance(parcel1: Parcel, parcel2: Parcel): Double {
      val distance = sqrt((parcel1.lat - parcel2.lat).pow(2) + (parcel1.lng - parcel2.lng).pow(2))
      return distance
    }
  }
}
