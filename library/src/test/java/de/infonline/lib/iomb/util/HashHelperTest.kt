package de.infonline.lib.iomb.util

import de.infonline.lib.iomb.util.HashHelper.toMD5
import de.infonline.lib.iomb.util.HashHelper.toSHA1
import de.infonline.lib.iomb.util.HashHelper.toSHA256
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

class HashHelperTest : KotlinBaseTest() {

    val testInput = "The Cake Is A Lie"

    @Test
    fun `hash string to MD5`() {
        testInput.toMD5() shouldBe "e42997e37d8d70d4927b0b396254c179"
    }

    @Test
    fun `hash string to SHA256`() {
        testInput.toSHA256() shouldBe "3afc82e0c5df81d1733fe0c289538a1a1f7a5038d5c261860a5c83952f4bcb61"
    }

    @Test
    fun `hash string to SHA1`() {
        testInput.toSHA1() shouldBe "4d57f806e5f714ebdb5a74a12fda9523fae21d76"
    }
}