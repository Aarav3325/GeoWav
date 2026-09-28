package com.aarav.geowav.data.repository

import com.aarav.geowav.data.model.DestinationLocation
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.SessionMode
import com.aarav.geowav.data.model.SessionStatus
import com.aarav.geowav.data.model.SharingSession
import com.aarav.geowav.data.model.canTransitionTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SharingSessionTest {

    @Test
    fun `default SharingSession initialization has correct defaults`() {
        val session = SharingSession()
        assertEquals(SessionMode.NORMAL, session.mode)
        assertEquals(SessionStatus.ACTIVE, session.status)
        assertNull(session.expiresAt)
        assertNull(session.destinationPlaceId)
        assertNull(session.destinationLocation)
        assertNull(session.createdFrom)
        assertTrue(session.active)
    }

    @Test
    fun `default SessionHistory maintains backward compatibility with v1 0`() {
        val history = SessionHistory(
            id = "test_123",
            userId = "user_1",
            userName = "Test User"
        )
        assertEquals(SessionMode.NORMAL, history.mode)
        assertEquals(SessionStatus.COMPLETED, history.status)
        assertNull(history.expiresAt)
        assertNull(history.destinationPlaceId)
        assertNull(history.destinationLocation)
        assertNull(history.createdFrom)
    }

    @Test
    fun `SessionStatus state machine validates active transitions correctly`() {
        // ACTIVE -> PAUSED, COMPLETED, EXPIRED, CANCELLED should be valid
        assertTrue(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.PAUSED))
        assertTrue(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.COMPLETED))
        assertTrue(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.EXPIRED))
        assertTrue(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.CANCELLED))
        assertTrue(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.ACTIVE)) // Idempotent
    }

    @Test
    fun `SessionStatus state machine validates paused transitions correctly`() {
        // PAUSED -> ACTIVE, COMPLETED, EXPIRED, CANCELLED should be valid
        assertTrue(SessionStatus.PAUSED.canTransitionTo(SessionStatus.ACTIVE))
        assertTrue(SessionStatus.PAUSED.canTransitionTo(SessionStatus.COMPLETED))
        assertTrue(SessionStatus.PAUSED.canTransitionTo(SessionStatus.EXPIRED))
        assertTrue(SessionStatus.PAUSED.canTransitionTo(SessionStatus.CANCELLED))
        assertTrue(SessionStatus.PAUSED.canTransitionTo(SessionStatus.PAUSED)) // Idempotent
    }

    @Test
    fun `SessionStatus terminal states cannot transition to non-terminal states`() {
        // COMPLETED, EXPIRED, CANCELLED are terminal
        assertTrue(SessionStatus.COMPLETED.isTerminal())
        assertTrue(SessionStatus.EXPIRED.isTerminal())
        assertTrue(SessionStatus.CANCELLED.isTerminal())
        assertFalse(SessionStatus.ACTIVE.isTerminal())
        assertFalse(SessionStatus.PAUSED.isTerminal())

        // Terminal states cannot transition to ACTIVE or PAUSED
        assertFalse(SessionStatus.COMPLETED.canTransitionTo(SessionStatus.ACTIVE))
        assertFalse(SessionStatus.EXPIRED.canTransitionTo(SessionStatus.ACTIVE))
        assertFalse(SessionStatus.CANCELLED.canTransitionTo(SessionStatus.ACTIVE))
        assertFalse(SessionStatus.COMPLETED.canTransitionTo(SessionStatus.PAUSED))
    }

    @Test
    fun `DestinationLocation creation and properties`() {
        val dest = DestinationLocation(
            latitude = 19.0760,
            longitude = 72.8777,
            name = "College",
            address = "Mumbai, MH"
        )
        assertEquals(19.0760, dest.latitude, 0.0001)
        assertEquals(72.8777, dest.longitude, 0.0001)
        assertEquals("College", dest.name)
        assertEquals("Mumbai, MH", dest.address)
    }

    @Test
    fun `SharingSession mode enum values support all required v1 1 modes`() {
        val modes = SessionMode.values()
        assertTrue(modes.contains(SessionMode.NORMAL))
        assertTrue(modes.contains(SessionMode.JOURNEY))
        assertTrue(modes.contains(SessionMode.CHECK_IN))
        assertTrue(modes.contains(SessionMode.SAFETY))
        assertTrue(modes.contains(SessionMode.TEMPORARY_CIRCLE))
    }
}
