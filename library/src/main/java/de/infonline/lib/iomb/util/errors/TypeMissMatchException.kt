package de.infonline.lib.iomb.util.errors

class TypeMissMatchException(private val expected: Any, private val actual: Any)
    : IllegalArgumentException("Type missmatch: Wanted $expected, but got $actual.") {

    companion object {
        fun check(expected: Any, actual: Any) {
            if (expected != actual) throw TypeMissMatchException(expected, actual)
        }
    }

}