package de.infonline.lib.iomb

enum class IOLSessionPrivacySetting(
        @JvmField val privacyType: String
) {
    ACK("ack"), LIN("lin"), PIO("pio");

}