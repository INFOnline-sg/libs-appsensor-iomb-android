package de.infonline.lib.iomb.events

import de.infonline.lib.iomb.IOLEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class IOLBaseEventTest {
    val goodLengthAndChars = """1234567890ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz,/_-...............................................................n..............t..............................................................................................................."""
    val badLengthAndChars = """1234567890ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz,/_-.........................!@#${'$'}%ˆ*()=?><|\\][{}\"'˜`±¡™£¢∞§¶•ªº–\n
            \t  ......................................................................................................................................................."""
    @Test
    fun `category sanitization `() {
        val event = object : IOLEvent(
                identifier = "identifier",
                category = badLengthAndChars,
                comment = null,
                state = null,
                customParams = null
        ) {}
        event.category shouldBe goodLengthAndChars
    }

    @Test
    fun `state sanitization `() {
        val event = object : IOLEvent(
                identifier = "identifier",
                category = null,
                comment = null,
                state = badLengthAndChars,
                customParams = null
        ) {}
        event.state shouldBe badLengthAndChars.substring(0, 255)
    }

    @Test
    fun `comment sanitization `() {

        val baadComment = "1234567890ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz ¡™£¢∞§¶•ªºLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONG"
        val goodComment = "1234567890ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz ..........LOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTISTOOLONGLOREMIPSUMTHISTEXTIST"

        val event = object : IOLEvent(
                identifier = "identifier",
                category = null,
                comment = baadComment,
                state = null,
                customParams = null
        ) {}
        event.comment shouldBe goodComment
    }

    @Test
    fun `customParams sanitization `() {
        val event = object : IOLEvent(
                identifier = "identifier",
                category = null,
                comment = null,
                state = null,
                customParams = mapOf(
                        "key$badLengthAndChars" to "value$badLengthAndChars"
                )
        ) {}
        event.customParams shouldBe mapOf(
                "key$badLengthAndChars".substring(0, 255) to "value$badLengthAndChars".substring(0, 255)
        )
    }
}