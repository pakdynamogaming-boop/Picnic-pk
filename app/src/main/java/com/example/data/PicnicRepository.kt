package com.example.data

import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

class PicnicRepository(private val picnicDao: PicnicDao) {
    val allSpots: Flow<List<PicnicSpot>> = picnicDao.getAllSpots()

    suspend fun insert(spot: PicnicSpot): Long {
        return picnicDao.insertSpot(spot)
    }

    suspend fun update(spot: PicnicSpot) {
        picnicDao.updateSpot(spot)
    }

    suspend fun deleteById(id: Int) {
        picnicDao.deleteSpotById(id)
    }

    suspend fun clear() {
        picnicDao.clearAll()
    }

    suspend fun researchPicnicSpotWithGemini(userQuery: String): String {
        val prompt = """
            The user is researching or searching for a picnic spot.
            Query: "$userQuery"
            
            As an expert travel guide specializing in hidden, scenic, and peaceful picnic spots (especially in Pakistan like Punjab, Sindh, KPK, Balochistan, AJ&K, Gilgit-Baltistan, but also globally), provide a beautiful, structured travel guide.
            Include:
            1. 📍 Spot Name & Location (Region/Province and coordinates/nearest city)
            2. ⛰️ Vibe / Scenic Elements (lush meadows, lake shore, historic forest, remote waterfall, beach sands)
            3. 🚗 Best route & accessibility guidelines
            4. 🎒 Pack Checklist (essential picnic gear, water sources, fuel)
            5. 💡 Best time of year & critical advice (such as "keep it green, pack out your litter")
            
            Make sure the tone is incredibly warm, professional, and inspiring! Keep it under 150 words and look clean with emoji lists.
        """.trimIndent()
        
        return callGemini(prompt) ?: """
            📍 Soon Valley Uchali Lake (Khushab, Punjab)
            ⛰️ Vibe: Secluded brackish saltwater lake framed by the Salt Range hills. Migratory white birds in winter. Deep tranquil nature.
            🚗 Route: 3 hours from Sargodha, 4.5 hours from Lahore via motorways. Suitable for family sedans.
            🎒 Pack: Binoculars for birdwatching, sun cap, foldable chairs, ready-to-eat local food.
            💡 Guidance: Perfect for winter picnics. Do not litter to preserve the ecology of migratory birds.
        """.trimIndent()
    }

    suspend fun searchOnlineSpotsWithGemini(userQuery: String): String {
        val prompt = """
            The user wants to find, list, and research picnic/travel spots based on this keyword or location name: "$userQuery".
            Search the web, YouTube, and global travel databases to find matching spots.
            If the query refers to a specific place name (either in Pakistan or globally, e.g. "Moola Chotok", "Katora Lake", "Umbrella Waterfall", "Niagara Falls", "Shogran", "Babusar", "Pir Sohawa"), the very first object in the returned JSON array MUST be that exact place with authentic, complete real-world details (like its correct province, coordinates, description, best time, and travel advice). The remaining items should be related scenic spots nearby or of a matching travel category.
            If the query is a category or region in Urdu or English (like "دریا", "rivers", "waterfalls", "آبشار", "swat", "bagh", "باغ"), make sure you return the most popular and scenic spots of that exact category in Pakistan.
            
            Each item in the JSON array MUST be a valid JSON object matching this exact schema:
            {
              "name": "Spot Name in English (and Urdu in parenthesis if applicable, e.g. Shalimar Gardens (شالیمار باغ))",
              "location": "District/City, Province/State (e.g. Lahore, Punjab)",
              "description": "Engaging visual description describing the beautiful trees, cascading waters, and atmosphere in Urdu and English (roughly 2 sentences)",
              "region": "Must be one of: Punjab, Sindh, KPK, Balochistan, Kashmir, Gilgit-Baltistan, Global",
              "category": "Must be one of: Lake, Meadow, Waterfall, Beach, Forest, Mountain/Valley, Historic",
              "isHiddenGem": true/false (set to true if lesser-known/secluded hidden spots),
              "rating": 4.5,
              "bestTimeToVisit": "Best months or seasons (e.g. September to April)",
              "travelTips": "Essential travel tips, route suitability, or cleanliness rules in Urdu and English",
              "imageUrl": "Choose a highly specific, beautiful landscape Unsplash image URL or use a relevant premium dynamic travel photograph corresponding to the scenic spot type from Unsplash (e.g. https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=800&q=80 for nature peaks, https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80 for lakes, https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?auto=format&fit=crop&w=800&q=80 for clean waterfalls, https://images.unsplash.com/photo-1547036967-23d11aacaee0?auto=format&fit=crop&w=800&q=80 for forestry trails)",
              "videoUrl": "Provide a clean title for a simulated drone virtual flight of this location (e.g. 'Epic Kaghan Drone Tour 4K' or 'Shalimar Fountains Cinematic virtual flight')",
              "itinerary": "Provide an elegant 3-step structured trip plan itinerary for a great day (e.g. '1. Morning: Arrival & Breakfast under pine shade. 2. Afternoon: Boating and walking around shore. 3. Evening: High tea and historic sunset walk.')"
            }
            
            Return ONLY the valid raw JSON array. Do NOT wrap it in any ```json or ``` markdown blocks. Ensure the response strictly follows the JSON array format starting with [ and ending with ]. Do not write any conversational intro or outro.
        """.trimIndent()
        
        return callGemini(prompt) ?: "[]"
    }

    private suspend fun callGemini(promptText: String): String? {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return null
        }
        
        return try {
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = promptText))
                    )
                )
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text.isNullOrBlank()) null else text.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
