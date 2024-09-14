package io.github.adventurefarm.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import io.github.adventurefarm.component.TiledComponent
import io.github.adventurefarm.event.CollisionDespawnEvent
import io.github.adventurefarm.event.fire

@AllOf([TiledComponent::class])
class CollisionDespawnSystem(
    @Qualifier("GameStage") private val stage: Stage,
    private val tiledCmps: ComponentMapper<TiledComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {

//        Gdx.app.log("ONtick", "CollisionDespawnSystem onTickEntity")

        // for existing collision tiled entities we check if there are no nearby entities anymore
        // and remove them in that case
        if (tiledCmps[entity].nearbyEntities.isEmpty()) {
            stage.fire(CollisionDespawnEvent(tiledCmps[entity].cell))
            world.remove(entity)
        }
    }
}
