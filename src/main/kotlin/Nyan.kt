package nyan

import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.ArrayList
import java.util.LinkedList

fun main(args: Array<String>) {
    val stats = Stats()
    val ps = PrintStream(FileOutputStream(FileDescriptor.out))
    val nyan = NyanCat(50, stats, ps) // FIXME replace with whatever Gradle does to pull off terminal width calculation
    for (i in 0..255) {
        stats.passes = i
        nyan.draw()
        Thread.sleep(100)
    }
}

class NyanCat(val columns: Int, val stats: Stats, val out: PrintStream) {
    val width = columns * 0.75
    val rainbowColors = generateColors()
    val numberOfLines: Int = 4
    val trajectories = ArrayList<LinkedList<String>>()
    val nyanCatWidth = 11
    val trajectoryWidthMax = width - nyanCatWidth
    val scoreboardWidth: Int = 5

    var tick = false
    var colorIndex = 0

    init {
        for (i in 1..numberOfLines) {
            trajectories.add(LinkedList()) // FIXME replace with more appropriate collection
        }
    }

    fun draw() {
        appendRainbow()
        drawScoreboard()
        drawRainbow()
        drawNyanCat()
        tick = !tick
    }

    private fun appendRainbow() {
        var segment = if (tick) "_" else "-"
        var rainbowified = rainbowify(segment)
        for (index in 0..numberOfLines - 1) {
            var trajectory = trajectories[index]
            if (trajectory.size() >= trajectoryWidthMax) trajectory.removeLast()
            trajectory.push(rainbowified)
        }
    }

    private fun drawScoreboard() {
        fun draw(color: Int, n: Int) {
            write(" ")
            write(color(color, n.toString()))
            newline()
        }

        draw(32, stats.passes)
        draw(31, stats.failures)
        draw(36, stats.skipped)
        newline()

        cursorUp(numberOfLines)
    }

    private fun drawRainbow() {
        trajectories.forEach { line ->
            cursorForward(scoreboardWidth)
            write(line.toList().join("")) // FIXME looks like LinkedList doesn't get map/filter/join/etc
            newline()
        }

        cursorUp(numberOfLines)
    }

    private fun drawNyanCat() {
        val startWidth = scoreboardWidth + trajectories[0].size()
        cursorForward(startWidth)
        write("_,------,")
        newline()

        cursorForward(startWidth)
        var padding = if (tick) "  " else "   "
        write("_|${padding}/\\_/\\ ")
        newline()

        cursorForward(startWidth)
        padding = if (tick) "_" else "__"
        var tail = if (tick) "~" else "^"
        write(tail + '|' + padding + face() + ' ')
        newline()

        cursorForward(startWidth)
        padding = if (tick) " " else "  "
        write(padding + "\"\"  \"\" ")
        newline()

        cursorUp(numberOfLines)
    }

    private fun face(): String {
        if (stats.failures > 0) {
            return "( x .x)"
        } else if (stats.skipped > 0) {
            return "( o .o)"
        } else if (stats.passes > 0) {
            return "( ^ .^)"
        } else {
            return "( - .-)"
        }
    }

    private fun generateColors(): List<Int> {
        val colors = ArrayList<Int>()
        val numColours = 6 * 7
        for (i in 0..numColours) {
            val pi3 = Math.floor(Math.PI / 3)
            val n = (i * (1.0 / 6))
            val r = Math.floor(3 * Math.sin(n) + 3)
            val g = Math.floor(3 * Math.sin(n + 2 * pi3) + 3)
            val b = Math.floor(3 * Math.sin(n + 4 * pi3) + 3)
            val color = 36 * r + 6 * g + b + 16
            colors.add(color.toInt())
        }
        return colors
    }

    private fun rainbowify(s: String): String {
        var color = rainbowColors[colorIndex % rainbowColors.size()]
        colorIndex += 1
        return "%c[38;5;%dm%s%c[0m".format(escCode, color, s, escCode)
    }

    private fun write(s: String) {
        out.print(s)
    }

    fun newline() {
        out.println()
    }

    // VT100 cursor codes - http://www.termsys.demon.co.uk/vtansi.htm#cursor

    val escCode = 0x1B.toChar()

    fun cursorUp(n: Int) {
        write("%c[%dA".format(escCode, n))
    }

    fun cursorDown(n: Int) {
        write("%c[%dB".format(escCode, n))
    }

    fun cursorForward(n: Int) {
        write("%c[%dC".format(escCode, n))
    }

    fun color(color: Int, str: String): String {
        return "%c[%dm%s%c[0m".format(escCode, color, str, escCode)
    }
}

data class Stats(var failures: Int = 0, var skipped: Int = 0, var passes: Int = 0)

