package io.github.adventurefarm.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
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
import io.github.adventurefarm.event.MapChangeEvent
import io.github.adventurefarm.event.UserHasNewLevel
import io.github.adventurefarm.event.fire
import io.github.adventurefarm.system.EntitySpawnSystem.Companion.HIT_BOX_SENSOR
import ktx.box2d.query
import ktx.tiled.forEachLayer

@NoneOf([DisarmComponent::class])
class LevelSystem(
    @Qualifier("Player") private val player: PlayerComponent,
) : IteratingSystem() {
    private val expForLevel = listOf(0, 50, 150, 300, 500, 750, 1050, 1400, 1800, 2250)

    override fun onTickEntity(entity: Entity) {
        if (player.addExp > 0 ) {
            addExp(player.addExp)
        }
    }

    private fun addExp(exp: Int) {
        player.exp += exp
        player.addExp = 0
        checkExpForLevelUp()
    }

    private fun checkExpForLevelUp() {
        while (player.level < expForLevel.size && player.exp >= expForLevel[player.level]) {
            levelUp()
        }
    }

    private fun levelUp() {
        player.level++
    }
}
