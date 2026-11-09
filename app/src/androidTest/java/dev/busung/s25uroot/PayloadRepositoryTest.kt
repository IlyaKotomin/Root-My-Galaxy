package dev.busung.s25uroot

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayloadRepositoryTest {
    @Test
    fun bundledKitPreparesWithoutNetworkAndRepairsCorruptCache() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = PayloadRepository(context)
        val snapshot = DeviceSnapshot.current()
        val profile = repository.resolveTarget(snapshot)
        assertTrue(profile.matches(snapshot))

        val payloads = repository.download(profile) { }
        assertEquals(profile.exploit.size, payloads.exploit.length())
        assertEquals(profile.kernelSu.size, payloads.kernelSu.length())
        assertTrue(payloads.exploit.canRead())
        assertTrue(payloads.kernelSu.canRead())

        val expected = payloads.exploit.readBytes()
        android.system.Os.chmod(payloads.exploit.absolutePath, 0b110000000)
        // Same-size damage must be detected by SHA-256, not just a length check.
        val damaged = expected.copyOf()
        damaged[0] = (damaged[0].toInt() xor 1).toByte()
        payloads.exploit.writeBytes(damaged)
        val repaired = repository.download(profile) { }
        org.junit.Assert.assertArrayEquals(expected, repaired.exploit.readBytes())
    }
}
