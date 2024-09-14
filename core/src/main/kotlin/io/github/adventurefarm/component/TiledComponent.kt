package io.github.adventurefarm.component

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.github.quillraven.fleks.Entity

class TiledComponent {
    lateinit var cell: TiledMapTileLayer.Cell
    val nearbyEntities = mutableSetOf<Entity>()
}
