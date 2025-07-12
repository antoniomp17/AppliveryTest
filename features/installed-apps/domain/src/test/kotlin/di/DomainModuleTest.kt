package di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import org.junit.Test
import repositories.InstalledAppsRepository

class DomainModuleTest {
    
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `verify domainModule configuration`() {
        domainModule.verify(
            extraTypes = listOf(
                InstalledAppsRepository::class
            )
        )
    }
}