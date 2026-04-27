package e2e.base

import android.app.Application
import android.content.Context
import android.content.pm.ProviderInfo
import android.net.ConnectivityManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import de.infonline.lib.iomb.IOMB
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.IOLViewEvent
import de.infonline.lib.iomb.core.IOLInitProvider
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import de.infonline.lib.iomb.measurements.iomb.IOMBMeasurement
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import de.infonline.lib.iomb.measurements.iomb.dispatch.IOMBEventDispatcher
import de.infonline.lib.iomb.measurements.common.SecureSettingsWrap
import de.infonline.lib.iomb.measurements.common.config.ConfigManager
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import de.infonline.lib.iomb.util.BuildConfigWrap
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowConnectivityManager
import testhelper.*
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@Ignore
internal class IOMBMockServerTest : KotlinBaseTest() {

    // TODO: adjust test for iomb

    lateinit var eventServer: MockWebServer
    lateinit var configServer: MockWebServer

    private var shadowConnectivityManager: ShadowConnectivityManager? = null

    val testScheduler = TestScheduler()
    lateinit var defaultSetup: IOMBSetup
    val defaultConfig = IOMBConfig()

    @Before
    fun setup() {
        eventServer = MockWebServer()
        eventServer.start()
        configServer = MockWebServer()
        configServer.start()

        val apiUrlEvents = eventServer.url("/events-here")
        val apiUrlConfig = configServer.url("/config-here")

        mockkObject(SecureSettingsWrap)
        every { SecureSettingsWrap.getString(any(), "bluetooth_name") } returns "iotest5ee391701234"

//        BuildConfigWrap.isDebugBuild shouldBe true

        defaultSetup = IOMBSetup(baseUrl = apiUrlEvents.toString(), offerIdentifier = "iamtest")

        val connectivityManager = RuntimeEnvironment.systemContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        shadowConnectivityManager = Shadows.shadowOf(connectivityManager)

        val application = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(application)

        val info = ProviderInfo().apply {
            authority = "my.authority"
            grantUriPermissions = true
        }
        // Init IOL
        Robolectric.buildContentProvider(IOLInitProvider::class.java).create(info)
    }

    @After
    fun teardown() {
        IOMB.getAllBlocking().forEach {
            IOMB.delete(it.key.measurementKey).blockingGet() shouldBe (it.value != null)
        }
    }

    @Test
    fun `initialization and release`() {
        IOMB.getBlocking(Measurement.Type.IOMB) shouldBe null

        val measurement = IOMB.createBlocking(defaultSetup) as IOMBMeasurement
        measurement.localConfig.testObservableFirstValue(testScheduler) shouldBe defaultConfig
        measurement.isReleased shouldBe false
        IOMB.getBlocking(Measurement.Type.IOMB) shouldBe measurement

        IOMB.deleteBlocking(Measurement.Type.IOMB)
        measurement.isReleased shouldBe true
        IOMB.getBlocking(Measurement.Type.IOMB) shouldBe null

        measurement.localConfig.testObservableFirstValue(testScheduler) shouldBe defaultConfig
    }

    @Test
    fun `dispatch one batch of events`() {
        // Dispatch spies are not available in release builds
        if (!BuildConfigWrap.isDebugBuild) return

        IOMB.getBlocking(Measurement.Type.IOMB) shouldBe null
        configServer.enqueue(MockResponse().setBody(RELAY_CONFIG_THAT_INSERTS_OUR_RSA_KEY))
        eventServer.enqueue(MockResponse().setBody(RELAY_DEFAULT_OK_RESPONSE))

        val measurement = IOMB.createBlocking(defaultSetup)

        val dispatchSpy = IOLDebug.dispatchSpy[defaultSetup.measurementKey]!!.subscribeOn(Schedulers.computation()).test()

        // Wait for the initial config check to be done
        measurement.remoteConfigInfo.test().apply {
            awaitCount(2)
            dispose()
        }

        for (i in 0 until 2) {
            measurement.logEvent(IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared, category = "category", comment = "#$i"))
        }

/*        IOLDebug.cacheSpy[defaultSetup.measurementKey]!!.data.testAwaitUntil {
            it as StandardEventCache.State
            it.inQueue.size == 2
        }*/

