package de.infonline.lib.iomb.util


internal data class NTuple4<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

internal data class NTuple5<out A, out B, out C, out D, out E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
) {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}