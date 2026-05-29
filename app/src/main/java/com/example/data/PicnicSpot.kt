package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picnic_spots")
data class PicnicSpot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String,              // e.g. "Soon Valley, Khushab, Punjab"
    val description: String,
    val region: String,                // "Punjab", "KPK", "Sindh", "Balochistan", "Kashmir", "Gilgit-Baltistan", "Global"
    val category: String,              // "Lake", "Meadow", "Waterfall", "Beach", "Forest", "Mountain/Valley"
    val isHiddenGem: Boolean = true,
    val isBookmarked: Boolean = false,
    val isVisited: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val customNotes: String? = null,
    val rating: Float = 4.5f,
    val bestTimeToVisit: String = "All year round",
    val travelTips: String = "Ensure you carry sufficient drinking water and clean up local waste.",
    val isCustomUserSpot: Boolean = false,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val itinerary: String? = null
)
