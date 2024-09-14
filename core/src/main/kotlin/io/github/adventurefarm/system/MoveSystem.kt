package io.github.adventurefarm.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import io.github.adventurefarm.component.FarmComponent
import io.github.adventurefarm.component.ImageComponent
import io.github.adventurefarm.component.MoveComponent
import io.github.adventurefarm.component.PhysicComponent
import io.github.adventurefarm.component.PlayerComponent
import ktx.math.component1
import ktx.math.component2

@AllOf([MoveComponent::class, PhysicComponent::class])
class MoveSystem(
    world: World,
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val imgCmps: ComponentMapper<ImageComponent>,
    private val farmCmps: ComponentMapper<FarmComponent>,
) : IteratingSystem() {
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

    override fun onTickEntity(entity: Entity) {
        val moveCmp = moveCmps[entity]
        val physicCmp = physicCmps[entity]

        // Calculate the current position of the entity
        val playerPosX = physicCmp.body.position.x
        val playerPosY = physicCmp.body.position.y

        // Calculate the distance to the target
        val deltaX = moveCmp.targetX - playerPosX
        val deltaY = moveCmp.targetY - playerPosY
        val distance = Vector2(deltaX, deltaY).len()

        // Check if the entity is close enough to the target to stop
        if (distance <= 0.5f) {
            // Stop the movement
            moveCmp.cosSin.set(0f, 0f)
            physicCmp.body.linearVelocity.set(0f, 0f)

            playerEntities.forEach {
                farmCmps[it].targetX = moveCmp.targetX
                farmCmps[it].targetY = moveCmp.targetY
            }

            return
        }


        val mass = physicCmp.body.mass
        val (velX, velY) = physicCmp.body.linearVelocity
        val slowFactor = if (moveCmp.slow) 0.2f else 1f
        val (cos, sin) = moveCmp.cosSin

        physicCmp.impulse.set(
            mass * (moveCmp.speed * slowFactor * cos - velX),
            mass * (moveCmp.speed * slowFactor * sin - velY)
        )

        // Flip image if entity moves left/right
        imgCmps.getOrNull(entity)?.let { imgCmp ->
            if (cos != 0f) {
                imgCmp.image.flipX = cos < 0
            }
        }
    }
}
