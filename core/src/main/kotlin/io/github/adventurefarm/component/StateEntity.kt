package io.github.adventurefarm.component

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.adventurefarm.state.DefaultState

data class StateEntity(
    val entity: Entity,
    val world: World,
    private val stateCmps: ComponentMapper<StateComponent> = world.mapper(),
    private val animationCmps: ComponentMapper<AnimationComponent> = world.mapper(),
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val farmCmps: ComponentMapper<FarmComponent> = world.mapper(),
) {
    val wantsToMove: Boolean
        get() {
            val moveCmp = moveCmps[entity]
            return !moveCmp.cosSin.isZero
        }

    val wantsToFarm: Boolean
        get() = farmCmps.getOrNull(entity)?.doFarm ?: false

    val isAnimationDone: Boolean
        get() = animationCmps[entity].isAnimationFinished()

    val moveCmp: MoveComponent
        get() = moveCmps[entity]

    val farmCmp: FarmComponent
        get() = farmCmps[entity]

    fun animation(type: AnimationType, mode: PlayMode = PlayMode.LOOP, resetAnimation: Boolean = false) {
        with(animationCmps[entity]) {
            nextAnimation(type)
            this.mode = mode
            if (resetAnimation) {
                stateTime = 0f
            }
        }
    }

    fun resetAnimation() {
        animationCmps[entity].stateTime = 0f
    }

    fun state(newState: EntityState, changeImmediate: Boolean = false) {
        with(stateCmps[entity]) {
            nextState = newState
            if (changeImmediate) {
                stateMachine.changeState(newState)
            }
        }
    }

    fun changeToPreviousState() {
        with(stateCmps[entity]) {
            nextState = stateMachine.previousState
        }
    }

    fun startFarm() {
        farmCmps[entity].startFarm()
    }
}

interface EntityState : State<StateEntity> {
    override fun enter(stateEntity: StateEntity) = Unit

    override fun update(stateEntity: StateEntity) = Unit

    override fun exit(stateEntity: StateEntity) = Unit

    override fun onMessage(stateEntity: StateEntity, telegram: Telegram) = false
}

data class StateComponent(
    var nextState: EntityState = DefaultState.IDLE,
    val stateMachine: DefaultStateMachine<StateEntity, EntityState> = DefaultStateMachine()
) {
    companion object {
        class StateComponentListener(
            private val world: World
        ) : ComponentListener<StateComponent> {
            override fun onComponentAdded(entity: Entity, component: StateComponent) {
                component.stateMachine.owner = StateEntity(entity, world)
            }

            override fun onComponentRemoved(entity: Entity, component: StateComponent) = Unit
        }
    }
}
