package io.github.adventurefarm.component

enum class FarmState {
    READY, PREPARE, ATTACKING
}

data class FarmComponent(
    var doFarm: Boolean = false,
    var damage: Int = 0,
    var delay: Float = 0f,
    var maxDelay: Float = 0f,
    var extraRange: Float = 0f,
    var state: FarmState = FarmState.READY,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
) {
    fun isReady() = state == FarmState.READY

    fun startFarm() {
        state = FarmState.PREPARE
    }
}