        val (internalRequest, internalResponse) = dispatchSpy.awaitCount(1).values()[0]
        internalRequest as IOMBEventDispatcher.Request
        internalRequest.events.size shouldBe 2

        internalResponse as EventDispatcher.Response
        internalResponse.configStatusCode shouldBe ConfigManager.Status.OK

        IOMB.deleteBlocking(Measurement.Type.IOMB)
        measurement.isReleased shouldBe true
        IOMB.getBlocking(Measurement.Type.IOMB) shouldBe null

        val eventServerRequest = eventServer.takeRequest(10, TimeUnit.SECONDS)!!

        val postedJSON = eventServerRequest.body.readUtf8().toJSONArray()

        val event0API = postedJSON.getJSONObject(0).getString("api")
        val event0Checksum = postedJSON.getJSONObject(0).getString("cs")
        val event0Secret = postedJSON.getJSONObject(0).getString("rsa")

        val event1API = postedJSON.getJSONObject(1).getString("api")
        val event1Checksum = postedJSON.getJSONObject(1).getString("cs")
        val event1Secret = postedJSON.getJSONObject(1).getString("rsa")

        postedJSON.toFormattedJson() shouldBe """
            [
                {
                    "api": "$event0API",
                    "rsa": "$event0Secret",
                    "cs": "$event0Checksum",
                    "mo" : 0,
                    "rp" : 1,
                    "sm": 2,
                    "ty": "app",
                    "st": "iamtest"
                },
                {
                    "api": "$event1API",
                    "rsa": "$event1Secret",
                    "cs": "$event1Checksum",
                    "mo" : 0,
                    "rp" : 1,
                    "sm": 2,
                    "ty": "app",
                    "st": "iamtest"
                }
            ]
        """.toFormattedJson()

/*        val decryptedEvents = postedJSON.asSequence()
                .map { event ->
                    val dispatchEncryption = DispatchEncryption(
                            SecureRandom(),
                            PasswordCrypto(defaultSetup),
                            DataCrypto(defaultSetup, SecureRandom())
                    )
                    dispatchEncryption.decrypt(
                            DispatchEncryption.Request.SecurityMode.PBKDF2withSHA1,
                            PRIVATE_KEY_RSA,
                            DispatchEncryption.Result(
                                    encryptionMode = DispatchEncryption.Request.SecurityMode.PBKDF2withSHA1,
                                    encryptedData = event.getString("api"),
                                    encryptedSecret = event.getString("rsa"),
                                    preEncryptionSHA1 = "doesn't matter currently"
                            )
                    )
                }
                .map { JSONObject(it) }
                .toList()

        val originalPayloadDecrypted = JSONArray(decryptedEvents)*/

        val sortedInternalEvents = internalRequest.events.sortedBy {
            it.createdAt
        }
        val event0Version = (sortedInternalEvents[0].event["ti"] as Map<*, *>)["vr"]
        val event0Timestamp = (sortedInternalEvents[0].event["di"] as Map<*, *>)["lt"]
        val event0TIOLConfigTTL = (sortedInternalEvents[0].event["ti"] as Map<*, *>)["tt"]

        val event1Version = (sortedInternalEvents[0].event["ti"] as Map<*, *>)["vr"]
        val event1Timestamp = (sortedInternalEvents[1].event["di"] as Map<*, *>)["lt"]
        val event1TIOLConfigTTL = (sortedInternalEvents[1].event["ti"] as Map<*, *>)["tt"]

        val event1LastTransactionViewTime = sortedInternalEvents[1].event["md"].let { md ->
            md as Map<*, *>
            md["st"].let { st ->
                st as Map<*, *>
                st["vt"]
            }
        }

