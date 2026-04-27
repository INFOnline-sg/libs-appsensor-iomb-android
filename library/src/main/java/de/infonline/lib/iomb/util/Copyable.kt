package de.infonline.lib.iomb.util

interface Copyable<T> {
    fun copy(fields: T.() -> T): T
}