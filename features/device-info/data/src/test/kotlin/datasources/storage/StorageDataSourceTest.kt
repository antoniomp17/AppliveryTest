package datasources.storage

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StorageDataSourceTest {

    private lateinit var dataSource: StorageDataSource

    @Before
    fun setup() {
        dataSource = StorageDataSourceImpl()
    }

    @Test
    fun `should return storage info with valid values`() = runTest {
        // When
        val result = dataSource.getStorageInfo()

        assertThat(result.totalSpace).isAtLeast(0)
        assertThat(result.freeSpace).isAtLeast(0)
        assertThat(result.usedSpace).isAtLeast(0)
        assertThat(result.totalSpace).isEqualTo(result.freeSpace + result.usedSpace)

        if (result.totalSpace > 0) {
            assertThat(result.totalSpace).isGreaterThan(1_000_000L) // Al menos 1MB
            assertThat(result.freeSpace).isAtMost(result.totalSpace)
            assertThat(result.usedSpace).isAtMost(result.totalSpace)
        }
    }

    @Test
    fun `should calculate used percentage correctly`() = runTest {
        val result = dataSource.getStorageInfo()

        assertThat(result.usedPercentage).isAtLeast(0f)
        assertThat(result.usedPercentage).isAtMost(100f)
        assertThat(result.usedPercentage).isNotNaN()

        if (result.totalSpace > 0) {
            val expectedPercentage = (result.usedSpace.toFloat() / result.totalSpace.toFloat()) * 100
            assertThat(result.usedPercentage).isWithin(0.1f).of(expectedPercentage)
        }
    }

    @Test
    fun `should return consistent values on multiple calls`() = runTest {
        val firstResult = dataSource.getStorageInfo()

        val secondResult = dataSource.getStorageInfo()

        assertThat(secondResult.totalSpace).isEqualTo(firstResult.totalSpace)
        assertThat(secondResult.freeSpace).isEqualTo(firstResult.freeSpace)
        assertThat(secondResult.usedSpace).isEqualTo(firstResult.usedSpace)
    }

    @Test
    fun `should handle storage access gracefully`() = runTest {
        val result = dataSource.getStorageInfo()

        assertThat(result.totalSpace).isAtLeast(0)
        assertThat(result.freeSpace).isAtLeast(0)
        assertThat(result.usedSpace).isAtLeast(0)
        assertThat(result.usedPercentage).isNotNaN()
    }

    @Test
    fun `should return fallback values in testing environment`() = runTest {
        val result = dataSource.getStorageInfo()

        if (result.totalSpace == 64_000_000_000L) {
            assertThat(result.freeSpace).isEqualTo(32_000_000_000L)
            assertThat(result.usedSpace).isEqualTo(32_000_000_000L)
            assertThat(result.usedPercentage).isEqualTo(50f)
        }

        assertThat(result.usedPercentage).isAtLeast(0f)
        assertThat(result.usedPercentage).isAtMost(100f)
    }
}