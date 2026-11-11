package com.iturrizaga.fairplay.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class AppEnvironmentTest {
    @Test fun fromName_isCaseInsensitive() {
        assertEquals(AppEnvironment.DEV, AppEnvironment.fromName("dev"))
        assertEquals(AppEnvironment.PROD, AppEnvironment.fromName("PROD"))
    }

    @Test fun fromName_unknownFallsBackToLocal() {
        assertEquals(AppEnvironment.LOCAL, AppEnvironment.fromName("staging"))
    }

    @Test fun remoteEnvironmentsUseHttps() {
        listOf(AppEnvironment.DEV, AppEnvironment.PROD).forEach {
            assertEquals("https", it.baseUrl.substringBefore("://"))
        }
    }
}
