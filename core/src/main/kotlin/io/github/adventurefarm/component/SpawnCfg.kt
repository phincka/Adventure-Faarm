package io.github.adventurefarm.component

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import ktx.math.vec2

data class SpawnCfg(
    val atlasKey: String,
    val scaleSize: Float = 1f,
    val scaleSpeed: Float = 1f,
    val canAttack: Boolean = true,
    val scaleAttackDamage: Float = 1f,
    val attackDelay: Float = 0.2f,
    val attackExtraRange: Float = 0f,
    val lifeScale: Float = 1f,
    val bodyType: BodyDef.BodyType = BodyDef.BodyType.DynamicBody,
    val scalePhysic: Vector2 = vec2(1f, 1f),
    val physicOffset: Vector2 = vec2(0f, 0f),
    val aiTreePath: String = "",
    val hasLight: Boolean = false,
    val categoryBit: Short? = null,
) {
    companion object {
        const val DEFAULT_SPEED = 2f
        const val DEFAULT_ATTACK_DAMAGE = 5
    }
}

data class SpawnComponent(
    var type: String = "",
    var location: Vector2 = vec2(),
    var color: Color = Color.WHITE
)
