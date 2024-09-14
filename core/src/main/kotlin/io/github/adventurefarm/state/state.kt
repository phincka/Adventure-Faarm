package io.github.adventurefarm.state

import com.badlogic.gdx.graphics.g2d.Animation
import io.github.adventurefarm.component.AnimationType
import io.github.adventurefarm.component.EntityState
import io.github.adventurefarm.component.StateEntity

enum class DefaultState : EntityState {
    IDLE {
        override fun enter(stateEntity: StateEntity) {
            stateEntity.animation(AnimationType.IDLE)
        }

        override fun update(stateEntity: StateEntity) {
            when {
                stateEntity.wantsToFarm -> stateEntity.state(ATTACK)
                stateEntity.wantsToMove -> stateEntity.state(RUN)
            }
        }
    },
    RUN {
        override fun enter(stateEntity: StateEntity) {
            stateEntity.animation(AnimationType.RUN)
        }

        override fun update(stateEntity: StateEntity) {
            when {
                stateEntity.wantsToFarm -> stateEntity.state(ATTACK)
                !stateEntity.wantsToMove -> stateEntity.state(IDLE)
            }
        }
    },
    ATTACK {
        override fun enter(stateEntity: StateEntity) {
            with(stateEntity) {
                animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL)
                moveCmp.root = true
                startFarm()
            }
        }

        override fun exit(stateEntity: StateEntity) {
            stateEntity.moveCmp.root = false
        }

        override fun update(stateEntity: StateEntity) {
            val farmCmp = stateEntity.farmCmp
            if (farmCmp.isReady() && !farmCmp.doFarm) {
                // done farm
                stateEntity.changeToPreviousState()
            } else if (farmCmp.isReady()) {
                // start another farm
                stateEntity.resetAnimation()
                farmCmp.startFarm()
            }
        }
    };
}