       /* // Deshuffle the events
        val payloadWithSortedEvents = originalPayloadDecrypted.asSequence()
                .sortedBy { it.getJSONObject("di").getLong("lt") }
                .let {
                    JSONArray(it.toList())
                }

        payloadWithSortedEvents.toFormattedJson() shouldBe """
        [
            {
                "sv": "0.0.1",
                "si": {
                    "cn": "US",
                    "st": "iamtest",
                    "cp": "category",
                    "pt": "ap",
                    "ev": "view.appeared",
                    "co": "#0"
                },
                "di": {
                    "dp": "unknown,robolectric,robolectric,Android,robolectric,robolectric",
                    "dd": 160,
                    "dy": "smartphone",
                    "dx": "",
                    "dn": "offline",
                    "lt": $event0Timestamp,
                    "pn": "android",
                    "pv": "8.1.0",
                    "tz": -60,
                    "xy": "320x470",
                    "lg": "en-US",
                    "to": "iotest5ee391701234"
                },
                "ti": {
                    "vr": "$event0Version",
                    "cv": "2020070800",
                    "dm": true,
                    "od": 0,
                    "tt": $event0TIOLConfigTTL
                },
                "ci": {
                    "ap": "de.infonline.lib.core.test",
                    "av": "0",
                    "ac": 0,
                    "cy": 0,
                    "cs": "n"
                },
                "md": {
                    "st": {
                        "pi": 1,
                        "vi": 1,
                        "ch": 1,
                        "cd": 1,
                        "cw": 1,
                        "cm": 1,
                        "nk": 0,
                        "sn": 1
                    },
                    "cp": {
                        "pi": 1,
                        "vi": 1,
                        "ch": 1,
                        "cd": 1,
                        "cw": 1,
                        "cm": 1
                    }
                }
            },
            {
                "sv": "0.0.1",
                "si": {
                    "cn": "US",
                    "st": "iamtest",
                    "cp": "category",
                    "pt": "ap",
                    "ev": "view.appeared",
                    "co": "#1"
                },
                "di": {
                    "dp": "unknown,robolectric,robolectric,Android,robolectric,robolectric",
                    "dd": 160,
                    "dy": "smartphone",
                    "dx": "",
                    "dn": "offline",
                    "lt": $event1Timestamp,
                    "pn": "android",
                    "pv": "8.1.0",
                    "tz": -60,
                    "xy": "320x470",
                    "lg": "en-US",
                    "to": "iotest5ee391701234"
                },
                "ti": {
                    "vr": "$event1Version",
                    "cv": "2020070800",
                    "dm": true,
                    "od": 0,
                    "tt": $event1TIOLConfigTTL
                },
                "ci": {
                    "ap": "de.infonline.lib.core.test",
                    "av": "0",
                    "ac": 0,
                    "cy": 0,
                    "cs": "r"
                },
                "md": {
                    "st": {
                        "pi": 1,
                        "vi": 0,
                        "ch": 0,
                        "cd": 0,
                        "cw": 0,
                        "cm": 0,
                        "nk": 0,
                        "sn": 2,
                        "vt": $event1LastTransactionViewTime
                    },
                    "cp": {
                        "pi": 1,
                        "vi": 0,
                        "ch": 0,
                        "cd": 0,
                        "cw": 0,
                        "cm": 0
                    }
                }
            }
        ]
        """.toFormattedJson()*/
    }

    companion object {
        val RELAY_DEFAULT_OK_RESPONSE = """
            {
                "status": {
                    "no": 200,
                    "shortmsg": "OK",
                    "msg": "OK"
                }
            }
        """.trimIndent()

        // Just test keys
        const val PUBLIC_KEY_RSA = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4SLJ+Ac0ICgOrMyM715V" +
                "1YHXeMmf3lbmOmW9I5e9mNxKl7MKGDciifGr7EBpFJawdLiRfahHw+YKlirz70KF" +
                "WI2vUoi00eMyZxo3sGUVgeSE3kNB9FzR4/mE835ZnQrM1LVNNl4/ZRBNxzBm4sOt" +
                "9M3vmbZkIhL1fq5SS8U++liSzv5LYw2ISYSl7+asf3uXnIqAC4FNvoeYMG5spJzM" +
                "WtQ76TiUu2J5skqxyNhP4/5cliuVO9V5GDzVaxeDaVb2tptrQZ0gjRlhloy6yTO0" +
                "NOOgCVts+ODTUwDNoUxQrCcIHpR4Idp0rQz4bNMHF3/relLxIcfBWAFLXxPtVZyl" +
                "+QIDAQAB"

        val RELAY_CONFIG_THAT_INSERTS_OUR_RSA_KEY = """
        {
            "formatVersion" : "1.0.0",
            "configuration" : {
                "configVersion" : "2020070800",
                "publicRSA" : "$PUBLIC_KEY_RSA",
                "maxBulkEvents" : 2
            }
        }
        """.trimIndent()
        const val PRIVATE_KEY_RSA = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDhIsn4BzQgKA6s" +
                "zIzvXlXVgdd4yZ/eVuY6Zb0jl72Y3EqXswoYNyKJ8avsQGkUlrB0uJF9qEfD5gqW" +
                "KvPvQoVYja9SiLTR4zJnGjewZRWB5ITeQ0H0XNHj+YTzflmdCszUtU02Xj9lEE3H" +
                "MGbiw630ze+ZtmQiEvV+rlJLxT76WJLO/ktjDYhJhKXv5qx/e5ecioALgU2+h5gw" +
                "bmyknMxa1DvpOJS7YnmySrHI2E/j/lyWK5U71XkYPNVrF4NpVva2m2tBnSCNGWGW" +
                "jLrJM7Q046AJW2z44NNTAM2hTFCsJwgelHgh2nStDPhs0wcXf+t6UvEhx8FYAUtf" +
                "E+1VnKX5AgMBAAECggEBALhj8d8+pyafKEXG0rdKICraSUwYduN3cODratm38gU8" +
                "h6tvbBkhLxyj3xeEOYwvTx6J/D9akEyWIJ2VWGzhoq1AfhOu+8nbtvBSvMGwSMk2" +
                "DMytcVtemlmJh6aWGXdR3SlpxG9/CZUQoWM+9UVM1zDlahQPGjv+Iys6QxTj3AzU" +
                "7NyChXjx/6AWDXVa9iktfra+ooVm85I7J0x/+kfZUmZtniD1G+npGyHPTrejBYKM" +
                "OVEnKvPPuCpzHTrUd7W+U1CEQdDwm+bCcZjn9cHqfu4eTlH5xg3hElo9sM69yPQl" +
                "yMqLMHXx5kOc7ooPXLY+0E00Z172JsIZUE4K5w5MJAECgYEA+2pnyAHJNKeJ40T2" +
                "oNlI50X7x7Qop3KwV6aDSnZ52OU3y6tHiSZY5gIt33FV938lw5US1Lq2uwOWAPPm" +
                "HFNBZzMFBxripuVvCtbALkD1HYy8gkdVrQeKBiHQbrBXwLmBU3Huc06ONXsaRi5C" +
                "+0rj1C9NEiBUxeI55feDwUM5aCECgYEA5T22CeqgfK1rAS/xiS2StL5tey3FATaw" +
                "prM0m0P2hy1YgEYXXh2lYJWtwBb7I1noDRCV0rWIOMHb7Igzsu11a2zPoaa1EoGd" +
                "IrWHJtJNRiqPaRMBdQvhQ69xctFerCKTZ/GEo1p4H7quBr49JDXwP5qD5NPKI4ph" +
                "1eNcQdo/ItkCgYAMXjesxym2xWcrHwFi+E110yOHt30of6PdK/vZdeqYmO4dvtdO" +
                "D+zfo8vN6i1od4DMFlFO1cCXgp2mflkbm5zkDsZ4iwILY84KonXh0KA+S+YaIpg8" +
                "YtqPYqp2R4aJnJaYHEq2sW6dgujP+waghhafIZSRB7Yj5fUjMwHwR/ZTAQKBgGxO" +
                "GppWypn9CMgX0aLA8EYKjgkbV686Gn23vwv4MRoud1irRNEHaqHWNa/Ca8aI7JR3" +
                "mIFjmMdP98qpkttfSxGE6Bf0fioPKKFEaGUUCMQ0yWqYOyEEitoCorPfbT4gW8pJ" +
                "FiRjUsuS1DOFX7ei8C3sn5HV6fOepph6AZVvOlMJAoGBAM/AVgTSdosetuVSX4en" +
                "28YMMbLMoP7l/QV5cuePYw4TnPt3cOUme1qsd6VEN3yWD/fDfxLRMFzCliU2zEVf" +
                "neQLwP9i0LwitxC+H2s7LiVrgYwzit8xOJbDgyvPWsqMt5JsVYJ/k7tj8bvXjCLy" +
                "U8Q1PaGf+C7GDTvBgxPo1NOd"

    }

}