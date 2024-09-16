package io.github.adventurefarm.ui.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Qualifier
import com.github.quillraven.fleks.World
import io.github.adventurefarm.component.PlayerComponent
import io.github.adventurefarm.event.UserHasNewLevel

class GameModel(
    world: World,
    stage: Stage,
    @Qualifier("Player") private val player: PlayerComponent,
    ) : PropertyChangeSource(), EventListener {

    private val playerCmps: ComponentMapper<PlayerComponent> = world.mapper()

    var playerLevel by propertyNotify(player.level)

    init {
        playerCmps
        stage.addListener(this)
    }

    private fun updateUser(user: PlayerComponent) {
        playerLevel = user.level
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is UserHasNewLevel -> {
                Gdx.app.log("USER", "GAME MODEL: ${player.exp}")

                updateUser(user = event.user)
            }

            else -> return false
        }
        return true
    }
}
