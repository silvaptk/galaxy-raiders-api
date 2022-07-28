package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

class Explosion(
  initialPosition: Point2D,
  radius: Double
): SpaceObject(
  type = "Explosion",
  symbol = '*',
  initialPosition = initialPosition,
  initialVelocity = Vector2D(0.0, 0.0),
  radius = radius,
  mass = 0.0
) {
  private var visiblity: Double = 1.0

  val visible: Boolean
    get() = this.visiblity > 0

  fun decreaseVisibility (step: Double = 0.01) {
    this.visiblity -= step
  }
}