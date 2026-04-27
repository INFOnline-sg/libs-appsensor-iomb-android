package de.infonline.lib.iomb.measurements.iomb.config

import de.infonline.lib.iomb.IOLAdvertisementEvent
import de.infonline.lib.iomb.IOLCustomEvent
import de.infonline.lib.iomb.IOLGestureEvent
import de.infonline.lib.iomb.IOLViewEvent
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigData
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import java.time.Instant
import java.time.temporal.ChronoUnit

class IOMBConfigDataTest : KotlinBaseTest() {

    @Test
    fun `isExpired check`() {
        IOMBConfigData.Remote().isExpired shouldBe false
    }

    @Test
    fun `defined events, exact matches`() {
        val remoteConfig = IOMBConfigData.Remote().let {
            val newActiveEvents = mapOf(
                "view" to mapOf(
                    IOLViewEvent.IOLViewEventType.Appeared.name to IOMBConfigData.Remote.ActiveEvent(
                        audit = false,
                        regular = true,
                        pi = true
                    ),
                    IOLViewEvent.IOLViewEventType.Refreshed.name to IOMBConfigData.Remote.ActiveEvent(
                        audit = true,
                        regular = true,
                        pi = true
                    )
                )
            )
            it.copy(activeEvents = newActiveEvents)
        }
        val configData = IOMBConfigData(
            localConfig = IOMBConfig(),
            remoteConfig = remoteConfig
        )
        configData.isMeasuredRegular(IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared)) shouldBe true
        configData.isMeasuredRegular(IOLViewEvent(IOLViewEvent.IOLViewEventType.Refreshed)) shouldBe true
        configData.isMeasuredRegular(IOLViewEvent(IOLViewEvent.IOLViewEventType.Disappeared)) shouldBe false
        configData.isMeasuredAudit(IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared)) shouldBe false
        configData.isMeasuredAudit(IOLViewEvent(IOLViewEvent.IOLViewEventType.Refreshed)) shouldBe true
        configData.isMeasuredAudit(IOLViewEvent(IOLViewEvent.IOLViewEventType.Disappeared)) shouldBe false
        configData.isPIEvent(IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared)) shouldBe true
        configData.isPIEvent(IOLViewEvent(IOLViewEvent.IOLViewEventType.Refreshed)) shouldBe true
        configData.isPIEvent(IOLViewEvent(IOLViewEvent.IOLViewEventType.Disappeared)) shouldBe false
        configData.isMeasuredRegular(IOLAdvertisementEvent(IOLAdvertisementEvent.IOLAdvertisementEventType.Open)) shouldBe false
        configData.isMeasuredRegular(IOLCustomEvent("something")) shouldBe false
    }

    @Test
    fun `defined events, wildcard matches`() {
        val remoteConfig = IOMBConfigData.Remote().let {
            val newActiveEvents = mapOf(
                "view" to mapOf(
                    IOLViewEvent.IOLViewEventType.Appeared.name to IOMBConfigData.Remote.ActiveEvent(
                        audit = false,
                        regular = true,
                        pi = true
                    ),
                ),
                "custom" to mapOf(
                    "*" to IOMBConfigData.Remote.ActiveEvent(
                        audit = false,
                        regular = true,
                        pi = true
                    ),
                )
            )
            it.copy(activeEvents = newActiveEvents)
        }
        val configData = IOMBConfigData(
            localConfig = IOMBConfig(),
            remoteConfig = remoteConfig
        )
        configData.isMeasuredRegular(IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared)) shouldBe true
        configData.isMeasuredRegular(IOLViewEvent(IOLViewEvent.IOLViewEventType.Refreshed)) shouldBe false
        configData.isMeasuredRegular(IOLCustomEvent("something")) shouldBe true
        configData.isPIEvent(IOLCustomEvent("something")) shouldBe true
        configData.isMeasuredRegular(IOLGestureEvent(IOLGestureEvent.IOLGestureEventType.Shake)) shouldBe false
    }
}

private const val CONFIG_MISSING_VALUES = """{
	"formatVersion": "1.0.0",
	"configVersion": 123,
	"what is this": 1234.00,
	"activeEvents": {
		"view": {
			"appeared": {
				"audit": true,
				"pi": true,
				"regular": true
			},
			"refreshed": {
				"audit": true,
				"pi": true,
				"regular": true
			},
			"disappeared": {
				"audit": true,
				"pi": false,
				"regular": true
			}
		}
	}
}
"""