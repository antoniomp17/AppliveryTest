package datasources.network

import android.net.ConnectivityManager
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import entities.info.ConnectionType
import entities.info.NetworkInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import mappers.NetworkMapper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
class NetworkDataSourceTest {
    
    private val context = RuntimeEnvironment.getApplication()
    private val connectivityManager: ConnectivityManager = mockk()
    private val networkMapper: NetworkMapper = mockk()
    
    private lateinit var dataSource: NetworkDataSource
    
    @Before
    fun setup() {
        dataSource = NetworkDataSourceImpl(context, connectivityManager, networkMapper)
    }
    
    @Test
    fun `should return network info when permission granted`() = runTest {
        val shadowApp = shadowOf(context)
        shadowApp.grantPermissions(android.Manifest.permission.ACCESS_NETWORK_STATE)
        
        val expectedNetworkInfo = NetworkInfo(ConnectionType.WIFI, true, 90)
        every { connectivityManager.activeNetwork } returns mockk()
        every { networkMapper.mapNetworkInfo(any(), connectivityManager) } returns expectedNetworkInfo
        
        val result = dataSource.getNetworkInfo()
        
        assertThat(result).isEqualTo(expectedNetworkInfo)
    }
    
    @Test
    fun `should return fallback when permission denied`() = runTest {
        val shadowApp = shadowOf(context)
        shadowApp.denyPermissions(android.Manifest.permission.ACCESS_NETWORK_STATE)
        
        val result = dataSource.getNetworkInfo()
        
        assertThat(result.connectionType).isEqualTo(ConnectionType.NONE)
        assertThat(result.isConnected).isFalse()
        assertThat(result.signalStrength).isNull()
    }
    
    @Test
    fun `should emit fallback when permission denied`() = runTest {
        val shadowApp = shadowOf(context)
        shadowApp.denyPermissions(android.Manifest.permission.ACCESS_NETWORK_STATE)

        dataSource.observeNetworkChanges().test {
            val emission = awaitItem()
            assertThat(emission.connectionType).isEqualTo(ConnectionType.NONE)
            assertThat(emission.isConnected).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `should handle security exception gracefully`() = runTest {

        val shadowApp = shadowOf(context)
        shadowApp.denyPermissions(android.Manifest.permission.ACCESS_NETWORK_STATE)

        val result = dataSource.getNetworkInfo()

        assertThat(result.connectionType).isEqualTo(ConnectionType.NONE)
        assertThat(result.isConnected).isFalse()
        assertThat(result.signalStrength).isNull()
    }
}