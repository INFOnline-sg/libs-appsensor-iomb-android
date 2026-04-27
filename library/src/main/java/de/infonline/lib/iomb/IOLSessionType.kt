package de.infonline.lib.iomb


enum class IOLSessionType(
    @JvmField val state: String
) {
    IOMB("IOMB");

    override fun toString(): String {
        return "IOLSessionType{" +
                "state='" + state + '\'' +
                '}'
    }

}