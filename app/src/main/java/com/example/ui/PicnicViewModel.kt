package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PicnicSpot
import com.example.data.PicnicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PicnicViewModel(private val repository: PicnicRepository) : ViewModel() {

    // Flows for database spots
    val allSpots: StateFlow<List<PicnicSpot>> = repository.allSpots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedRegion = MutableStateFlow("All")
    val selectedRegion = _selectedRegion.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Filtered lists combining search query, region, and category
    val filteredSpots: StateFlow<List<PicnicSpot>> = combine(
        allSpots, _searchQuery, _selectedRegion, _selectedCategory
    ) { spots, query, region, category ->
        var temp = spots
        if (query.isNotBlank()) {
            temp = temp.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.travelTips.contains(query, ignoreCase = true)
            }
        }
        if (region != "All") {
            temp = temp.filter { it.region.equals(region, ignoreCase = true) }
        }
        if (category != "All") {
            temp = temp.filter { it.category.equals(category, ignoreCase = true) }
        }
        temp
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Form inputs for custom spots addition
    private val _inputName = MutableStateFlow("")
    val inputName = _inputName.asStateFlow()

    private val _inputLocation = MutableStateFlow("")
    val inputLocation = _inputLocation.asStateFlow()

    private val _inputDescription = MutableStateFlow("")
    val inputDescription = _inputDescription.asStateFlow()

    private val _inputRegion = MutableStateFlow("Punjab")
    val inputRegion = _inputRegion.asStateFlow()

    private val _inputCategory = MutableStateFlow("Lake")
    val inputCategory = _inputCategory.asStateFlow()

    private val _inputIsHidden = MutableStateFlow(true)
    val inputIsHidden = _inputIsHidden.asStateFlow()

    private val _inputTips = MutableStateFlow("")
    val inputTips = _inputTips.asStateFlow()

    private val _inputBestTime = MutableStateFlow("")
    val inputBestTime = _inputBestTime.asStateFlow()

    private val _inputRating = MutableStateFlow(4.5f)
    val inputRating = _inputRating.asStateFlow()

    // Detail modal/sheet state
    private val _selectedSpotDetail = MutableStateFlow<PicnicSpot?>(null)
    val selectedSpotDetail = _selectedSpotDetail.asStateFlow()

    // Gemini Travel Research Engine States
    private val _researchQuery = MutableStateFlow("")
    val researchQuery = _researchQuery.asStateFlow()

    private val _researchResult = MutableStateFlow<String?>(null)
    val researchResult = _researchResult.asStateFlow()

    private val _isResearching = MutableStateFlow(false)
    val isResearching = _isResearching.asStateFlow()

    // Online Multi-Spot Search Discovery States (Urdu & English)
    private val _isResearchModeCatalog = MutableStateFlow(true)
    val isResearchModeCatalog = _isResearchModeCatalog.asStateFlow()

    private val _isSearchingCatalog = MutableStateFlow(false)
    val isSearchingCatalog = _isSearchingCatalog.asStateFlow()

    private val _onlineResearchSpots = MutableStateFlow<List<PicnicSpot>>(emptyList())
    val onlineResearchSpots = _onlineResearchSpots.asStateFlow()

    // AdMob Configurations & Simulated Earnings for Monetization Screen
    private val _admobAppId = MutableStateFlow("ca-app-pub-3940256099942544~3347511713")
    val admobAppId = _admobAppId.asStateFlow()

    private val _admobBannerId = MutableStateFlow("ca-app-pub-3940256099942544/6300978111")
    val admobBannerId = _admobBannerId.asStateFlow()

    private val _admobInterstitialId = MutableStateFlow("ca-app-pub-3940256099942544/1033173712")
    val admobInterstitialId = _admobInterstitialId.asStateFlow()

    private val _isAdminModeActive = MutableStateFlow(false)
    val isAdminModeActive = _isAdminModeActive.asStateFlow()

    fun toggleAdminMode(active: Boolean) {
        _isAdminModeActive.value = active
    }

    private val _adClicksCount = MutableStateFlow(0)
    val adClicksCount = _adClicksCount.asStateFlow()

    private val _adImpressionsCount = MutableStateFlow(12) // Start with some organic baseline impressions
    val adImpressionsCount = _adImpressionsCount.asStateFlow()

    private val _isTestMode = MutableStateFlow(true)
    val isTestMode = _isTestMode.asStateFlow()

    private val _customCpc = MutableStateFlow(0.45f)
    val customCpc = _customCpc.asStateFlow()

    fun recordAdImpression() {
        _adImpressionsCount.value += 1
    }

    fun recordAdClick() {
        _adClicksCount.value += 1
        _adImpressionsCount.value += 1
    }

    fun updateAdmobConfig(appId: String, bannerId: String, interstitialId: String, testMode: Boolean, cpcValue: Float) {
        _admobAppId.value = appId
        _admobBannerId.value = bannerId
        _admobInterstitialId.value = interstitialId
        _isTestMode.value = testMode
        _customCpc.value = cpcValue
    }

    init {
        // Pre-load static awesome spots if none found
        viewModelScope.launch {
            try {
                val spots = repository.allSpots.first()
                if (spots.isEmpty()) {
                    prepopulateSpots()
                    appendSportsSpots()
                } else {
                    val hasSports = spots.any { it.category == "Sports & Gaming" }
                    if (!hasSports) {
                        appendSportsSpots()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetAndReseedDatabase() {
        viewModelScope.launch {
            try {
                repository.clear()
                prepopulateSpots()
                appendSportsSpots()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllSpots() {
        viewModelScope.launch {
            try {
                repository.clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun appendSportsSpots() {
        val sportsSpots = listOf(
            PicnicSpot(
                name = "Gaddafi Stadium Cricket Ground (قذافی اسٹیڈیم کرکٹ گراؤنڈ)",
                location = "Ferozepur Road, Lahore, Punjab",
                description = "The historic heart of Pakistan cricket. A beautifully managed stadium with a green turf outfield and spectator enclosures where major international matches and PSL games take place.",
                region = "Punjab",
                category = "Sports & Gaming",
                isHiddenGem = false,
                rating = 4.8f,
                bestTimeToVisit = "October to April (Matches or practice sessions)",
                travelTips = "Check match schedules in advance. Clean security clearance is required. Bring a flag and enjoy the matches with local snacks.",
                imageUrl = "https://images.unsplash.com/photo-1540747737956-378724044453?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Gaddafi Stadium Lahore Historic Lights Tour",
                itinerary = "1. Afternoon: Walking around the outer sports complex and museum.\n2. Evening: Seating in VIP/general gallery under massive stadium lights.\n3. Night: Experiencing local live match energy."
            ),
            PicnicSpot(
                name = "National Snooker & Esports Hub (نیشنل سنوکر کلب اور گیمنگ لانج)",
                location = "Gishkori Street, Clifton, Karachi, Sindh",
                description = "A premier indoor sports center equipped with world-class professional snooker tables, high-end PCs supporting online multiplayer games, and table tennis systems.",
                region = "Sindh",
                category = "Sports & Gaming",
                isHiddenGem = true,
                rating = 4.7f,
                bestTimeToVisit = "Afternoon & Late Evening (All year round)",
                travelTips = "Advance booking is useful on weekends. Snooker cues are provided. Enjoy quality milk tea while playing.",
                imageUrl = "https://images.unsplash.com/photo-1611195974226-a6a9be9dd763?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Snooker Championship & Clifton Esports Tour",
                itinerary = "1. Evening: Quick warm-up table tennis matches.\n2. Night: Professional snooker frame and online PC gaming tournaments."
            ),
            PicnicSpot(
                name = "Shalimar Clay Tennis Courts (شالیمار کلے ٹینس کلب)",
                location = "Mughalpura near Shalimar, Lahore, Punjab",
                description = "High-quality clay courts surrounded by historic landscaping, popular for amateur tournaments, youth summer camps, and recreational games.",
                region = "Punjab",
                category = "Sports & Gaming",
                isHiddenGem = false,
                rating = 4.5f,
                bestTimeToVisit = "6 AM to 9 AM & 5 PM to 8 PM",
                travelTips = "Wear flat tennis shoes for clay courts. Rackets and balls can be rented from the gear counter.",
                imageUrl = "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Shalimar Junior Tennis Cup Highlights",
                itinerary = "1. Early Morning: Friendly singles play set.\n2. Evening: Doubles rallying and training with club coach."
            ),
            PicnicSpot(
                name = "Korangi Football Ground & Play Arena (کورنگی فٹبال گراؤنڈ)",
                location = "Sector 33, Korangi, Karachi, Sindh",
                description = "An active local astro-turf football stadium popular for local night tournament matches and youth physical training. Includes a modern kids' play area and running track.",
                region = "Sindh",
                category = "Sports & Gaming",
                isHiddenGem = false,
                rating = 4.6f,
                bestTimeToVisit = "4 PM to 10 PM",
                travelTips = "Best to join local friendly club matches in the evening. Keep hydrated.",
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Karachi Youth Grassroots Football Cup",
                itinerary = "1. Afternoon: Watching juniors practice basic football drills.\n2. Evening: Play matches under outdoor street sport lights."
            ),
            PicnicSpot(
                name = "National Hockey Stadium & Arena (ہاکی اسٹیڈیم اور پلے ایریا)",
                location = "Nishter Park, Gulberg III, Lahore, Punjab",
                description = "The world's largest field hockey stadium by seating capacity. Famous for historic victories, featuring artificial grass pitches and dedicated local clubs.",
                region = "Punjab",
                category = "Sports & Gaming",
                isHiddenGem = false,
                rating = 4.7f,
                bestTimeToVisit = "October to February",
                travelTips = "Entry passes are generally free during domestic cups. Take a tour of the historic trophy display room.",
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Lahore Field Hockey National League Drone View",
                itinerary = "1. Morning: Exploring the Nishter park sports complex.\n2. Afternoon: Watching national athletes train on the astro-turf pitch.\n3. Evening: Local club hockey practice match."
            ),
            PicnicSpot(
                name = "The Arena Online Gaming Zone & VR Café (دی ایرینا گیمنگ زون اور وی آر کیفے)",
                location = "Bahria Town Phase 4, Rawalpindi, Punjab",
                description = "A premier contemporary gaming zone with high-performance VR gaming stations, PS5 chambers, online LAN multiplayer support, and snooker tables.",
                region = "Punjab",
                category = "Sports & Gaming",
                isHiddenGem = true,
                rating = 4.9f,
                bestTimeToVisit = "All year round",
                travelTips = "Try out the immersive VR simulators with friends. Delicious local refreshments are available.",
                imageUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Rawalpindi Bahria Elite Gaming & Snooker Tour",
                itinerary = "1. 3 PM: Experience Virtual Reality survival and action gaming.\n2. 5 PM: Challenging friends in a local 5v5 FPS LAN tournament.\n3. 8 PM: Dinner at the in-house cafe followed by casual snooker frames."
            ),
            PicnicSpot(
                name = "Margalla Hills Youth Sports Complex (مارگلہ ہلز اسپورٹس کمپلیکس)",
                location = "Sector H-8, Islamabad, Capital",
                description = "A beautifully situated sports fields zone in Islamabad near Sector H-8, offering public cricket pitches, Table Tennis centers, volleyball courts, and beautiful mountain vistas.",
                region = "Punjab",
                category = "Sports & Gaming",
                isHiddenGem = false,
                rating = 4.6f,
                bestTimeToVisit = "March to November",
                travelTips = "Public access is allowed; bring your tennis, table tennis, or cricket bats. Plenty of green spaces around for family relaxation.",
                imageUrl = "https://images.unsplash.com/photo-1540747737956-378724044453?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Islamabad Sector H8 Play Area & Sports Highlights",
                itinerary = "1. Morning: Jogging and table tennis matches at the activity gym.\n2. Afternoon: Family outdoor lunch near the picnic trees.\n3. Evening: Witnessing local cricket tape ball matches under the mountain air."
            )
        )
        sportsSpots.forEach { repository.insert(it) }
    }

    private suspend fun prepopulateSpots() {
        val seedSpots = listOf(
            PicnicSpot(
                name = "Shalimar Gardens (شالیمار باغ)",
                location = "GT Road, Lahore, Punjab",
                description = "A legendary Mughal garden complex completed in 1642 AD by Emperor Shah Jahan. Built as a gated paradise with 410 marble fountains, tiered cascades, and majestic stone terraces.",
                region = "Punjab",
                category = "Historic",
                isHiddenGem = false,
                rating = 4.8f,
                bestTimeToVisit = "October to March",
                travelTips = "Best visited in the cool afternoons. Carry a camera for historic carvings. Easily accessible via Lahore Metro Transit.",
                imageUrl = "https://images.unsplash.com/photo-1569003339405-ea396a5a8a90?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Shalimar Mughal Terraces Cinematic Flyover",
                itinerary = "1. Morning: Walk inside Shahi Hammam and corridors.\n2. Afternoon: Marvel at the 410 marble cascades and fountains.\n3. Evening: Historic garden walk on terraces."
            ),
            PicnicSpot(
                name = "Saif-ul-Muluk Lake (جھیل سیف الملوک)",
                location = "Naran, Kaghan Valley, KPK",
                description = "A breathtaking high-altitude lake nestled at 10,578 ft, reflecting the towering snow peak of Malika Parbat. Famed for its local fairy-tale legends and emerald-green glacial water.",
                region = "KPK",
                category = "Lake",
                isHiddenGem = false,
                rating = 4.9f,
                bestTimeToVisit = "June to September",
                travelTips = "Accessed via 4x4 Jeep from Naran Bazar. Bring warm sweaters and take a peaceful traditional boat ride across the water.",
                imageUrl = "https://images.unsplash.com/photo-1627581512404-58aa44f77c44?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Saif ul Muluk Legend Aerial Drone Tour",
                itinerary = "1. Morning: Jeep ride from Naran Bazar to lake side.\n2. Afternoon: Boat rowing and hot tea under Malika Parbat reflections.\n3. Evening: Listening to fairy tale stories at lake resort."
            ),
            PicnicSpot(
                name = "Bagh-e-Jinnah / Lawrence Gardens (باغِ جناح)",
                location = "Mall Road, Lahore, Punjab",
                description = "A historic Victorian-era public botanical garden. Features vast serene grass lawns, a massive variety of ancient trees, botanical glasshouses, and clean walking tracks.",
                region = "Punjab",
                category = "Historic",
                isHiddenGem = false,
                rating = 4.6f,
                bestTimeToVisit = "All year round",
                travelTips = "Perfect for a quiet family picnic in the morning. Has beautiful shaded areas under century-old banyan trees.",
                imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Bagh e Jinnah Botanical Walks Tour",
                itinerary = "1. Morning: Bird watching under century-old giant shade trees.\n2. Afternoon: Library visit and Victorian glasshouse walk.\n3. Evening: Relaxing picnic with family on carpeted lawns."
            ),
            PicnicSpot(
                name = "Neelum Valley Oasis (وادی نیلم)",
                location = "Muzaffarabad District, Azad Kashmir",
                description = "A celestial bow-shaped green valley lined by the roaring turquoise Neelum River, pine-scented hillsides, and sprawling alpine flowery meadows.",
                region = "Kashmir",
                category = "Mountain/Valley",
                isHiddenGem = false,
                rating = 4.9f,
                bestTimeToVisit = "April to September",
                travelTips = "Book riverside wood cabins in Kel or Sharda. Try local Kashmiri tea by the riverbank.",
                imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Azad Kashmir Neelum River Rapids Drone Tour",
                itinerary = "1. Morning: Wood cabin breakfast by the gushing Neelum river.\n2. Afternoon: Suspension bridge trial hike to Kel village.\n3. Evening: Hot Kashmiri pink tea with mountain views."
            ),
            PicnicSpot(
                name = "Soon Valley Dream (Uchali Lake - سون ویلی)",
                location = "Khushab District, Salt Range, Punjab",
                description = "A magnificent deep saltwater-lake oasis nestled at the heart of the Salt Range, surrounded by rolling green hills. Famous for migratory pink Siberian birds.",
                region = "Punjab",
                category = "Lake",
                isHiddenGem = true,
                rating = 4.7f,
                bestTimeToVisit = "November to March",
                travelTips = "Highly secluded. Bring binoculars for birdwatching and pack all your food supplies from Khushab.",
                imageUrl = "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Soon Valley Uchali Lake Birds Horizon",
                itinerary = "1. Morning: Scenic driving past Salt Range rock beds.\n2. Afternoon: Binoculars bird-spotting on lakeside banks.\n3. Evening: Panoramic sunset over serene Uchali peak hills."
            ),
            PicnicSpot(
                name = "Gorakh Hill Station (The Roof of Sindh - گورکھ)",
                location = "Kirthar Mountains, Dadu, Sindh",
                description = "Situated at 5,688 ft elevation in the Kirthar Range. Offers a uniquely cold, crisp breeze amidst hot plains, and majestic golden canyon sunsets.",
                region = "Sindh",
                category = "Mountain/Valley",
                isHiddenGem = false,
                rating = 4.6f,
                bestTimeToVisit = "September to February",
                travelTips = "Requires a rugged 4x4 Jeep ride from Wahi Pandhi. Pack warm winter jackets as night temperatures drop sharply.",
                imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Gorakh Hills Sunset Drone virtual play",
                itinerary = "1. Morning: High-altitude Jeep climb up the rocky Sindh canyons.\n2. Afternoon: Coffee on the balcony as clouds roll directly over you.\n3. Evening: Bonfire and sky gazing in cold mountain winds."
            ),
            PicnicSpot(
                name = "Astola Island Pristine Beach (جزیرہ آسٹولا)",
                location = "Arabian Sea, Coastal Pasni, Balochistan",
                description = "Pakistan's largest marine protected island. A secluded paradise of crystalline turquoise water, sandy coves, coral reefs, and dramatic limestone cliffs.",
                region = "Balochistan",
                category = "Beach",
                isHiddenGem = true,
                rating = 4.9f,
                bestTimeToVisit = "October to February",
                travelTips = "Requires a 3-hour speed boat ride from Pasni. Bring fresh water reserves, solid tents, and pack all garbage back out.",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Astola Marine Sanctuary Coral Reef Drone flyover",
                itinerary = "1. Morning: Boating from Pasni harbor past beautiful seagull tides.\n2. Afternoon: Snorkeling along coral reefs and exploring caves.\n3. Evening: Beach camp under clear Milky Way galaxy skies."
            ),
            PicnicSpot(
                name = "Kumrat Valley Pine Canopy (وادی کمراٹ)",
                location = "Upper Dir District, KPK",
                description = "A lush valley famed for its imposing towering deodar forest floor, winding crystal-clear Panjkora River meadows, and towering mountain walls.",
                region = "KPK",
                category = "Forest",
                isHiddenGem = true,
                rating = 4.8f,
                bestTimeToVisit = "June to September",
                travelTips = "Camping directly in the pine woods is recommended. Carry raincoats as mountain showers can begin suddenly.",
                imageUrl = "https://images.unsplash.com/photo-1547036967-23d11aacaee0?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Kumrat Forest Panjkora Stream Drone 4K",
                itinerary = "1. Morning: Forest stroll smelling deodar pine needles.\n2. Afternoon: Stepping along freezing Panjkora riverbank meadows.\n3. Evening: Cozy camp under imposing dark woodland shadows."
            ),
            PicnicSpot(
                name = "Hunza Valley Heaven (وادی ہنزہ)",
                location = "Karakoram Highway, Gilgit-Baltistan",
                description = "A world-famous valley framed by colossal snow peaks like Rakaposhi. Features lush apricot orchards, roaring rivers, and historical forts.",
                region = "Gilgit-Baltistan",
                category = "Mountain/Valley",
                isHiddenGem = false,
                rating = 4.9f,
                bestTimeToVisit = "April to October",
                travelTips = "Great local shops and cafes. Walk up Baltit Fort for spectacular 360-degree aerial views of the valley.",
                imageUrl = "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Hunza Valley Rakaposhi Peak aerial view",
                itinerary = "1. Morning: Walk inside the historic 800-year-old Baltit Fort.\n2. Afternoon: Traditional walnut cake by Karimabad local market.\n3. Evening: Witnessing golden sunset on the sharp peak of Rakaposhi."
            ),
            PicnicSpot(
                name = "Rawal Lake Promenades (راول جھیل)",
                location = "Margalla Hills Foothills, Islamabad",
                description = "An artificial water reservoir with elegant lakeside picnic lawns, boating yards, and flowering flowerbeds, offering peace inside the capital.",
                region = "Punjab",
                category = "Lake",
                isHiddenGem = false,
                rating = 4.5f,
                bestTimeToVisit = "September to April",
                travelTips = "Perfect for standard family weekend picnics. Includes a dedicated kids play area and motorboat rides.",
                imageUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Rawal Lake Sunset Boating cinematic virtual video",
                itinerary = "1. Morning: Family stroll along the well-manicured floral pathways.\n2. Afternoon: Renting a classic motorboat for a lap on the reservoir.\n3. Evening: Watching migratory cranes from bird-watching towers."
            ),
            PicnicSpot(
                name = "Kunhar River Rocky Streams (دریائے کنہار)",
                location = "Kaghan, Balakot, KPK",
                description = "A roaring glacier-fed river that flows alongside the Kaghan road. Famed for its freezing water splash, river-rafting, and trout fishing.",
                region = "KPK",
                category = "Mountain/Valley",
                isHiddenGem = false,
                rating = 4.7f,
                bestTimeToVisit = "May to September",
                travelTips = "Enjoy hot pakoras and fresh trout at riverside stalls. Keep a safe distance from strong river currents.",
                imageUrl = "https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Kunhar River Balakot Splashing Streams Virtual tour",
                itinerary = "1. Morning: Listen to the gushing roar of cold glacial water in Balakot.\n2. Afternoon: Savoring spicy hot Pakistani pakoras directly by river currents.\n3. Evening: Catching sunset views over rocky green cliffs."
            ),
            PicnicSpot(
                name = "Bagh-e-Ibn-e-Qasim Waterfront (باغ ابن قاسم)",
                location = "Clifton Beach Rd, Karachi, Sindh",
                description = "Pakistan's largest coastal public park spanning 130 acres. Rich with stone obelisks, green lawns, and beautiful evening views of Clifton Beach.",
                region = "Sindh",
                category = "Historic",
                isHiddenGem = false,
                rating = 4.4f,
                bestTimeToVisit = "Year round (Evening time)",
                travelTips = "Marvelous architecture is lit up at night. Ideal for cool oceanic sea breeze walks with family.",
                imageUrl = "https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Bagh Ibn e Qasim Clifton Beach virtual camera",
                itinerary = "1. Afternoon: Strolling past majestic stone obelisks and green canopy pathways.\n2. Evening: Oceanic sunset views over the Clifton horizon.\n3. Night: Beautiful lights illuminate Mughal architectural designs."
            ),
            PicnicSpot(
                name = "Sajikot Emerald Waterfall (سجی کوٹ آبشار)",
                location = "Havelian, Abbottabad, KPK",
                description = "A spectacular three-tiered cascading waterfall plunging into a deep emerald water pool, surrounded by high lime cliffs.",
                region = "KPK",
                category = "Waterfall",
                isHiddenGem = true,
                rating = 4.6f,
                bestTimeToVisit = "March to October",
                travelTips = "Sedans can reach the top. Path down is rocky and steep, wear trekking boots with good grip.",
                imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Sajikot Abbottabad Waterfall virtual flight",
                itinerary = "1. Morning: Driving up Havelian peak canyons with family.\n2. Afternoon: Safe photography by towering cliffs with three cascading cascades.\n3. Evening: Listening to peaceful water plunge in pristine air."
            ),
            PicnicSpot(
                name = "Umbrella Waterfall (امبریلا آبشار)",
                location = "Sajikot, Abbottabad, KPK",
                description = "Famed as one of the most unique umbrella-shaped waterfall cascades on Earth. Features a stunning overhanging rock canopy where emerald stream water rains down.",
                region = "KPK",
                category = "Waterfall",
                isHiddenGem = true,
                rating = 4.8f,
                bestTimeToVisit = "April to September",
                travelTips = "Wear high-traction hiking sleepers or boots. The path downstairs takes 35-45 minutes but the reward is unforgettable.",
                imageUrl = "https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Umbrella Waterfall Mysterious Umbrella Hollow Drone",
                itinerary = "1. Morning: Hike down the lush green steps near Poona village.\n2. Afternoon: Swim or stand beneath the soothing umbrella of pristine mountain water.\n3. Evening: Warm soup and pakoras at the canyon base stall."
            ),
            PicnicSpot(
                name = "Noori Waterfall (نوری آبشار)",
                location = "Tila Haripur, KPK",
                description = "A spectacular hidden cove cove-waterfall with clear turquoise waters. It cascades inside a unique limestone rock dome resembling a secret cavern.",
                region = "KPK",
                category = "Waterfall",
                isHiddenGem = true,
                rating = 4.7f,
                bestTimeToVisit = "All year round",
                travelTips = "Located near Haripur city. Very close and easily reachable for families. Safe swimming pools but always carry life-jackets for children.",
                imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Noori Waterfall Blue Cove Drone Slow Flight",
                itinerary = "1. Morning: Travel from Islamabad via high-speed motorway to Haripur.\n2. Afternoon: Safe walking through the peaceful limestone canyon stream.\n3. Evening: Relaxing beach picnic setup on dry white shale rocks."
            ),
            PicnicSpot(
                name = "Katora Lake Waterfall (کٹورہ جھیل)",
                location = "Jahaz Banda Meadows, Upper Dir, KPK",
                description = "An alpine glacial lake resembling a bowl (Katora) with a magnificent waterfall feeding directly from the melting glaciers of High Dir peaks.",
                region = "KPK",
                category = "Waterfall",
                isHiddenGem = true,
                rating = 4.9f,
                bestTimeToVisit = "July to September",
                travelTips = "Only accessible via trekking or sturdy horses from Jahaz Banda meadows. Bring high-density thermal tents and heavy coats.",
                imageUrl = "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Katora Glacial Bowl Lake High Range Flight",
                itinerary = "1. Morning: Stepping past massive snowy patches from Jahaz Banda camping.\n2. Afternoon: Spotting the turquoise glacial ripples and spectacular waterfall feed.\n3. Evening: Campfire setup near forest floor line."
            ),
            PicnicSpot(
                name = "Tatara Park Peshawar (تاتارا پارک)",
                location = "Hayatabad, Peshawar, KPK",
                description = "A beautifully designed modern theme park and picnic garden. Consists of a serene lake boating yard, floral lawns, and high views of Khyber hills.",
                region = "KPK",
                category = "Historic",
                isHiddenGem = false,
                rating = 4.5f,
                bestTimeToVisit = "All year round",
                travelTips = "Great security, high-end family swings, and clean paved paths. Excellent playground for kids.",
                imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Tatara Park Lake and Boating Scenic evening view",
                itinerary = "1. Afternoon: Take children to the colorful slides and mini flower zoo.\n2. Evening: Boating on the central tranquil pond past illuminated fountains.\n3. Night: Hot traditional Peshawar Charsi Tikka inside the park."
            ),
            PicnicSpot(
                name = "Ayubia National Park Pine Trails (ایوبیہ پارک)",
                location = "Galiyat District, Abbotabad, KPK",
                description = "A massive alpine pine reserve at 8,000 ft. Features towering wild deodar canopies, wooden footpaths, and active wild monkeys in deep forests.",
                region = "KPK",
                category = "Forest",
                isHiddenGem = false,
                rating = 4.8f,
                bestTimeToVisit = "May to October",
                travelTips = "Take the historic pipeline hiking track from Ayubia to Dunga Gali. Highly flat, shade-shaded and safe for grandparents.",
                imageUrl = "https://images.unsplash.com/photo-1547036967-23d11aacaee0?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Ayubia Pipeline Track Cold Forest Aerial View",
                itinerary = "1. Morning: Take the standard scenic Chairlift ride up to the peaks.\n2. Afternoon: Hiking along the historic Pipeline Track watching birds.\n3. Evening: Hot Kashmiri tea over pine-wood embers in local shacks."
            ),
            PicnicSpot(
                name = "Neela Sandh Waterfall (نیلا سندھ)",
                location = "Mori Syedan, Kahuta, Punjab",
                description = "A stunning series of natural turquoise freshwater pools and waterfalls sourced from the clean springs of Margalla Hills.",
                region = "Punjab",
                category = "Waterfall",
                isHiddenGem = true,
                rating = 4.5f,
                bestTimeToVisit = "April to September",
                travelTips = "Saves long travel—just 45 minutes from Islamabad! Carry life jackets as the springs are very deep and fresh.",
                imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Neela Sandh Turquoise Pools Drone Scan",
                itinerary = "1. Morning: Quick scenic mountain drive from Islamabad past pine farms.\n2. Afternoon: Swimming inside the cold natural turquoise pool water.\n3. Evening: Spicy hot BBQ with friends on dry pebble beaches."
            ),
            PicnicSpot(
                name = "Narh Waterfall (نرھ آبشار)",
                location = "Kahuta, Rawalpindi, Punjab",
                description = "A beautiful series of mountain water rapids crashing past rock arches and giant pine roots inside the quiet Narh hills.",
                region = "Punjab",
                category = "Waterfall",
                isHiddenGem = true,
                rating = 4.5f,
                bestTimeToVisit = "March to October",
                travelTips = "Very peaceful spot away from standard noisy crowds. Standard small family cars can reach easily via well-built mountain roads.",
                imageUrl = "https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Narh Hills Shaded Water Cascades virtual preview",
                itinerary = "1. Morning: Family road-trip through winding alpine curves.\n2. Afternoon: Setting picnic carpets under the cool mist of the fall.\n3. Evening: Cold drinks and snacks while children safely splash in shallow banks."
            ),
            PicnicSpot(
                name = "Changa Manga Forest (چھانگا مانگا)",
                location = "Kasur District, Punjab",
                description = "One of the world's largest man-made planted forests. Includes a giant quiet lake, historic miniature passenger steam trains, and wildlife reserves.",
                region = "Punjab",
                category = "Forest",
                isHiddenGem = false,
                rating = 4.4f,
                bestTimeToVisit = "October to April",
                travelTips = "Saves long mountain journeys. Perfect for standard day picnics from Lahore with kids who love historic steam trains.",
                imageUrl = "https://images.unsplash.com/photo-1547036967-23d11aacaee0?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Changa Manga Dense Canopy Steam Train Virtual Run",
                itinerary = "1. Morning: Ride the historic miniature train through thick green forests.\n2. Afternoon: Boating in the peaceful forest center basin.\n3. Evening: Strolling through the deer sanctuary and tree gardens."
            ),
            PicnicSpot(
                name = "Moola Chotok Water Oasis (مولہ چوٹوک)",
                location = "Khuzdar District, Balochistan",
                description = "A jaw-dropping paradise canyon nestled in the dry Balochistan mountains. Offers pristine deep-blue water pools, tropical ferns, and cool water rapids.",
                region = "Balochistan",
                category = "Waterfall",
                isHiddenGem = true,
                rating = 4.9f,
                bestTimeToVisit = "October to March",
                travelTips = "Requires a rugged 4x4 off-road Jeep ride from Khuzdar. Camping under starry desert skies is highly recommended.",
                imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Moola Chotok Balochistan Chasm Paradise Drone",
                itinerary = "1. Morning: Jeep safari past rugged mountains of Balochistan.\n2. Afternoon: Walking into the canyon to splash in emerald waterfalls.\n3. Evening: Warm soup and sky gazing under crystal clear stars."
            ),
            PicnicSpot(
                name = "Lauterbrunnen Valley Falls (آبشار سوئٹزرلینڈ)",
                location = "Bernese Oberland, Switzerland",
                description = "A world-famous glacial valley showcasing 72 plunging waterfalls. Lined by towering mountain cliffs, colorful chalets, and lush green meadows.",
                region = "Global",
                category = "Waterfall",
                isHiddenGem = false,
                rating = 4.9f,
                bestTimeToVisit = "May to October",
                travelTips = "Highly accessible via European scenic regional trains. Walk behind the famous Staubbach Waterfall.",
                imageUrl = "https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Lauterbrunnen Glacial Valley 72 Waterfalls Tour",
                itinerary = "1. Morning: Train excursion past imposing snowpeaks.\n2. Afternoon: Trekking along mountain flower lines under misty sprays.\n3. Evening: Cheese findue at a rustic village deck."
            ),
            PicnicSpot(
                name = "Banff Louise Alpine Lake (کینیڈا جھیل)",
                location = "Alberta Rockies, Canada",
                description = "The absolute jewel of Canadian alpine parks. A stunning turquoise lake feeding directly from the Victoria Glacier, surrounded by rugged pine peaks.",
                region = "Global",
                category = "Lake",
                isHiddenGem = false,
                rating = 4.9f,
                bestTimeToVisit = "June to September",
                travelTips = "Rent bright red wood canoes to row on the lake. Arrive early at 6 AM to secure park spaces with family.",
                imageUrl = "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Banff Lake Louise Mirror Reflections Drone",
                itinerary = "1. Morning: Canoeing in spectacular mirror-smooth emerald water.\n2. Afternoon: Hiking the high Teahouse Trail past wild valleys.\n3. Evening: Scenic photography of golden sun hitting glaciers."
            ),
            PicnicSpot(
                name = "Niagara Horseshoe Falls (نیاگرا آبشاریں)",
                location = "Ontario, Canada / NY, USA",
                description = "One of the most powerful and majestic waterfall structures on Earth. Moves 168,000 cubic meters of pristine water every single minute.",
                region = "Global",
                category = "Waterfall",
                isHiddenGem = false,
                rating = 4.8f,
                bestTimeToVisit = "June to August",
                travelTips = "Take the legendary 'Maid of the Mist' boat cruise directly into the roaring white spray.",
                imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Niagara Massive Horseshoe Falls Cinematic Overlook",
                itinerary = "1. Morning: Boarding the historic cruise in raincoat wraps.\n2. Afternoon: Walking the wooden cliff boards beside roaring water walls.\n3. Evening: Witnessing colorful searchlights painting the mist."
            ),
            PicnicSpot(
                name = "Blue Lagoon Thermal Oasis (بلو لیگون آئس لینڈ)",
                location = "Grindavik, Iceland",
                description = "A geological dream of hot, milky-blue mineral waters framed by dramatic black lava rock fields.",
                region = "Global",
                category = "Lake",
                isHiddenGem = false,
                rating = 4.8f,
                bestTimeToVisit = "September to April (for northern lights)",
                travelTips = "Book months in advance. Drink hot herbal tea while soaking in the steaming 38°C water.",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Blue Lagoon Aurora Borealis Magic Drone View",
                itinerary = "1. Afternoon: Enjoying warm mineral mud scrubs on your face.\n2. Evening: Soaking in natural heat amidst volcanic steam.\n3. Night: Watching the green Aurora dance directly across the clear sky."
            ),
            PicnicSpot(
                name = "Fuji Cherry Blossom Gardens (جاپان چیری بلاسم)",
                location = "Five Lakes, Shizuoka, Japan",
                description = "A magnificent lakeside park offering the iconic postcard view of Mt. Fuji framed by thousands of pink blooming Sakura trees.",
                region = "Global",
                category = "Historic",
                isHiddenGem = false,
                rating = 4.9f,
                bestTimeToVisit = "April (Sakura Season)",
                travelTips = "Perfect for traditional Japanese family picnic (Hanami). Pack bento lunch boxes and green tea.",
                imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=800&q=80",
                videoUrl = "Fuji Sakura Blossom Mirror Lake Mavic Flyover",
                itinerary = "1. Morning: Lake walk beneath raining pink flower petals.\n2. Afternoon: Relaxing picnic on carpeted grass under Fuji peaks.\n3. Evening: Traditional tea house ceremony over mirror lake waters."
            )
        )
        seedSpots.forEach { repository.insert(it) }
    }

    // Input handlers
    fun onNameChange(v: String) { _inputName.value = v }
    fun onLocationChange(v: String) { _inputLocation.value = v }
    fun onDescriptionChange(v: String) { _inputDescription.value = v }
    fun onRegionChange(v: String) { _inputRegion.value = v }
    fun onCategoryChange(v: String) { _inputCategory.value = v }
    fun onIsHiddenToggle() { _inputIsHidden.value = !_inputIsHidden.value }
    fun onTipsChange(v: String) { _inputTips.value = v }
    fun onBestTimeChange(v: String) { _inputBestTime.value = v }
    fun onRatingChange(v: Float) { _inputRating.value = v }

    fun onSearchQueryChange(v: String) { _searchQuery.value = v }
    fun onRegionFilterChange(v: String) { _selectedRegion.value = v }
    fun onCategoryFilterChange(v: String) { _selectedCategory.value = v }

    fun onResearchQueryChange(v: String) { _researchQuery.value = v }

    // Toggle spot Bookmarks / Visited stats
    fun toggleBookmark(spot: PicnicSpot) {
        viewModelScope.launch {
            val updated = spot.copy(isBookmarked = !spot.isBookmarked)
            repository.update(updated)
            if (_selectedSpotDetail.value?.id == spot.id) {
                _selectedSpotDetail.value = updated
            }
        }
    }

    fun toggleVisited(spot: PicnicSpot) {
        viewModelScope.launch {
            val updated = spot.copy(isVisited = !spot.isVisited)
            repository.update(updated)
            if (_selectedSpotDetail.value?.id == spot.id) {
                _selectedSpotDetail.value = updated
            }
        }
    }

    fun selectSpot(spot: PicnicSpot?) {
        _selectedSpotDetail.value = spot
    }

    // Insert user custom picnic spots
    fun addCustomUserSpot() {
        if (_inputName.value.isBlank() || _inputLocation.value.isBlank()) return
        
        viewModelScope.launch {
            val spot = PicnicSpot(
                name = _inputName.value,
                location = _inputLocation.value,
                description = _inputDescription.value.ifBlank { "A stunning, serene custom picnic spot discovered by you." },
                region = _inputRegion.value,
                category = _inputCategory.value,
                isHiddenGem = _inputIsHidden.value,
                rating = _inputRating.value,
                bestTimeToVisit = _inputBestTime.value.ifBlank { "All seasons" },
                travelTips = _inputTips.value.ifBlank { "A beautiful spot. Carry essential supplies and keep it clean." },
                isCustomUserSpot = true
            )
            repository.insert(spot)
            clearInputs()
        }
    }

    fun clearInputs() {
        _inputName.value = ""
        _inputLocation.value = ""
        _inputDescription.value = ""
        _inputRegion.value = "Punjab"
        _inputCategory.value = "Lake"
        _inputIsHidden.value = true
        _inputRating.value = 4.5f
        _inputBestTime.value = ""
        _inputTips.value = ""
    }

    fun deleteSpot(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (_selectedSpotDetail.value?.id == id) {
                _selectedSpotDetail.value = null
            }
        }
    }

    // Toggle between online search and detailed QA
    fun setResearchModeCatalog(isCatalog: Boolean) {
        _isResearchModeCatalog.value = isCatalog
    }

    // Call Gemini to research custom spots or tourist planning
    fun runGeminiResearch() {
        val query = _researchQuery.value
        if (query.isBlank()) return

        viewModelScope.launch {
            _isResearching.value = true
            _researchResult.value = null
            try {
                val response = repository.researchPicnicSpotWithGemini(query)
                _researchResult.value = response
            } catch (e: Exception) {
                _researchResult.value = "Error while researching with Gemini. Please try again."
            } finally {
                _isResearching.value = false
            }
        }
    }

    // Run dynamic Gemini Online Search to construct dynamic list of spots (Thousands search)
    fun searchOnlineCatalog() {
        val query = _researchQuery.value
        if (query.isBlank()) return

        viewModelScope.launch {
            _isSearchingCatalog.value = true
            _onlineResearchSpots.value = emptyList()
            try {
                val response = repository.searchOnlineSpotsWithGemini(query)
                val parsed = parseGeminiSpots(response)
                _onlineResearchSpots.value = parsed
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearchingCatalog.value = false
            }
        }
    }

    // Add online spot to offline Room Database
    fun saveOnlineSpotToDb(spot: PicnicSpot) {
        viewModelScope.launch {
            repository.insert(spot.copy(isCustomUserSpot = false))
        }
    }

    // High performance JSON list extractor utilizing built-in Android SDK classes (Zero overhead)
    private fun parseGeminiSpots(jsonString: String): List<PicnicSpot> {
        val list = mutableListOf<PicnicSpot>()
        try {
            var cleanJson = jsonString.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substringAfter("```json")
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substringAfter("```")
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substringBeforeLast("```")
            }
            cleanJson = cleanJson.trim()

            val jsonArray = org.json.JSONArray(cleanJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val img = if (obj.has("imageUrl") && !obj.isNull("imageUrl")) obj.getString("imageUrl") else null
                val vid = if (obj.has("videoUrl") && !obj.isNull("videoUrl")) obj.getString("videoUrl") else null
                val itin = if (obj.has("itinerary") && !obj.isNull("itinerary")) obj.getString("itinerary") else null
                list.add(
                    PicnicSpot(
                        name = obj.optString("name", "Unknown Spot"),
                        location = obj.optString("location", "Unknown Location"),
                        description = obj.optString("description", "A beautiful spot to visit."),
                        region = obj.optString("region", "Punjab"),
                        category = obj.optString("category", "Lake"),
                        isHiddenGem = obj.optBoolean("isHiddenGem", true),
                        rating = obj.optDouble("rating", 4.5).toFloat(),
                        bestTimeToVisit = obj.optString("bestTimeToVisit", "All year round"),
                        travelTips = obj.optString("travelTips", "Carry essentials and do not litter."),
                        isCustomUserSpot = false,
                        imageUrl = img,
                        videoUrl = vid,
                        itinerary = itin
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}

class PicnicViewModelFactory(private val repository: PicnicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PicnicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PicnicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
