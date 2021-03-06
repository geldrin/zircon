package org.codetome.zircon.builder

import org.codetome.zircon.TextColor
import org.codetome.zircon.terminal.config.CursorStyle
import org.codetome.zircon.terminal.config.DeviceConfiguration

/**
 * Builder for [org.codetome.zircon.terminal.config.DeviceConfiguration]s.
 */
class DeviceConfigurationBuilder {

    private var blinkLengthInMilliSeconds: Long = 500
    private var cursorStyle: CursorStyle = CursorStyle.REVERSED
    private var cursorColor: TextColor = TextColorFactory.DEFAULT_FOREGROUND_COLOR
    private var cursorBlinking: Boolean = false
    private var clipboardAvailable: Boolean = true

    /**
     * Sets the length of a blink. All blinking characters will use this setting.
     */
    fun blinkLengthInMilliSeconds(blinkLengthInMilliSeconds: Long) = also {
        this.blinkLengthInMilliSeconds = blinkLengthInMilliSeconds
    }

    /**
     * Sets the cursor style. See: [CursorStyle].
     */
    fun cursorStyle(cursorStyle: CursorStyle) = also {
        this.cursorStyle = cursorStyle
    }

    /**
     * Sets the color of the cursor.
     */
    fun cursorColor(cursorColor: TextColor) = also {
        this.cursorColor = cursorColor
    }

    /**
     * Sets whether the cursor blinks or not.
     */
    fun cursorBlinking(cursorBlinking: Boolean) = also {
        this.cursorBlinking = cursorBlinking
    }

    /**
     * Enables or disables clipboard. <code>Shift + Insert</code> will paste text
     * at the cursor location if clipboard is available.
     */
    fun clipboardAvailable(clipboardAvailable: Boolean) = also {
        this.clipboardAvailable = clipboardAvailable
    }

    fun build() = DeviceConfiguration(
            blinkLengthInMilliSeconds = blinkLengthInMilliSeconds,
            cursorStyle = cursorStyle,
            cursorColor = cursorColor,
            isCursorBlinking = cursorBlinking,
            isClipboardAvailable = clipboardAvailable)

    companion object {

        @JvmStatic
        fun newBuilder() = DeviceConfigurationBuilder()

        @JvmStatic
        fun getDefault() = newBuilder().build()
    }
}