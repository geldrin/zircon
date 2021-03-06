package org.codetome.zircon.terminal.swing

import org.codetome.zircon.font.Font
import org.codetome.zircon.terminal.Size
import org.codetome.zircon.terminal.config.DeviceConfiguration
import org.codetome.zircon.terminal.virtual.DefaultVirtualTerminal
import java.awt.AWTKeyStroke
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import java.awt.event.HierarchyEvent
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.SwingUtilities

/**
 * Concrete implementation of [Java2DTerminalImplementation] that adapts it to Swing.
 */
class SwingTerminalImplementation(
        private val component: JComponent,
        val font: Font<BufferedImage>,
        initialSize: Size,
        deviceConfiguration: DeviceConfiguration)

    : Java2DTerminalImplementation(
        deviceConfiguration = deviceConfiguration,
        font = font,
        virtualTerminal = DefaultVirtualTerminal(initialSize)) {

    init {
        //Prevent us from shrinking beyond one character
        component.minimumSize = Dimension(font.getWidth(), font.getHeight())
        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet<AWTKeyStroke>())
        component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, emptySet<AWTKeyStroke>())
        //Make sure the component is double-buffered to prevent flickering
        component.isDoubleBuffered = true
        component.addKeyListener(TerminalInputListener())
        component.addMouseListener(object : TerminalMouseListener() {
            override fun mouseClicked(e: MouseEvent) {
                super.mouseClicked(e)
                this@SwingTerminalImplementation.component.requestFocusInWindow()
            }
        })
        component.addHierarchyListener { e ->
            if (e.changeFlags == HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) {
                if (e.changed.isDisplayable) {
                    onCreated()
                } else {
                    onDestroyed()
                }
            }
        }
    }

    override fun getFontHeight() = font.getHeight()

    override fun getFontWidth() = font.getWidth()

    override fun getHeight() = component.height

    override fun getWidth() = component.width

    override fun isTextAntiAliased() = false // TODO: what to do?

    override fun repaint() {
        if (SwingUtilities.isEventDispatchThread()) {
            component.repaint()
        } else {
            SwingUtilities.invokeLater { component.repaint() }
        }
    }
}
