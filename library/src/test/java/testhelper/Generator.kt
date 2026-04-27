package testhelper

object Generator {

    fun randomString(length: Long): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..length).map { allowedChars.random() }.joinToString("")
    }

}