package mmap.data.di

import mmap.core.IgnoreCoverage
import mmap.data.auth.SessionsDataSource
import mmap.data.auth.UsersDataSource
import mmap.data.maps.MapsDataSource
import mmap.data.maps.NodesDataSource
import mmap.data.migrate.MigrateDataSource
import mmap.data.tests.opexams.OpexamsDataSource
import mmap.data.tests.yandex.Yandex300DataSource
import mmap.plugins.OPEXAMS_CLIENT_QUALIFIER
import mmap.plugins.YANDEX_300_CLIENT_QUALIFIER
import org.koin.dsl.module

@IgnoreCoverage
val dataModule = module {
    single { SessionsDataSource() }
    single { UsersDataSource() }
    single { NodesDataSource() }
    single { MapsDataSource() }
    single { OpexamsDataSource(client = get(OPEXAMS_CLIENT_QUALIFIER)) }
    single { Yandex300DataSource(client = get(YANDEX_300_CLIENT_QUALIFIER)) }
    single { MigrateDataSource() }
}
