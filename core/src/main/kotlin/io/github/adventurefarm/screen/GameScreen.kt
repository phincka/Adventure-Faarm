package io.github.adventurefarm.screen

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.world
import io.github.adventurefarm.Main
import io.github.adventurefarm.component.ImageComponent.Companion.ImageComponentListener
import io.github.adventurefarm.component.PhysicComponent.Companion.PhysicComponentListener
import io.github.adventurefarm.component.StateComponent.Companion.StateComponentListener
import io.github.adventurefarm.event.MapChangeEvent
import io.github.adventurefarm.event.fire
import io.github.adventurefarm.input.PlayerInputProcessor
import io.github.adventurefarm.input.gdxInputProcessor
import io.github.adventurefarm.system.AnimationSystem
import io.github.adventurefarm.system.CameraSystem
import io.github.adventurefarm.system.CollisionDespawnSystem
import io.github.adventurefarm.system.CollisionSpawnSystem
import io.github.adventurefarm.system.EntitySpawnSystem
import io.github.adventurefarm.system.FarmSystem
import io.github.adventurefarm.system.MoveSystem
import io.github.adventurefarm.system.PhysicSystem
import io.github.adventurefarm.system.RenderSystem
import io.github.adventurefarm.ui.disposeSkin
import io.github.adventurefarm.ui.loadSkin
import io.github.adventurefarm.ui.model.GameModel
import io.github.adventurefarm.ui.view.gameView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.scene2d.actors


class GameScreen(game: Main) : KtxScreen {
    private val gameStage = game.gameStage
    private val uiStage = game.uiStage
    private val gameAtlas = TextureAtlas("graphics/game.atlas")
    private val phWorld = createWorld(gravity = Vector2.Zero).apply {
        autoClearForces = false
    }

    private val eWorld = world {
        injectables {
            add(phWorld)
            add("GameStage", gameStage)
            add("UiStage", uiStage)
            add("GameAtlas", gameAtlas)
        }

        components {
            add<PhysicComponentListener>()
            add<ImageComponentListener>()
            add<StateComponentListener>()
        }

        systems {
            add<EntitySpawnSystem>()
            add<PhysicSystem>()
            add<CollisionSpawnSystem>()
            add<CollisionDespawnSystem>()
            add<AnimationSystem>()
            add<MoveSystem>()
            add<FarmSystem>()
            add<CameraSystem>()
            add<RenderSystem>()
        }
    }

    init {
        loadSkin()
        eWorld.systems.forEach { sys ->
            if (sys is EventListener) {
                gameStage.addListener(sys)
            }
        }
        PlayerInputProcessor(eWorld, gameStage)
        gdxInputProcessor(uiStage)

        // UI
        uiStage.actors {
            gameView(GameModel(eWorld, gameStage))
        }
    }

    override fun show() {
        gameStage.fire(MapChangeEvent(TmxMapLoader().load("maps/demo.tmx")))
    }

    override fun render(delta: Float) {
        val dt = delta.coerceAtMost(0.25f)
        GdxAI.getTimepiece().update(dt)
        eWorld.update(dt)
    }

    override fun dispose() {
        eWorld.dispose()
        phWorld.disposeSafely()
        gameAtlas.disposeSafely()
        disposeSkin()
    }
}


