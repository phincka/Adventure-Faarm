package io.github.adventurefarm.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import io.github.adventurefarm.component.FarmComponent
import io.github.adventurefarm.component.ImageComponent
import io.github.adventurefarm.component.MoveComponent
import io.github.adventurefarm.component.PhysicComponent
import io.github.adventurefarm.component.PlayerComponent
import ktx.app.KtxInputAdapter

fun gdxInputProcessor(processor: InputProcessor) {
    val currProcessor = Gdx.input.inputProcessor
    if (currProcessor == null) {
        Gdx.input.inputProcessor = processor
    } else {
        if (currProcessor is InputMultiplexer) {
            if (processor !in currProcessor.processors) {
                currProcessor.addProcessor(processor)
            }
        } else {
            Gdx.input.inputProcessor = InputMultiplexer(currProcessor, processor)
        }
    }
}

class PlayerInputProcessor(
    world: World,
    private val gameStage: Stage,
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val posCmps: ComponentMapper<PhysicComponent> = world.mapper(),
    private val attackCmps: ComponentMapper<FarmComponent> = world.mapper(),
    private val imgCmps: ComponentMapper<ImageComponent> = world.mapper()
) : KtxInputAdapter {
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))
    private val posEntities =
        world.family(allOf = arrayOf(PhysicComponent::class, ImageComponent::class))
    private var targetX = 0f
    private var targetY = 0f
    private var paused = false

    init {
        gdxInputProcessor(this)
    }

    private fun updatePlayerMovementTowardsTarget() {
        playerEntities.forEach { player ->
            with(moveCmps[player]) {
                val physicCmp = posCmps[player]
                val playerPosX = physicCmp.body.position.x
                val playerPosY = physicCmp.body.position.y

                targetX = this@PlayerInputProcessor.targetX
                targetY = this@PlayerInputProcessor.targetY

                val deltaX = targetX - playerPosX
                val deltaY = targetY - playerPosY
                val distance = Vector2(deltaX, deltaY).len()

                if (distance > 1f) {
                    cosSin.set(deltaX, deltaY).nor()
                    speed = 5f
                } else {
                    cosSin.set(0f, 0f)
                }

                playerEntities.forEach {
                    attackCmps[it].doFarm = true
                }
            }
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val worldCoords =
            gameStage.viewport.unproject(Vector2(screenX.toFloat(), screenY.toFloat()))
        targetX = worldCoords.x
        targetY = worldCoords.y

        updatePlayerMovementTowardsTarget()

        return true
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val worldCoords =
            gameStage.viewport.unproject(Vector2(screenX.toFloat(), screenY.toFloat()))
        targetX = worldCoords.x
        targetY = worldCoords.y

        updatePlayerMovementTowardsTarget()
        return true
    }
}
