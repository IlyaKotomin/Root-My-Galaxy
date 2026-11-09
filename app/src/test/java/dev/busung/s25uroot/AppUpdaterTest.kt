package dev.busung.s25uroot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdaterTest {
    @Test fun onlyNewerVersionsAreOffered() {
        assertFalse(AppUpdater.isUpdateAvailable("0.1.0", "0.1.0"))
        assertFalse(AppUpdater.isUpdateAvailable("0.1.0", "0.2.0"))
        assertTrue(AppUpdater.isUpdateAvailable("0.10.0", "0.9.0"))
        assertFalse(AppUpdater.isUpdateAvailable("v0.1", "0.1.0"))
    }
}
