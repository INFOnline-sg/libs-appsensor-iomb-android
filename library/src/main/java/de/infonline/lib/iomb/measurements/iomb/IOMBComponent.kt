package de.infonline.lib.iomb.measurements.iomb

import dagger.BindsInstance
import dagger.Subcomponent
import de.infonline.lib.iomb.measurements.common.CommonModule
import de.infonline.lib.iomb.util.PerMeasurement

@PerMeasurement
@Subcomponent(modules = [IOMBModule::class, CommonModule::class])
internal interface IOMBComponent {

    val measurement: IOMBMeasurement

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance setup: IOMBSetup,
            @BindsInstance localConfig: IOMBConfig?
        ): IOMBComponent
    }

}