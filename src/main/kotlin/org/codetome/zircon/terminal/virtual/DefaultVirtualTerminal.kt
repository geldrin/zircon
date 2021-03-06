package org.codetome.zircon.terminal.virtual

import org.codetome.zircon.Position
import org.codetome.zircon.TextCharacter
import org.codetome.zircon.behavior.CursorHolder
import org.codetome.zircon.behavior.Layerable
import org.codetome.zircon.behavior.impl.DefaultCursorHolder
import org.codetome.zircon.behavior.impl.DefaultLayerable
import org.codetome.zircon.input.Input
import org.codetome.zircon.input.KeyStroke
import org.codetome.zircon.terminal.AbstractTerminal
import org.codetome.zircon.terminal.Cell
import org.codetome.zircon.terminal.Size
import org.codetome.zircon.util.TextUtils
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class DefaultVirtualTerminal private constructor(initialSize: Size,
                                                 private val cursorHolder: CursorHolder,
                                                 private val layerable: Layerable)
    : AbstractTerminal(), VirtualTerminal,
        CursorHolder by cursorHolder,
        Layerable by layerable {

    private var terminalSize = initialSize
    private var wholeBufferDirty = false
    private var lastDrawnCursorPosition: Position = Position.UNKNOWN
    private val textBuffer: TextCharacterBuffer = TextCharacterBuffer()
    private val dirtyTerminalCells = TreeSet<Position>()
    private val listeners = mutableListOf<VirtualTerminalListener>()
    private val inputQueue = LinkedBlockingQueue<Input>()

    constructor(initialSize: Size = Size.DEFAULT)
            : this(
            initialSize = initialSize,
            cursorHolder = DefaultCursorHolder(),
            layerable = DefaultLayerable(
                    size = initialSize))

    init {
        dirtyTerminalCells.add(getCursorPosition())
    }

    @Synchronized
    override fun setCursorPosition(cursorPosition: Position) {
        cursorHolder.setCursorPosition(cursorPosition
                // this is not a bug! the cursor can extend beyond the last row
                .withColumn(Math.min(cursorPosition.column, terminalSize.columns))
                .withRow(Math.min(cursorPosition.row, terminalSize.rows - 1)))
        dirtyTerminalCells.add(getCursorPosition())
    }

    override fun getBoundableSize(): Size = terminalSize

    @Synchronized
    override fun setSize(newSize: Size) {
        if (newSize != terminalSize) {
            this.terminalSize = newSize
            textBuffer.resize(newSize)
            getCursorPosition().let { (cursorCol, cursorRow) ->
                if (cursorRow >= newSize.rows || cursorCol >= newSize.columns) {
                    setCursorPosition(Position(
                            column = Math.min(newSize.columns, cursorCol),
                            row = Math.min(newSize.rows, cursorRow)))
                }
            }
            wholeBufferDirty = true // TODO: this can be optimized later
            listeners.forEach { it.onResized(this, terminalSize) }
            super.onResized(newSize)
        }
    }

    @Synchronized
    override fun clear() {
        textBuffer.clear()
        setWholeBufferDirty()
        setCursorPosition(Position.DEFAULT_POSITION)
    }

    @Synchronized
    override fun putCharacter(c: Char) {
        if (c == '\n') {
            moveCursorToNextLine()
        } else if (TextUtils.isPrintableCharacter(c)) {
            putCharacter(TextCharacter(
                    character = c,
                    foregroundColor = getForegroundColor(),
                    backgroundColor = getBackgroundColor(),
                    modifiersToUse = getActiveModifiers()))
        }
    }

    @Synchronized
    internal fun putCharacter(textCharacter: TextCharacter) {
        checkCursorPosition()
        textBuffer.setCharacter(getCursorPosition(), textCharacter)
        dirtyTerminalCells.add(getCursorPosition())
        setCursorPosition(getCursorPosition().withRelativeColumn(1))
        checkCursorPosition()
        dirtyTerminalCells.add(getCursorPosition())
    }

    private fun checkCursorPosition() {
        if (cursorIsAtTheEndOfTheLine()) {
            moveCursorToNextLine()
        }
    }

    @Synchronized
    override fun flush() = listeners.forEach { it.onFlush() }

    override fun close() {
        inputQueue.add(KeyStroke.EOF_STROKE)
        listeners.forEach { it.onClose() }
    }

    @Synchronized
    override fun pollInput() = Optional.ofNullable(inputQueue.poll())

    override fun newTextGraphics() = VirtualTerminalTextGraphics(this)

    @Synchronized
    override fun addVirtualTerminalListener(listener: VirtualTerminalListener) {
        listeners.add(listener)
    }

    @Synchronized
    override fun removeVirtualTerminalListener(listener: VirtualTerminalListener) {
        listeners.remove(listener)
    }

    override fun addInput(input: Input) {
        inputQueue.add(input)
    }

    @Synchronized
    override fun getCharacter(position: Position): TextCharacter {
        return textBuffer.getCharacter(position)
    }

    @Synchronized
    override fun setCharacter(position: Position, textCharacter: TextCharacter) {
        textBuffer.setCharacter(position, textCharacter)
    }

    @Synchronized
    override fun forEachDirtyCell(fn: (Cell) -> Unit) {
        if (lastDrawnCursorPosition != getCursorPosition()
                && lastDrawnCursorPosition != Position.UNKNOWN) {
            dirtyTerminalCells.add(lastDrawnCursorPosition)
        }
        if (wholeBufferDirty) {
            textBuffer.forEachCell(fn)
            fn(Cell(getCursorPosition(), textBuffer.getCharacter(getCursorPosition())))
            wholeBufferDirty = false
        } else {
            dirtyTerminalCells.forEach { pos ->
                fn(Cell(pos, textBuffer.getCharacter(pos)))
            }
        }
        val blinkingChars = dirtyTerminalCells.filter { getCharacter(it).isBlinking() }
        dirtyTerminalCells.clear()
        dirtyTerminalCells.addAll(blinkingChars)
        this.lastDrawnCursorPosition = getCursorPosition()
        dirtyTerminalCells.add(getCursorPosition())
    }

    override fun forEachCell(fn: (Cell) -> Unit) {
        textBuffer.forEachCell(fn)
    }

    private fun moveCursorToNextLine() {
        setCursorPosition(getCursorPosition().withColumn(0).withRelativeRow(1))
        if (getCursorPosition().row >= textBuffer.getLineCount()) {
            textBuffer.newLine()
        }
    }

    private fun setWholeBufferDirty() {
        wholeBufferDirty = true
        dirtyTerminalCells.clear()
    }

    private fun cursorIsAtTheEndOfTheLine() = getCursorPosition().column == terminalSize.columns
}
