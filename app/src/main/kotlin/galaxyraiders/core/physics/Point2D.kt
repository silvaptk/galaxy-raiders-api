@file:Suppress("UNUSED_PARAMETER") // <- REMOVE
package galaxyraiders.core.physics

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class Point2D(val x: Double, val y: Double) {
  operator fun plus(p: Point2D): Point2D {
    return Point2D(this.x + p.x, this.y + p.y)
  }

  operator fun plus(v: Vector2D): Point2D {
    return v.plus(this)
  }

  override fun toString(): String {
    return "Point2D(x=$x, y=$y)"
  }

  fun toVector(): Vector2D {
    return Vector2D(this.x, this.y)
  }

  fun impactVector(p: Point2D): Vector2D {
    return Vector2D(
      abs(this.x - p.x),
      abs(this.y - p.y)
    )
  }

  fun impactDirection(p: Point2D): Vector2D {
    return this.impactVector(p).unit
  }

  fun contactVector(p: Point2D): Vector2D {
    return this.impactVector(p).normal
  }

  fun contactDirection(p: Point2D): Vector2D {
    return this.contactVector(p).unit
  }

  fun distance(p: Point2D): Double {
    return sqrt(
      (this.x - p.x).pow(2.0) + (this.y - p.y).pow(2.0)
    )
  }
}
