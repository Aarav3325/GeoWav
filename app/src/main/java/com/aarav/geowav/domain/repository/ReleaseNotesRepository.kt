package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.ReleaseNote

interface ReleaseNotesRepository {
    suspend fun getReleaseNotes(): List<ReleaseNote>
}
