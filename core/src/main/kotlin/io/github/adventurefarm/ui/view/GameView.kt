package io.github.adventurefarm.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import io.github.adventurefarm.ui.Drawables
import io.github.adventurefarm.ui.model.GameModel
import io.github.adventurefarm.ui.widget.CharacterInfo
import io.github.adventurefarm.ui.widget.characterInfo
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

class GameView(
    model: GameModel,
    skin: Skin,
) : Table(skin), KTable {

    private val playerInfo: CharacterInfo
    private val playerLevel = model.playerLevel

    init {
        setFillParent(true)
        playerInfo = characterInfo(Drawables.PLAYER, skin, playerLevel)
        top().left().pad(10f)

        model.onPropertyChange(GameModel::playerLevel) { level ->
            playerLevel(level)
        }
    }

    private fun playerLevel(level: Int) = playerInfo.setLevel(level)
}

@Scene2dDsl
fun <S> KWidget<S>.gameView(
    model: GameModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: GameView.(S) -> Unit = {}
): GameView = actor(GameView(model, skin), init)
