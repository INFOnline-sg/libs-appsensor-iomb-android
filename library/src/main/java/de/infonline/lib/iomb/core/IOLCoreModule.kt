package de.infonline.lib.iomb.core

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.util.LifecycleOwnerForProcess
import de.infonline.lib.iomb.util.rx.SchedulersCustom
import de.infonline.lib.iomb.util.serialization.HashingTypeAdapter
import de.infonline.lib.iomb.util.serialization.InstantAdapter
import de.infonline.lib.iomb.util.serialization.UUIDAdapter
import io.reactivex.rxjava3.core.Scheduler
import java.io.File
import java.security.SecureRandom
import javax.inject.Singleton

@Module
class IOLCoreModule {

    @Provides
    @Singleton
    @IOLibCoreScheduler
    internal fun provideCoreScheduler(): Scheduler = SchedulersCustom.customScheduler(2, "IOL:Core")

    @Provides
    @Singleton
    @IOLibDataPath
    internal fun provideDataPath(context: Context): File = File(context.filesDir, "infonline")

    @Provides
    @Singleton
    internal fun moshi(): Moshi = Moshi.Builder()
        .add(InstantAdapter)
        .add(Measurement.Setup.MOSHI_FACTORY)
//        .add(LegacyLocalConfiguration.MOSHI_FACTORY)
        .add(HashingTypeAdapter)
        .add(UUIDAdapter)
        .build()

    @Provides
    @Singleton
    @LifecycleOwnerForProcess
    internal fun processLifeCycleOwner(): LifecycleOwner = ProcessLifecycleOwner.get()

    @Provides
    @Singleton
    internal fun secureRandom(): SecureRandom = SecureRandom()
}