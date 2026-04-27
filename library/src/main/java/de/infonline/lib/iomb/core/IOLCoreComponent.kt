package de.infonline.lib.iomb.core

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import de.infonline.lib.iomb.measurements.iomb.IOMBComponent
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        IOLCoreModule::class
    ]
)
internal interface IOLCoreComponent {

    val iolCore: IOLCore

    val iombComponentFactory: IOMBComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): IOLCoreComponent
    }

}