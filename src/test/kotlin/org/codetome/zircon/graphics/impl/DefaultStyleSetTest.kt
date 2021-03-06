package org.codetome.zircon.graphics.impl

import org.assertj.core.api.Assertions.assertThat
import org.codetome.zircon.ANSITextColor
import org.codetome.zircon.Modifier
import org.codetome.zircon.Modifier.*
import org.codetome.zircon.builder.TextColorFactory
import org.codetome.zircon.graphics.style.DefaultStyleSet
import org.junit.Before
import org.junit.Test

class DefaultStyleSetTest {

    lateinit var target: DefaultStyleSet

    @Before
    fun setUp() {
        target = DefaultStyleSet()
    }

    @Test
    fun shouldHaveNoModifiersByDefault() {
        assertThat(target.getActiveModifiers()).isEmpty()
    }

    @Test
    fun shouldHaveProperFGByDefault() {
        assertThat(target.getForegroundColor())
                .isEqualTo(TextColorFactory.DEFAULT_FOREGROUND_COLOR)
    }

    @Test
    fun shouldHaveProperBGByDefault() {
        assertThat(target.getBackgroundColor())
                .isEqualTo(TextColorFactory.DEFAULT_BACKGROUND_COLOR)
    }

    @Test
    fun shouldProperlyEnableModifier() {
        val modifier = BOLD

        target.enableModifier(modifier)

        assertThat(target.getActiveModifiers()).containsExactly(modifier)
    }

    @Test
    fun shouldProperlyDisableModifier() {
        val modifier = BOLD

        target.enableModifier(modifier)
        target.disableModifier(modifier)

        assertThat(target.getActiveModifiers()).isEmpty()
    }

    @Test
    fun shouldProperlyEnableModifiers() {
        val modifiers = setOf(BOLD, CROSSED_OUT).toTypedArray()

        target.enableModifiers(*modifiers)

        assertThat(target.getActiveModifiers()).containsExactlyInAnyOrder(*modifiers)
    }

    @Test
    fun shouldProperlySetModifiers() {
        val modifiers = setOf(BOLD, CROSSED_OUT)

        target.setModifiers(modifiers)

        assertThat(target.getActiveModifiers()).containsExactlyInAnyOrder(*modifiers.toTypedArray())
    }

    @Test
    fun shouldProperlyClearModifiers() {
        target.enableModifier(BOLD)

        target.clearModifiers()

        assertThat(target.getActiveModifiers())
                .isEmpty()
    }

    companion object {
        val EXPECTED_BG_COLOR = ANSITextColor.YELLOW
        val EXPECTED_FG_COLOR = ANSITextColor.CYAN
        val EXPECTED_MODIFIERS = setOf(CROSSED_OUT, BLINK)

        val OTHER_STYLE = DefaultStyleSet(
                foregroundColor = EXPECTED_FG_COLOR,
                backgroundColor = EXPECTED_BG_COLOR,
                modifiers = EXPECTED_MODIFIERS)
    }
}