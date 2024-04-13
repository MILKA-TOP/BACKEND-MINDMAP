package mmap.features.maps.di

import mmap.features.maps.MapsController
import mmap.features.maps.MapsEditUpdateController
import org.koin.dsl.module

val featureMapsModule = module {
    factory {
        MapsController(
            mapsRepository = get()
        )
    }
    factory {
        MapsEditUpdateController(
            mapsEditRepository = get()
        )
    }
}
