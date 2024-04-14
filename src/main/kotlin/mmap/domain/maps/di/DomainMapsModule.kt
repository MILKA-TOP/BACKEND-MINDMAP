package mmap.domain.maps.di

import mmap.core.IgnoreCoverage
import mmap.domain.maps.MapsRepository
import mmap.features.maps.MapsEditUpdateController
import org.koin.dsl.module

@IgnoreCoverage
val domainMapsModule = module {
    factory {
        MapsRepository(
            mapsDataSource = get(),
            migrateDataSource = get()
        )
    }
    factory {
        MapsEditUpdateController(
            mapsEditRepository = get(),
        )
    }
}
