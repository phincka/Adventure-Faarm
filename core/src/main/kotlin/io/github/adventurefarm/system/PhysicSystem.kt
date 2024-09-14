package io.github.adventurefarm.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import io.github.adventurefarm.component.CollisionComponent
import io.github.adventurefarm.component.ImageComponent
import io.github.adventurefarm.component.PhysicComponent
import io.github.adventurefarm.component.TiledComponent
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

@AllOf(components = [PhysicComponent::class, ImageComponent::class])
class PhysicSystem(
    private val physicWorld: World,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val tiledCmps: ComponentMapper<TiledComponent>,
    private val collisionCmps: ComponentMapper<CollisionComponent>,
) : IteratingSystem(interval = Fixed(1 / 60f)), ContactListener {
    init {
        physicWorld.setContactListener(this)
    }

    override fun onUpdate() {
        if (physicWorld.autoClearForces) {
            LOG.error { "AutoClearForces must be set to false to guarantee a correct physic step behavior." }
            physicWorld.autoClearForces = false
        }
        super.onUpdate()
        physicWorld.clearForces()
    }

    override fun onTick() {
        super.onTick()
        physicWorld.step(deltaTime, 6, 2)
    }

    override fun onTickEntity(entity: Entity) {
        val physicCmp = physicCmps[entity]
        physicCmp.prevPos.set(physicCmp.body.position)

        if (!physicCmp.impulse.isZero) {
            physicCmp.body.applyLinearImpulse(physicCmp.impulse, physicCmp.body.worldCenter, true)
            physicCmp.impulse.setZero()
        }
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val imageCmp = imageCmps[entity]
        val physicCmp = physicCmps[entity]

        imageCmp.image.run {
            val (prevX, prevY) = physicCmp.prevPos
            val (bodyX, bodyY) = physicCmp.body.position

            setPosition(
                MathUtils.lerp(prevX, bodyX, alpha) - width * 0.5f,
                MathUtils.lerp(prevY, bodyY, alpha) - height * 0.5f
            )
        }
    }

    private val Fixture.entity: Entity
        get() = this.body.userData as Entity

    private val Contact.isSensorA: Boolean
        get() = this.fixtureA.isSensor

    private val Contact.isSensorB: Boolean
        get() = this.fixtureB.isSensor

    override fun beginContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity

        when {
            entityA in tiledCmps && entityB in collisionCmps && contact.isSensorA && !contact.isSensorB -> {
                tiledCmps[entityA].nearbyEntities += entityB
            }

            entityB in tiledCmps && entityA in collisionCmps && contact.isSensorB && !contact.isSensorA -> {
                tiledCmps[entityB].nearbyEntities += entityA
            }
        }
    }

    override fun endContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity

        when {
            entityA in tiledCmps && contact.isSensorA && !contact.isSensorB -> {
                tiledCmps[entityA].nearbyEntities -= entityB
            }

            entityB in tiledCmps && contact.isSensorB && !contact.isSensorA -> {
                tiledCmps[entityB].nearbyEntities -= entityA
            }
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        contact.isEnabled =
            (contact.fixtureA.body.type == StaticBody && contact.fixtureB.body.type == DynamicBody) ||
                (contact.fixtureB.body.type == StaticBody && contact.fixtureA.body.type == DynamicBody)
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) = Unit

    companion object {
        private val LOG = logger<PhysicSystem>()
    }
}
