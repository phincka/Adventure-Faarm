package io.github.adventurefarm.component

import com.badlogic.gdx.math.Vector2
import ktx.math.vec2

data class MoveComponent(
    var speed: Float = 0f,
    var cosSin: Vector2 = vec2(),
    var root: Boolean = false,
    var slow: Boolean = false,
    var targetX: Float = 0f,
    var targetY: Float = 0f
)
