package de.infonline.lib.iomb.util.extensions


inline fun <V> List<V>.mutate(block: MutableList<V>.() -> Unit): List<V> {
    return toMutableList().apply(block).toList()
}