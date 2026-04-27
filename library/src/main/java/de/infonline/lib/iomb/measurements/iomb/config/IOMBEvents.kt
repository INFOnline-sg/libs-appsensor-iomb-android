package de.infonline.lib.iomb.measurements.iomb.config

object IOMBEvents {
    internal val data = mapOf(
        "advertisement" to mapOf(
            "open" to IOMBConfigData.Remote.ActiveEvent(),
            "close" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "application" to mapOf(
            "start" to IOMBConfigData.Remote.ActiveEvent(),
            "enterBackground" to IOMBConfigData.Remote.ActiveEvent(),
            "enterForeground" to IOMBConfigData.Remote.ActiveEvent(),
            "resignActive" to IOMBConfigData.Remote.ActiveEvent(),
            "becomeActive" to IOMBConfigData.Remote.ActiveEvent(),
            "terminate" to IOMBConfigData.Remote.ActiveEvent(),
            "crashed" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "audio" to mapOf(
            "play" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "pause" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "stop" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "next" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "previous" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "replay" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "seekBack" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "seekForward" to IOMBConfigData.Remote.ActiveEvent(pi = true)
        ),
        "data" to mapOf(
            "canceled" to IOMBConfigData.Remote.ActiveEvent(),
            "refresh" to IOMBConfigData.Remote.ActiveEvent(),
            "succeeded" to IOMBConfigData.Remote.ActiveEvent(),
            "failed" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "deviceOrientation" to mapOf(
            "changed" to IOMBConfigData.Remote.ActiveEvent(pi = true)
        ),
        "document" to mapOf(
            "open" to IOMBConfigData.Remote.ActiveEvent(),
            "edit" to IOMBConfigData.Remote.ActiveEvent(),
            "close" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "download" to mapOf(
            "canceled" to IOMBConfigData.Remote.ActiveEvent(),
            "start" to IOMBConfigData.Remote.ActiveEvent(),
            "succeeded" to IOMBConfigData.Remote.ActiveEvent(),
            "failed" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "game" to mapOf(
            "action" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "started" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "finished" to IOMBConfigData.Remote.ActiveEvent(),
            "won" to IOMBConfigData.Remote.ActiveEvent(),
            "lost" to IOMBConfigData.Remote.ActiveEvent(),
            "newhighscore" to IOMBConfigData.Remote.ActiveEvent(),
            "newachievement" to IOMBConfigData.Remote.ActiveEvent(),
            "highscore" to IOMBConfigData.Remote.ActiveEvent(),
            "achievement" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "gesture" to mapOf(
            "shake" to IOMBConfigData.Remote.ActiveEvent(pi = true)
        ),
        "hardwareButton" to mapOf(
            "pushed" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "iap" to mapOf(
            "started" to IOMBConfigData.Remote.ActiveEvent(),
            "finished" to IOMBConfigData.Remote.ActiveEvent(),
            "canceled" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "internetConnection" to mapOf(
            "established" to IOMBConfigData.Remote.ActiveEvent(),
            "lost" to IOMBConfigData.Remote.ActiveEvent(),
            "switchedInterface" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "login" to mapOf(
            "succeeded" to IOMBConfigData.Remote.ActiveEvent(),
            "failed" to IOMBConfigData.Remote.ActiveEvent(),
            "logout" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "openApp" to mapOf(
            "maps" to IOMBConfigData.Remote.ActiveEvent(),
            "other" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "push" to mapOf(
            "received" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "upload" to mapOf(
            "canceled" to IOMBConfigData.Remote.ActiveEvent(),
            "start" to IOMBConfigData.Remote.ActiveEvent(),
            "succeeded" to IOMBConfigData.Remote.ActiveEvent(),
            "failed" to IOMBConfigData.Remote.ActiveEvent()
        ),
        "video" to mapOf(
            "play" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "pause" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "stop" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "next" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "previous" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "replay" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "seekBack" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "seekForward" to IOMBConfigData.Remote.ActiveEvent(pi = true)
        ),
        "view" to mapOf(
            "appeared" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "refreshed" to IOMBConfigData.Remote.ActiveEvent(pi = true),
            "disappeared" to IOMBConfigData.Remote.ActiveEvent()
        ),
    )

}