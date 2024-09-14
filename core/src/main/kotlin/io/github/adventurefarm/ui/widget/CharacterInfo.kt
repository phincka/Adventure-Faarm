package io.github.adventurefarm.ui.widget

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Scaling
import io.github.adventurefarm.ui.Drawables
import io.github.adventurefarm.ui.Labels
import io.github.adventurefarm.ui.get
import ktx.actors.plusAssign
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label

@Scene2dDsl
class CharacterInfo(
    charDrawable: Drawables?,
    private val skin: Skin,
    private val playerLevel: Int = 1
) : WidgetGroup(), KGroup {
    private val background: Image = Image(skin[Drawables.CHAR_INFO_BGD])
    private val charBgd: Image = Image(if (charDrawable == null) null else skin[charDrawable])
    private val lifeBar: Image = Image(skin[Drawables.LIFE_BAR])
    private val manaBar: Image = Image(skin[Drawables.MANA_BAR])
    private var userLevel: Int = playerLevel
    private val levelLabel: Label

    init {
        this += background
        this += charBgd.apply {
            setPosition(2f, 2f)
            setSize(22f, 20f)
            setScaling(Scaling.contain)
        }
        this += lifeBar.apply { setPosition(26f, 19f) }
        this += manaBar.apply { setPosition(26f, 13f) }

        levelLabel = label(
            text = "Poziom: $userLevel",
            style = Labels.SMALL.skinKey
        ).apply { setPosition(28f, 2f) }
        this += levelLabel
    }

    override fun getPrefWidth() = background.drawable.minWidth

    override fun getPrefHeight() = background.drawable.minHeight

    fun setLevel(level: Int) {
        userLevel = level
        levelLabel.setText("Poziom: $userLevel")
    }
}

@Scene2dDsl
fun <S> KWidget<S>.characterInfo(
    charDrawable: Drawables?,
    skin: Skin = Scene2DSkin.defaultSkin,
    playerLevel: Int,
    init: CharacterInfo.(S) -> Unit = {}
): CharacterInfo = actor(CharacterInfo(charDrawable, skin, playerLevel), init)
