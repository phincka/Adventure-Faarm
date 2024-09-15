package io.github.adventurefarm.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import io.github.adventurefarm.component.PlayerComponent

fun Stage.fire(event: Event) = this.root.fire(event)

data class MapChangeEvent(val map: TiledMap) : Event()

data class CollisionDespawnEvent(val cell: Cell) : Event()

data class EntityFarmEvent(val atlasKey: String) : Event()

data class UserHasNewLevel(val user: PlayerComponent) : Event()
