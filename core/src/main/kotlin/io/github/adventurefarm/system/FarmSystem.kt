package io.github.adventurefarm.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import com.github.quillraven.fleks.Qualifier
import io.github.adventurefarm.component.AnimationComponent
import io.github.adventurefarm.component.DisarmComponent
import io.github.adventurefarm.component.FarmComponent
import io.github.adventurefarm.component.FarmState
import io.github.adventurefarm.component.ImageComponent
import io.github.adventurefarm.component.PhysicComponent
import io.github.adventurefarm.component.PlayerComponent
import io.github.adventurefarm.event.EntityFarmEvent
import io.github.adventurefarm.event.UserHasNewLevel
import io.github.adventurefarm.event.fire
import io.github.adventurefarm.system.EntitySpawnSystem.Companion.HIT_BOX_SENSOR
import ktx.box2d.query

@AllOf([FarmComponent::class, PhysicComponent::class, ImageComponent::class])
@NoneOf([DisarmComponent::class])
class FarmSystem(
    private val farmCmps: ComponentMapper<FarmComponent>,
    private val animationCmps: ComponentMapper<AnimationComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val phWorld: World,
    @Qualifier("GameStage") private val stage: Stage,
    @Qualifier("Player") private val player: PlayerComponent,
) : IteratingSystem() {
        override fun onTickEntity(entity: Entity) {
        val farmCmp = farmCmps[entity]

        if (farmCmp.doFarm) {
            val targetX = farmCmp.targetX
            val targetY = farmCmp.targetY

            // Query the physics world to check for entities at targetX, targetY
            phWorld.query(targetX, targetY, targetX + 1f, targetY + 1f) { fixture ->
                if (fixture.userData != HIT_BOX_SENSOR) {
                    return@query true
                }

                val fixtureEntity = fixture.body.userData as Entity
                if (fixtureEntity == entity) {
                    return@query true
                }

                val isFarmPlayer = entity in playerCmps
                if (isFarmPlayer && fixtureEntity in playerCmps) {
                    return@query true
                } else if (!isFarmPlayer && fixtureEntity !in playerCmps) {
                    return@query true
                }

                player.addExp = 100

                // If we find an entity at the clicked location, attack it
                Gdx.app.log("USER", "FARM SYSTEM: ${player.exp}")
                Gdx.app.log("ATTACK", "Player is attacking entity: $fixtureEntity")

                world.remove(fixtureEntity)
                stage.fire(UserHasNewLevel(player))

                // Perform attack logic
                farmCmp.doFarm = false
                farmCmp.targetX = 0f
                farmCmp.targetY = 0f
                farmCmp.state = FarmState.READY
                farmCmp.delay = farmCmp.maxDelay

                return@query false // stop querying after finding a target
            }
        } else {
            farmCmp.doFarm = false
            farmCmp.targetX = 0f
            farmCmp.targetY = 0f
            farmCmp.state = FarmState.READY
            farmCmp.delay = farmCmp.maxDelay
        }

        // Rest of the attack logic
        farmCmp.delay -= deltaTime

        if (farmCmp.delay <= 0f && farmCmp.state == FarmState.ATTACKING) {
            // Trigger an farm event and animation
            animationCmps.getOrNull(entity)?.let { aniCmp ->
                stage.fire(EntityFarmEvent(aniCmp.atlasKey))
            }
        }

        val isDone = animationCmps.getOrNull(entity)?.isAnimationFinished() ?: true
        if (isDone) {
            farmCmp.state = FarmState.READY
        }
    }
}
