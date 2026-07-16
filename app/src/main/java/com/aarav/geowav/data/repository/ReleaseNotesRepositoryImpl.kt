package com.aarav.geowav.data.repository

import android.content.Context
import com.aarav.geowav.data.model.ReleaseNote
import com.aarav.geowav.domain.repository.ReleaseNotesRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReleaseNotesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) : ReleaseNotesRepository {

    private val gson = Gson()
    private val defaultNotesJson = """
    [
      {
        "versionCode": 39,
        "versionName": "0.9.9",
        "releaseDate": "July 2026",
        "title": "Safety & Awareness Upgrades",
        "summary": "We've added powerful, state-of-the-art tools to keep you informed about your surroundings and share updates with your inner circle.",
        "features": [
          {
            "title": "Live Journey Replay",
            "description": "Replay movements and visualize your tracking history in a gorgeous, intuitive timeline map.",
            "icon": "timeline"
          },
          {
            "title": "Subtle Privacy Controls",
            "description": "Grant or revoke location sharing with loved ones individually and at any time.",
            "icon": "lock"
          },
          {
            "title": "State-of-the-Art UX",
            "description": "Experience smooth animations, clean typography, and a brand new dark mode.",
            "icon": "eye"
          }
        ]
      }
    ]
    """.trimIndent()

    override suspend fun getReleaseNotes(): List<ReleaseNote> = withContext(Dispatchers.IO) {
        val type = object : TypeToken<List<ReleaseNote>>() {}.type
        
        // 1. Try Remote Config
        val remoteJson = remoteConfig.getString("release_notes_json")
        if (remoteJson.isNotBlank()) {
            try {
                val parsed: List<ReleaseNote>? = gson.fromJson(remoteJson, type)
                if (parsed != null) {
                    return@withContext parsed
                }
            } catch (e: Exception) {
                // log and fallback
            }
        }
        
        // 2. Try Assets Fallback
        try {
            val assetJson = context.assets.open("release_notes.json").bufferedReader().use { it.readText() }
            if (assetJson.isNotBlank()) {
                val parsed: List<ReleaseNote>? = gson.fromJson(assetJson, type)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
        } catch (e: Exception) {
            // log and fallback
        }
        
        // 3. Hardcoded Fallback
        val fallback: List<ReleaseNote>? = gson.fromJson(defaultNotesJson, type)
        return@withContext fallback ?: emptyList()
    }
}
