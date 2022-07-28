package galaxyraiders.core.game

import galaxyraiders.Config
import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D
import galaxyraiders.ports.RandomGenerator

object SpaceFieldConfig {
  private val config = Config(prefix = "GR__CORE__GAME__SPACE_FIELD__")

  val missileRadius = config.get<Double>("MISSILE_RADIUS")
  val missileMass = config.get<Double>("MISSILE_MASS")
  val missileDistanceFromShip = config.get<Double>("MISSILE_DISTANCE_FROM_SHIP")

  val asteroidMaxYaw = config.get<Double>("ASTEROID_MAX_YAW")
  val asteroidMinSpeed = config.get<Double>("ASTEROID_MIN_SPEED")
  val asteroidMaxSpeed = config.get<Double>("ASTEROID_MAX_SPEED")

  val asteroidMinRadius = config.get<Int>("ASTEROID_MIN_RADIUS")
  val asteroidMaxRadius = config.get<Int>("ASTEROID_MAX_RADIUS")
  val asteroidRadiusMultiplier = config.get<Double>("ASTEROID_RADIUS_MULTIPLIER")

  val asteroidMinMass = config.get<Int>("ASTEROID_MIN_MASS")
  val asteroidMaxMass = config.get<Int>("ASTEROID_MAX_MASS")
  val asteroidMassMultiplier = config.get<Double>("ASTEROID_MASS_MULTIPLIER")

  val explosionMinRadius = config.get<Double>("EXPLOSION_MIN_RADIUS")
  val explosionMaxRadius = config.get<Double>("EXPLOSION_MAX_RADIUS")
}

@Suppress("TooManyFunctions")
data class SpaceField(val width: Int, val height: Int, val generator: RandomGenerator) {
  val boundaryX = 0.0..width.toDouble()
  val boundaryY = 0.0..height.toDouble()

  val ship = initializeShip()

  var missiles: List<Missile> = emptyList()
    private set

  var asteroids: List<Asteroid> = emptyList()
    private set

  var explosions: List<Explosion> = emptyList()
    private set

  val spaceObjects: List<SpaceObject>
    get() = listOf(this.ship) + this.missiles + this.asteroids

  fun moveShip() {
    this.ship.move(boundaryX, boundaryY)
  }

  fun moveMissiles() {
    this.missiles.forEach { it.move() }
  }

  fun moveAsteroids() {
    this.asteroids.forEach { it.move() }
  }

  fun updateExplosions() {
    this.explosions.forEach { it.decreaseVisibility() }
  }

  fun generateMissile() {
    this.missiles += this.createMissile()
  }

  fun generateAsteroid() {
    this.asteroids += this.createAsteroidWithRandomProperties()
  }

  fun trimMissiles() {
    this.missiles = this.missiles.filter {
      it.inBoundaries(this.boundaryX, this.boundaryY)
    }
  }

  fun trimAsteroids() {
    this.asteroids = this.asteroids.filter {
      it.inBoundaries(this.boundaryX, this.boundaryY)
    }
  }

  fun trimExplosions() {
    this.explosions = this.explosions.filter { it.visible }
  }

  private fun initializeShip(): SpaceShip {
    return SpaceShip(
      initialPosition = standardShipPosition(),
      initialVelocity = standardShipVelocity(),
      radius = 1.0,
      mass = 10.0,
    )
  }

  private fun standardShipPosition(): Point2D {
    return Point2D(x = this.width / 2.0, y = 1.0)
  }

  private fun standardShipVelocity(): Vector2D {
    return Vector2D(dx = 0.0, dy = 0.0)
  }

  private fun createMissile(): Missile {
    return Missile(
      initialPosition = defineMissilePosition(SpaceFieldConfig.missileRadius),
      initialVelocity = defineMissileVelocity(),
      radius = SpaceFieldConfig.missileRadius,
      mass = SpaceFieldConfig.missileMass,
    )
  }

  private fun defineMissilePosition(missileRadius: Double): Point2D {
    return ship.center + Vector2D(dx = 0.0, dy = ship.radius + missileRadius + SpaceFieldConfig.missileDistanceFromShip)
  }

  private fun defineMissileVelocity(): Vector2D {
    return Vector2D(dx = 0.0, dy = 1.0)
  }

  private fun createAsteroidWithRandomProperties(): Asteroid {
    return Asteroid(
      initialPosition = generateRandomAsteroidPosition(),
      initialVelocity = generateRandomAsteroidVelocity(),
      radius = generateRandomAsteroidRadius(),
      mass = generateRandomAsteroidMass(),
    )
  }

  private fun generateRandomAsteroidPosition(): Point2D {
    return Point2D(
      x = this.generator.generateIntegerInRange(0, width).toDouble(),
      y = this.height.toDouble(),
    )
  }

  private fun generateRandomAsteroidVelocity(): Vector2D {
    val asteroidYaw = this.generator.generateDoubleInInterval(
      min = -SpaceFieldConfig.asteroidMaxYaw, max = SpaceFieldConfig.asteroidMaxYaw
    )

    val asteroidSpeed = -1 * this.generator.generateDoubleInInterval(
      min = SpaceFieldConfig.asteroidMinSpeed,
      max = SpaceFieldConfig.asteroidMaxSpeed,
    )

    return Vector2D(dx = asteroidYaw, dy = asteroidSpeed)
  }

  private fun generateRandomAsteroidRadius(): Double {
    val scaledRadius = this.generator.generateIntegerInRange(
      min = SpaceFieldConfig.asteroidMinRadius,
      max = SpaceFieldConfig.asteroidMaxRadius,
    )

    return scaledRadius * SpaceFieldConfig.asteroidRadiusMultiplier
  }

  private fun generateRandomAsteroidMass(): Double {
    val scaledMass = this.generator.generateIntegerInRange(
      min = SpaceFieldConfig.asteroidMinMass,
      max = SpaceFieldConfig.asteroidMaxMass,
    )

    return scaledMass * SpaceFieldConfig.asteroidMassMultiplier
  }

  fun generateExplosion(firstCollider: SpaceObject, secondCollider: SpaceObject) {
    this.explosions = this.explosions + Explosion(
      initialPosition = getExplosionPosition(firstCollider, secondCollider),
      radius = getExplosionRadius(firstCollider, secondCollider)
    )
  }

  private fun getExplosionPosition(
    firstColider: SpaceObject,
    secondColider: SpaceObject
  ): Point2D {
    val firstCenter = firstColider.center
    val secondCenter = secondColider.center

    val auxiliaryVector = (
      secondCenter.toVector() - firstCenter.toVector()
    ).unit * firstColider.radius

    return firstCenter + auxiliaryVector
  }

  private fun getExplosionRadius(
    firstCollider: SpaceObject,
    secondCollider: SpaceObject
  ): Double {
    val delta = (
      SpaceFieldConfig.explosionMaxRadius - SpaceFieldConfig.explosionMinRadius
    )

    val asteroidMinMass = SpaceFieldConfig.asteroidMinMass
    val asteroidMaxMass = SpaceFieldConfig.asteroidMaxMass

    val asteroidMass = if (firstCollider.symbol == '.')
      firstCollider.mass / SpaceFieldConfig.asteroidMassMultiplier
    else
      secondCollider.mass / SpaceFieldConfig.asteroidMassMultiplier

    val factor = (asteroidMass - asteroidMinMass) / (asteroidMaxMass - asteroidMinMass)

    val radius = SpaceFieldConfig.explosionMinRadius + delta * factor

    return radius
  }
}
