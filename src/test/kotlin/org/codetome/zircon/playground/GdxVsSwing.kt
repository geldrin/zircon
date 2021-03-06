package org.codetome.zircon.playground

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import org.codetome.zircon.builder.TextCharacterBuilder
import org.codetome.zircon.builder.TextColorFactory
import org.codetome.zircon.font.DFTilesetResource
import org.codetome.zircon.util.Stats
import java.awt.Canvas
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import javax.swing.JFrame
import javax.swing.SwingUtilities

object Config {
    val TILESET = DFTilesetResource.WANDERLUST_16X16
    val WIDTH = 60
    val HEIGHT = 30
    val PIXEL_WIDTH = WIDTH * TILESET.width
    val PIXEL_HEIGHT = HEIGHT * TILESET.height
}


// SWING

class MyPanel : Canvas() {
    val tilesetFont = DFTilesetResource.WANDERLUST_16X16.asJava2DFont()
    val chars = listOf('a', 'b')
    val bgColors = listOf(
            TextColorFactory.fromString("#112233"),
            TextColorFactory.fromString("#332211"))
    val fgColors = listOf(
            TextColorFactory.fromString("#ffaaff"),
            TextColorFactory.fromString("#aaffaa"))
    var currIdx = 0
    val pool = Executors.newFixedThreadPool(1)
    var measurements = 0
    var avgMs: Long = 0

    init {
        this.preferredSize = Dimension(Config.TILESET.width * Config.WIDTH, Config.TILESET.height * Config.HEIGHT)
        pool.submit {
            while (true) {
                try {
                    val start = System.nanoTime()
                    draw()
                    val end = System.nanoTime()
                    var totalMs = (end - start) / 1000 / 1000
                    avgMs = (avgMs * measurements + totalMs.toInt()) / ++measurements
                    if (measurements % 10 == 0) {
                        println(String.format("Current FPS is: %d. Average fps is %d. Render time is %d.",
                                1000 / totalMs,
                                1000 / avgMs,
                                totalMs))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun draw() {
        val bs = this.bufferStrategy
        val gc: Graphics2D

        try {
            gc = bs.drawGraphics as Graphics2D
        } catch (e: Exception) {
            draw()
            return
        }

        val char = TextCharacterBuilder.newBuilder()
                .foregroundColor(fgColors[currIdx])
                .backgroundColor(bgColors[currIdx])
                .character(chars[currIdx])
                .build()

        for (row in 0..Config.HEIGHT) {
            for (column in 0..Config.WIDTH) {
                gc.drawImage(
                        tilesetFont.fetchRegionForChar(char),
                        Config.TILESET.width * column,
                        Config.TILESET.height * row,
                        null)
            }
        }
        currIdx = if (currIdx == 0) 1 else 0
        gc.dispose()
        bs.show()
    }
}


private fun createAndShowGUI() {
    val f = JFrame("Swing")
    val panel = MyPanel()
    f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    f.add(panel)
    f.isResizable = false
    f.pack()
    f.setLocationRelativeTo(null)
    f.isVisible = true

    panel.ignoreRepaint = true
    panel.createBufferStrategy(2)
    panel.isFocusable = true
}

object SwingLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        SwingUtilities.invokeLater { createAndShowGUI() }
    }
}

// GDX

class GdxFont(private val source: Texture,
              private val width: Int,
              private val height: Int) {

    init {
        if (!source.textureData.isPrepared) {
            source.textureData.prepare()
        }
    }

    fun fetchRegionForChar(char: Char): TextureRegion {
        val cp437Idx = DFTilesetResource.fetchCP437IndexForChar(char)
        val x = cp437Idx.rem(16) * width
        val y = cp437Idx.div(16) * height
        return TextureRegion(source, x, y, width, height)
    }

}

class GdxExample : ApplicationAdapter() {
    lateinit var batch: SpriteBatch

    val chars = listOf('a', 'b')
    var currIdx = 0
    var loopCount = 0
    var running = true

    override fun create() {
        batch = SpriteBatch()
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        val width = Config.TILESET.width.toFloat()
        val height = Config.TILESET.height.toFloat()
        val font = GdxFont(
                source = Texture(Config.TILESET.path),
                width = Config.TILESET.width,
                height = Config.TILESET.height)
        val result = Pixmap(Config.PIXEL_WIDTH, Config.PIXEL_HEIGHT, Pixmap.Format.RGBA8888)
        Stats.addTimedStatFor("character drawing") {
            (0..Config.HEIGHT).forEach { row ->
                (0..Config.WIDTH).forEach { column ->

                    val region = font.fetchRegionForChar(
                            chars[currIdx])
                    val drawable = TextureRegionDrawable(region)
                    val tinted = drawable.tint(com.badlogic.gdx.graphics.Color(0.5f, 0.5f, 0f, 1f)) as SpriteDrawable
                    tinted.draw(batch,
                            column * width,
                            row * height + height,
                            width,
                            height)
//                    batch.draw(oldfont.fetchRegionForChar(
//                            chars[currIdx]),
//                            column * width,
//                            row * height + height)
                }
            }
            currIdx = if (currIdx == 0) 1 else 0
            loopCount++
            if (loopCount.rem(100) == 0) {
                Stats.printStats()
            }
        }

        batch.end()
    }

    override fun dispose() {
        batch.dispose()
    }
}

object GdxLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        LwjglApplication(GdxExample(), config)
    }
}

