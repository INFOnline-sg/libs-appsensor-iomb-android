package testhelper

import timber.log.Timber


abstract class KotlinBaseTest {

    init {
        Timber.uprootAll()
        Timber.plant(JUnitTree())
    }

    companion object {
        const val TEST_DIR = "build/tmp/testing"
    }
}
