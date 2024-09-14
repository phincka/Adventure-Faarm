package io.github.adventurefarm.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.adventurefarm.component.CollisionComponent
import io.github.adventurefarm.component.PhysicComponent
import io.github.adventurefarm.component.PhysicComponent.Companion.physicCmpFromShape2D
import io.github.adventurefarm.component.TiledComponent
import io.github.adventurefarm.event.MapChangeEvent
import ktx.box2d.body
import ktx.box2d.loop
import ktx.collections.GdxArray
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.isEmpty
import ktx.tiled.shape
import ktx.tiled.width

@AllOf([CollisionComponent::class, PhysicComponent::class])
class CollisionSpawnSystem(
    private val physicWorld: World,
    private val physicCmps: ComponentMapper<PhysicComponent>,
) : EventListener, IteratingSystem() {
    private val tileLayers = GdxArray<TiledMapTileLayer>()
    private val processedCells = mutableSetOf<TiledMapTileLayer.Cell>()

    private fun TiledMapTileLayer.forEachCell(
        startX: Int,
        startY: Int,
        size: Int,
        action: (TiledMapTileLayer.Cell, Int, Int) -> Unit
    ) {
        for (x in startX - size..startX + size) {
            for (y in startY - size until startY + size) {
                this.getCell(x, y)?.let { action(it, x, y) }
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        // for collision entities we will spawn the collision objects around them that are not spawned yet
        val (entityX, entityY) = physicCmps[entity].body.position

        tileLayers.forEach { layer ->
            layer.forEachCell(entityX.toInt(), entityY.toInt(), SPAWN_AREA_SIZE) { tileCell, x, y ->
                if (tileCell.tile.objects.isEmpty()) {
                    // tileCell is not linked to a tile with collision objects -> do nothing
                    return@forEachCell
                }
                if (tileCell in processedCells) {
                    // tileCell already processed -> do nothing
                    return@forEachCell
                }

                processedCells.add(tileCell)
                   tileCell.tile.objects.forEach { mapObj ->
                    world.entity {
                        physicCmpFromShape2D(physicWorld, x, y, mapObj.shape)
                        add<TiledComponent> {
                            cell = tileCell
                            // add entity immediately here, otherwise the newly created
                            // collision entity might get removed by the CollisionDespawnSystem because
                            // the physic collision event will come later in the PhysicSystem when
                            // the physic world gets updated
                            nearbyEntities.add(entity)
                        }
                    }
                }
            }
        }
    }

    override fun handle(event: Event?): Boolean {
        if (event is MapChangeEvent) {
            processedCells.clear()
            event.map.layers.getByType(TiledMapTileLayer::class.java, tileLayers)

            // create map boundary collision object
            world.entity {
                val w = event.map.width.toFloat()
                val h = event.map.height.toFloat()
                add<PhysicComponent> {
                    body = physicWorld.body(BodyDef.BodyType.StaticBody) {
                        position.set(0f, 0f)
                        fixedRotation = true
                        allowSleep = false
                        loop(
                            vec2(0f, 0f),
                            vec2(w, 0f),
                            vec2(w, h),
                            vec2(0f, h),
                        )
                    }
                }
            }
            return true
        }
        return false
    }

    companion object {
        // increase from 1 to 7 to correctly throw Light shadows for objects
        const val SPAWN_AREA_SIZE = 7
    }
}
