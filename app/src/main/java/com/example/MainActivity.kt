package com.example

import android.os.Bundle
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import coil.compose.AsyncImage
import com.example.data.AppDatabase
import com.example.data.PicnicSpot
import com.example.data.PicnicRepository
import com.example.ui.PicnicViewModel
import com.example.ui.PicnicViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember { PicnicRepository(database.picnicDao()) }
                val viewModel: PicnicViewModel = viewModel(
                    factory = PicnicViewModelFactory(repository)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PicnicExplorerApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun PicnicExplorerApp(viewModel: PicnicViewModel) {
    var activeTab by remember { mutableStateOf("Explore") } // Explore, Research, Discover, Earn, Stats
    val spots by viewModel.filteredSpots.collectAsStateWithLifecycle()
    val allSpotsList by viewModel.allSpots.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegion.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val selectedSpotDetail by viewModel.selectedSpotDetail.collectAsStateWithLifecycle()
    var isExportDialogOpen by remember { mutableStateOf(false) }
    val isAdminModeActive by viewModel.isAdminModeActive.collectAsStateWithLifecycle()
    var isAdminDialogOpen by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "Explore",
                    onClick = { activeTab = "Explore" },
                    icon = { Icon(Icons.Default.Map, "Explore Pakistan") },
                    label = { Text("Explore PK", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_explore")
                )
                NavigationBarItem(
                    selected = activeTab == "Research",
                    onClick = { activeTab = "Research" },
                    icon = { Icon(Icons.Default.AutoAwesome, "Gemini Research") },
                    label = { Text("AI Guide", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_research")
                )
                NavigationBarItem(
                    selected = activeTab == "Discover",
                    onClick = { activeTab = "Discover" },
                    icon = { Icon(Icons.Default.AddLocationAlt, "Add Custom Spot") },
                    label = { Text("Discover", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_discover")
                )
                NavigationBarItem(
                    selected = activeTab == "Stats",
                    onClick = { activeTab = "Stats" },
                    icon = { Icon(Icons.Default.QueryStats, "My Statistics") },
                    label = { Text("Passport", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_stats")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF141F19), Color(0xFF101211))
                        } else {
                            listOf(Color(0xFFF6F8F5), Color(0xFFFCFDFB))
                        }
                    )
                )
        ) {
            // Main Top Branding Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "سیر و تفریح",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "Picnic PK",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Hidden Travel & Picnic Spots Explorer",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isAdminDialogOpen = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAdminModeActive) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                            .testTag("admin_access_button")
                    ) {
                        Icon(
                            imageVector = if (isAdminModeActive) Icons.Default.Security else Icons.Default.Lock,
                            contentDescription = "Admin Area",
                            tint = if (isAdminModeActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExportDialogOpen = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .testTag("export_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export backup",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "BrandingTabTransitions",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    "Explore" -> ExploreSpotsScreen(
                        viewModel = viewModel,
                        spots = spots,
                        searchQuery = searchQuery,
                        selectedRegion = selectedRegion,
                        selectedCategory = selectedCategory,
                        isDark = isDark,
                        onNavigateToResearch = { query ->
                            activeTab = "Research"
                            viewModel.onResearchQueryChange(query)
                            viewModel.setResearchModeCatalog(true)
                            viewModel.searchOnlineCatalog()
                        }
                    )
                    "Research" -> GeminiResearchScreen(viewModel, isDark)
                    "Discover" -> DiscoverCustomSpotScreen(viewModel, isDark)
                    "Stats" -> StatsPassportScreen(viewModel, allSpotsList, isDark)
                }
            }
        }
    }

    // Secondary Detail & Export overlays
    selectedSpotDetail?.let { spot ->
        PicnicDetailDialog(
            spot = spot,
            onClose = { viewModel.selectSpot(null) },
            onBookmarkToggle = { viewModel.toggleBookmark(spot) },
            onVisitedToggle = { viewModel.toggleVisited(spot) },
            onDeleteCustom = { viewModel.deleteSpot(spot.id) },
            isDark = isDark
        )
    }

    if (isExportDialogOpen) {
        PicnicExportDialog(
            spots = allSpotsList,
            onClose = { isExportDialogOpen = false }
        )
    }

    if (isAdminDialogOpen) {
        PicnicAdminDialog(
            viewModel = viewModel,
            onClose = { isAdminDialogOpen = false },
            isDark = isDark
        )
    }
}

@Composable
fun ExploreSpotsScreen(
    viewModel: PicnicViewModel,
    spots: List<PicnicSpot>,
    searchQuery: String,
    selectedRegion: String,
    selectedCategory: String,
    isDark: Boolean,
    onNavigateToResearch: (String) -> Unit
) {
    val regions = listOf("All", "Punjab", "Sindh", "KPK", "Balochistan", "Kashmir", "Gilgit-Baltistan", "Global")
    val categories = listOf("All", "Lake", "Meadow", "Waterfall", "Beach", "Forest", "Mountain/Valley", "Historic", "Sports & Gaming")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_bar"),
            placeholder = { Text("Search Soon Valley, Astola, waterfalls...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
        )

        // Regions filtering horizontal ribbon
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Regions & Provinces 📍",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(regions) { region ->
                    val isActive = selectedRegion == region
                    val chipBg = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    val labelColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { viewModel.onRegionFilterChange(region) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = region,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                    }
                }
            }
        }

        // Categories filtering horizontal ribbon
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Spot Type 🏕️",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isActive = selectedCategory == category
                    val chipBg = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    val labelColor = if (isActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { viewModel.onCategoryFilterChange(category) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val icon = when (category) {
                                "Lake" -> "💧"
                                "Meadow" -> "🌿"
                                "Waterfall" -> "🌊"
                                "Beach" -> "🏖️"
                                "Forest" -> "🌲"
                                "Mountain/Valley" -> "🏔️"
                                "Historic" -> "🏛️"
                                "Sports & Gaming" -> "🎮"
                                else -> "🌎"
                            }
                            Text(text = icon, fontSize = 11.sp)
                            Text(text = category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor)
                        }
                    }
                }
            }
        }

        // Listing of Catalog Grid
        if (spots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExploreOff,
                        contentDescription = "No spots",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(52.dp)
                    )
                    Text(
                        text = "No Hidden Picnic Gems Match",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try refining your filters or search the entire web and internet using our integrated premium Gemini AI Search Engine!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    
                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { onNavigateToResearch(searchQuery) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Try AI Search: \"$searchQuery\" 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("spots_list")
            ) {
                // Immersive Front Face Landscape Slide Carousel (مشہور ترین تفریحی مقامات)
                item {
                    var activeSlide by remember { mutableStateOf(0) }
                    val slideImages = listOf(
                        "https://images.unsplash.com/photo-1627581512404-58aa44f77c44?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?auto=format&fit=crop&w=800&q=80"
                    )
                    val slideTitlesEn = listOf(
                        "Saif-ul-Muluk Fairy Lake",
                        "Astola Marine Coral Shores",
                        "Lauterbrunnen Glacial Falls"
                    )
                    val slideTitlesUr = listOf(
                        "جھیل سیف الملوک (پریوں کا دیس - ناران)",
                        "جزیرہ آسٹولا (نیلا شفاف سمندر - پسنی)",
                        "الپائن آبشاروں کی وادی (سوئٹزرلینڈ)"
                    )

                    LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(4000)
                            activeSlide = (activeSlide + 1) % slideImages.size
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp)
                            .padding(bottom = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = slideImages[activeSlide],
                                contentDescription = "scenic gateway",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Immersive Dark Vignette Overlay for Premium Typography
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.15f),
                                                Color.Black.copy(alpha = 0.82f)
                                            )
                                        )
                                    )
                            )

                            // Overlay Metadata and Play Indicators
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(horizontal = 9.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "RECOMMENDED SPOT 👑",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        slideImages.forEachIndexed { idx, _ ->
                                            Box(
                                                modifier = Modifier
                                                    .size(if (idx == activeSlide) 14.dp else 6.dp, 6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (idx == activeSlide) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f))
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = slideTitlesEn[activeSlide] + " 🌍",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = slideTitlesUr[activeSlide],
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✓ 4K High-Fidelity Drone Flights Ready",
                                            fontSize = 9.sp,
                                            color = Color.Green,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Red)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "FLIGHT TOUR 🎥",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Pakistan Highlights horizontal scrolling ribbon
                item {
                    val highlights = spots.filter {
                        it.name.contains("Shalimar", ignoreCase = true) ||
                        it.name.contains("Saif-ul-Muluk", ignoreCase = true) ||
                        it.name.contains("Jinnah", ignoreCase = true) ||
                        it.name.contains("Neelum", ignoreCase = true) ||
                        it.name.contains("Hunza", ignoreCase = true) ||
                        it.name.contains("Kunhar", ignoreCase = true) ||
                        it.name.contains("Sajikot", ignoreCase = true)
                    }
                    if (highlights.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Famous Pakistani Highlights 🌟 (مشہور وادیاں اور باغات)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Must Visit",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(highlights) { spot ->
                                    Card(
                                        modifier = Modifier
                                            .width(260.dp)
                                            .clickable { viewModel.selectSpot(spot) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    ) {
                                        val imgUrl = spot.imageUrl ?: "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=400&q=80"
                                        AsyncImage(
                                            model = imgUrl,
                                            contentDescription = spot.name,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                        )

                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = spot.category,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, null, tint = Color(0xFFF1C40F), modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(text = "${spot.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(
                                                text = spot.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(10.dp))
                                                Text(
                                                    text = spot.location,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = spot.description,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 14.sp
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Best time: " + spot.bestTimeToVisit,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                                Text(
                                                    text = "Explore ➔",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Exploring ${spots.size} Hidden Picnic Corners 🗺️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(spots) { spot ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSpot(spot) }
                            .testTag("spot_card_${spot.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    ) {
                        val imgUrl = spot.imageUrl ?: "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=600&q=80"
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = spot.name,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        )

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Row: Category + Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val icon = when (spot.category) {
                                        "Lake" -> "💧"
                                        "Meadow" -> "🌿"
                                        "Waterfall" -> "🌊"
                                        "Beach" -> "🏖️"
                                        "Forest" -> "🌲"
                                        "Mountain/Valley" -> "🏔️"
                                        "Historic" -> "🏛️"
                                        "Sports & Gaming" -> "🎮"
                                        else -> "⛳"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$icon ${spot.category}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (spot.isHiddenGem) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE5A93C).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "✨ Hidden Gem",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC78F1E)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Color(0xFFF1C40F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${spot.rating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Title & Location
                            Column {
                                Text(
                                    text = spot.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = spot.location,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Text(
                                text = spot.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )

                            // Highlights
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (spot.isBookmarked) {
                                        Icon(
                                            imageVector = Icons.Default.BookmarkAdded,
                                            contentDescription = "Bookmarked",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (spot.isVisited) {
                                        Icon(
                                            imageVector = Icons.Default.Beenhere,
                                            contentDescription = "Visited",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Best time: " + spot.bestTimeToVisit,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiResearchScreen(viewModel: PicnicViewModel, isDark: Boolean) {
    val researchQuery by viewModel.researchQuery.collectAsStateWithLifecycle()
    val researchResult by viewModel.researchResult.collectAsStateWithLifecycle()
    val isResearching by viewModel.isResearching.collectAsStateWithLifecycle()

    val isResearchModeCatalog by viewModel.isResearchModeCatalog.collectAsStateWithLifecycle()
    val isSearchingCatalog by viewModel.isSearchingCatalog.collectAsStateWithLifecycle()
    val onlineResearchSpots by viewModel.onlineResearchSpots.collectAsStateWithLifecycle()
    val allSpotsList by viewModel.allSpots.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val openYouTubeTour = remember(context) {
        { spotName: String ->
            try {
                val query = "$spotName tourism drone vlog tour"
                val uri = android.net.Uri.parse("https://www.youtube.com/results?search_query=" + android.net.Uri.encode(query))
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.youtube")
                }
                try {
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    val webIntent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(webIntent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not launch search intent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val suggestions = listOf(
        "famous rivers and cold water stream picnic points in KPK or Punjab",
        "beautiful historical gardens across Lahore and Rawalpindi",
        "lush green hidden valleys in Pakistan for family summer vacation",
        "secluded lakes and waterfalls in Azad Kashmir with camping spaces"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini Guide",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interactive Gemini Online Search Engine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Explore thousands of Pakistani or global scenic spots. Search anything (rivers, valleys, gardens) and save them offline in one tap!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Mode Toggles (Catalog Lists vs. General QA)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isResearchModeCatalog) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { viewModel.setResearchModeCatalog(true) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Default.Explore, null,
                        tint = if (isResearchModeCatalog) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Online Spot Finder 🏝️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isResearchModeCatalog) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isResearchModeCatalog) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { viewModel.setResearchModeCatalog(false) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Default.Forum, null,
                        tint = if (!isResearchModeCatalog) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AI Dialog Guide 💬",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isResearchModeCatalog) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Scrollable content wrapper for results
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Search Input Block
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = researchQuery,
                        onValueChange = { viewModel.onResearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("research_question_input"),
                        placeholder = {
                            Text(
                                text = if (isResearchModeCatalog) {
                                    "مثلاً: 'دریا' (rivers), 'آبشار' (waterfalls), 'Katora Lake'..."
                                } else {
                                    "Describe travel questions (e.g. Babusar top weather in April)"
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (researchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onResearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, "Clear query", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = {
                                if (researchQuery.isNotBlank() && !isResearching && !isSearchingCatalog) {
                                    if (isResearchModeCatalog) {
                                        viewModel.searchOnlineCatalog()
                                    } else {
                                        viewModel.runGeminiResearch()
                                    }
                                }
                            }
                        ),
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    // Smart Badge indicating system strength
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ انٹرنیٹ پر موجود ہر جگہ کی لائیو تلاش",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E5A44))
                            )
                            Text(
                                text = "YouTube & Web synced",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (isResearchModeCatalog) {
                                viewModel.searchOnlineCatalog()
                            } else {
                                viewModel.runGeminiResearch()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_research_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = researchQuery.isNotBlank() && !isResearching && !isSearchingCatalog
                    ) {
                        if (isResearching || isSearchingCatalog) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSearchingCatalog) "Searching spots online..." else "Analyzing facts...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(Icons.Default.AutoAwesome, "Research")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isResearchModeCatalog) "AI Search Live Internet (تفصیلی تلاش کریں) 🚀" else "Ask AI Travel Assistant (رہنمائی حاصل کریں) 💬",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Suggestions Item
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Search Ideas 💡",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    suggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                .clickable {
                                    viewModel.onResearchQueryChange(suggestion)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "🔎 $suggestion",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Loading / Catalog Results item
            if (isResearchModeCatalog) {
                if (isSearchingCatalog) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "سکاؤٹنگ وادیاں، دریا اور باغات... مہربانی فرما کر انتظار کریں",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Searching thousands of scenic Pakistani entries online using Gemini Artificial Intelligence...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (onlineResearchSpots.isNotEmpty()) {
                    item {
                        Text(
                            text = "Found ${onlineResearchSpots.size} Online Matching Locations 🌍",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    items(onlineResearchSpots) { spot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSpot(spot) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = spot.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    // Rating
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFF1C40F), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(text = "${spot.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    text = spot.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                                    Text(
                                        text = spot.location,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Text(
                                    text = spot.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 15.sp
                                )

                                Spacer(
                                    modifier = Modifier
                                        .height(1.dp)
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Season: ${spot.bestTimeToVisit}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Dynamic Database saved state lookup
                                    val isAlreadySaved = allSpotsList.any { dbSpot ->
                                        dbSpot.name.equals(spot.name, ignoreCase = true)
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // YouTube Live Vlog/Tour Button
                                        Button(
                                            onClick = { openYouTubeTour(spot.name) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFC62828),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("YouTube 🎥", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (isAlreadySaved) {
                                            Button(
                                                onClick = {},
                                                enabled = false,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    disabledContentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Saved Offline", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.saveOnlineSpotToDb(spot) },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.GetApp, null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Save offline", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // QA dialogue mode
                if (researchResult != null || isResearching) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("research_result_card")
                                .padding(top = 10.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AI Guide Insights",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                if (isResearching) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(text = "Evaluating traveling landscape guide...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                } else {
                                    Text(
                                        text = researchResult ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.testTag("research_result_text")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverCustomSpotScreen(viewModel: PicnicViewModel, isDark: Boolean) {
    val inputName by viewModel.inputName.collectAsStateWithLifecycle()
    val inputLocation by viewModel.inputLocation.collectAsStateWithLifecycle()
    val inputDescription by viewModel.inputDescription.collectAsStateWithLifecycle()
    val inputRegion by viewModel.inputRegion.collectAsStateWithLifecycle()
    val inputCategory by viewModel.inputCategory.collectAsStateWithLifecycle()
    val inputIsHidden by viewModel.inputIsHidden.collectAsStateWithLifecycle()
    val inputTips by viewModel.inputTips.collectAsStateWithLifecycle()
    val inputBestTime by viewModel.inputBestTime.collectAsStateWithLifecycle()
    val inputRating by viewModel.inputRating.collectAsStateWithLifecycle()

    val regionsList = listOf("Punjab", "Sindh", "KPK", "Balochistan", "Kashmir", "Gilgit-Baltistan", "Global")
    val categoriesList = listOf("Lake", "Meadow", "Waterfall", "Beach", "Forest", "Mountain/Valley", "Historic", "Sports & Gaming")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "Add Custom Secret Spot 🗺️",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Discovered an unmapped scenic vista or waterfall? Save it permanently in your regional database passport.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                lineHeight = 14.sp
            )
        }

        // Form fields
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Spot Name / Title *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = inputName,
                onValueChange = { viewModel.onNameChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_spot_name_input"),
                placeholder = { Text("e.g. Uchali Secret Meadow Point", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Specific Location / Distict *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = inputLocation,
                onValueChange = { viewModel.onLocationChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_spot_location_input"),
                placeholder = { Text("e.g. Soon Valley, Khushab District", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Region", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                // Small customized horizontal ribbon selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)), RoundedCornerShape(12.dp))
                        .clickable {
                            val nextIdx = (regionsList.indexOf(inputRegion) + 1) % regionsList.size
                            viewModel.onRegionChange(regionsList[nextIdx])
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "📍 $inputRegion", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Spot Type category", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)), RoundedCornerShape(12.dp))
                        .clickable {
                            val nextIdx = (categoriesList.indexOf(inputCategory) + 1) % categoriesList.size
                            viewModel.onCategoryChange(categoriesList[nextIdx])
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "🏕️ $inputCategory", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Description / Scenic Vibe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = inputDescription,
                onValueChange = { viewModel.onDescriptionChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .testTag("custom_spot_desc_input"),
                placeholder = { Text("Describe the surroundings, sound of water, availability of shade...", fontSize = 12.sp) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Keep it Classified? 🤫", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "Tag as Secret Hidden Gem in filters", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Switch(
                checked = inputIsHidden,
                onCheckedChange = { viewModel.onIsHiddenToggle() },
                modifier = Modifier.testTag("custom_spot_hidden_switch")
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Pro Packing/Trekking Tips", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = inputTips,
                onValueChange = { viewModel.onTipsChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_spot_tips_input"),
                placeholder = { Text("Waterproofing, garbage sacks, local contacts, route rules...", fontSize = 12.sp) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Best time/season", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = inputBestTime,
                    onValueChange = { viewModel.onBestTimeChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Sept to Nov", fontSize = 12.sp) }
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "My Personal Rating / 5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.onRatingChange((inputRating - 0.5f).coerceAtLeast(1.0f)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, "Dec rating", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(text = String.format(Locale.getDefault(), "%.1f ★", inputRating), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { viewModel.onRatingChange((inputRating + 0.5f).coerceAtMost(5.0f)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, "Inc rating", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.addCustomUserSpot()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("commit_custom_spot_button"),
            shape = RoundedCornerShape(12.dp),
            enabled = inputName.isNotBlank() && inputLocation.isNotBlank()
        ) {
            Icon(Icons.Default.AddLocation, "Add")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Log Custom Picnic Discovery", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(26.dp))
    }
}

@Composable
fun StatsPassportScreen(viewModel: PicnicViewModel, allSpots: List<PicnicSpot>, isDark: Boolean) {
    val totalSpots = allSpots.size
    val bookmarked = allSpots.count { it.isBookmarked }
    val visited = allSpots.count { it.isVisited }
    val averageRating = if (allSpots.isNotEmpty()) allSpots.map { it.rating }.average() else 4.5
    val customDiscoveries = allSpots.count { it.isCustomUserSpot }

    // Region distribution
    val regionsList = listOf("Punjab", "Sindh", "KPK", "Balochistan", "Kashmir", "Gilgit-Baltistan")
    val countByRegion = allSpots.groupBy { it.region }.mapValues { it.value.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "My Travel Passport & Metadata 🏔️",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track your exploration metrics across Pakistan and monitor database metrics.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }

        // Stats card blocks
        Card(
            modifier = Modifier.fillMaxWidth().testTag("stats_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Footprint Analytics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.QueryStats, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Total Spots", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(text = "$totalSpots", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "My Visited", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(text = "$visited", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Bookmarks", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(text = "$bookmarked", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Secret Gems", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(text = "${allSpots.count { it.isHiddenGem }}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Self Discovered", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(text = "$customDiscoveries", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Mean Rating", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(text = String.format(Locale.getDefault(), "%.1f ★", averageRating), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Horizontal Progress bars showing explore metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Provincial Distribution Progress",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                regionsList.forEach { region ->
                    val count = countByRegion[region] ?: 0
                    val fraction = if (totalSpots > 0) count.toFloat() / totalSpots else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = region,
                            fontSize = 11.sp,
                            modifier = Modifier.width(92.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        LinearProgressIndicator(
                            progress = fraction,
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                        )
                        Text(
                            text = "$count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Fun aesthetic custom canvas drawing - Picnic trail mountain diagram
        Text(
            text = "My Exploration Altitude Contour ⛰️",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height

                    // Drawing beautiful green forest hill profiles
                    val path1 = Path().apply {
                        moveTo(0f, height)
                        quadraticTo(width * 0.25f, height * 0.4f, width * 0.5f, height * 0.7f)
                        quadraticTo(width * 0.75f, height * 0.9f, width, height * 0.5f)
                        lineTo(width, height)
                        close()
                    }
                    val path2 = Path().apply {
                        moveTo(0f, height)
                        quadraticTo(width * 0.15f, height * 0.8f, width * 0.35f, height * 0.5f)
                        quadraticTo(width * 0.6f, height * 0.2f, width, height * 0.8f)
                        lineTo(width, height)
                        close()
                    }

                    // Draw back hill
                    drawPath(
                        path = path1,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF8BAE90).copy(alpha = 0.3f), Color(0xFF5E8B65).copy(alpha = 0.1f))
                        )
                    )

                    // Draw front hill
                    drawPath(
                        path = path2,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF2E5A44).copy(alpha = 0.4f), Color(0xFF1B3B2B).copy(alpha = 0.1f))
                        )
                    )

                    // Draw connecting travel line representing hiking metrics
                    val linePath = Path().apply {
                        moveTo(0f, height * 0.85f)
                        lineTo(width * 0.25f, height * 0.65f)
                        lineTo(width * 0.5f, height * 0.45f)
                        lineTo(width * 0.75f, height * 0.55f)
                        lineTo(width, height * 0.2f)
                    }
                    drawPath(
                        path = linePath,
                        color = Color(0xFFC28B5B),
                        style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )

                    // Draw visited camp node marker
                    drawCircle(
                        color = Color(0xFFC28B5B),
                        radius = 6.dp.toPx(),
                        center = Offset(width * 0.5f, height * 0.45f)
                    )
                }

                Text(
                    text = "Trail Elevation of $visited spots reached",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Dialog for viewing spot details
@Composable
fun PicnicDetailDialog(
    spot: PicnicSpot,
    onClose: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onVisitedToggle: () -> Unit,
    onDeleteCustom: () -> Unit,
    isDark: Boolean
) {
    Dialog(onDismissRequest = onClose) {
        val context = LocalContext.current
        val openYouTubeTour = remember(context, spot.name) {
            {
                try {
                    val query = "${spot.name} tourism drone vlog tour"
                    val uri = android.net.Uri.parse("https://www.youtube.com/results?search_query=" + android.net.Uri.encode(query))
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.youtube")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (ex: Exception) {
                        val webIntent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(webIntent)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not launch search intent", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("detail_dialog_card"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            var isPlayingVideo by remember { mutableStateOf(false) }
            var videoProgress by remember { mutableFloatStateOf(0.0f) }

            LaunchedEffect(isPlayingVideo) {
                if (isPlayingVideo) {
                    while (true) {
                        kotlinx.coroutines.delay(100)
                        videoProgress += 0.02f
                        if (videoProgress >= 1.0f) {
                            videoProgress = 0.0f
                        }
                    }
                }
            }

            // Stateful cinematic video/image layout header
            if (isPlayingVideo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                        .background(Color.Black)
                ) {
                    val zoomScale = 1.0f + (videoProgress * 0.12f)
                    val imgUrl = spot.imageUrl ?: "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=600&q=80"
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = "drone view video play",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.85f)
                    )

                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔴 VIRTUAL DRONE FLIGHT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                text = spot.videoUrl ?: "Scenic Flight Guide",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "virtual navigation",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp).align(Alignment.Center)
                        )

                        Column(
                            modifier = Modifier.align(Alignment.BottomStart),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "ALTITUDE: ${(120 + (videoProgress * 60).toInt())}m | COMPASS: 180° S",
                                fontSize = 8.sp,
                                color = Color.Green,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Text(
                                text = "LIVE TELEMETRY: 4K HIGH FIDELITY STREAM",
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { isPlayingVideo = false },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "pause navigation flight simulation",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(videoProgress)
                            .height(3.dp)
                            .background(Color.Red)
                            .align(Alignment.BottomStart)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val imgUrl = spot.imageUrl ?: "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=600&q=80"
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = spot.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isPlayingVideo = true },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.Explore, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Drone Flight 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { openYouTubeTour() },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828).copy(alpha = 0.95f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watch YouTube Tour 🎥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = spot.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row {
                        IconButton(onClick = onBookmarkToggle) {
                            Icon(
                                imageVector = if (spot.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, "Close details")
                        }
                    }
                }

                // Name & Location
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = spot.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "District",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${spot.location} | ${spot.region}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                // Description
                Text(
                    text = "Scenic description",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = spot.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                // Best time & rating bar details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "📅 Best season:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(text = spot.bestTimeToVisit, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "🔥 Well-being score:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(text = "${spot.rating} / 5.0 ★", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC78F1E))
                        }
                    }
                }

                // 3-Step Elegant Itinerary Section
                val itineraryText = spot.itinerary
                if (!itineraryText.isNullOrBlank()) {
                    Text(
                        text = "Prepared Day Itinerary 🗺️ (منصوبہ بندی)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itineraryText.split("\n").forEach { step ->
                                if (step.isNotBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                        Text(
                                            text = step.trim(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Packing / Trail Guidlines
                Text(
                    text = "Pro Explorer Packing & Tips 🎒",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = spot.travelTips,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 16.sp,
                    fontStyle = FontStyle.Italic
                )

                // Eco-friendly action items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2E5A44).copy(alpha = 0.08f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolunteerActivism, null, tint = Color(0xFF2E5A44), modifier = Modifier.size(16.dp))
                    Text(
                        text = "Leave No Trace: Please pack out any plastic waste you generate to keep this hidden point green.",
                        fontSize = 10.sp,
                        color = Color(0xFF2E5A44),
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onVisitedToggle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (spot.isVisited) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary,
                            contentColor = if (spot.isVisited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (spot.isVisited) Icons.Default.Beenhere else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (spot.isVisited) "Visited! ✅" else "Mark Visited", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (spot.isCustomUserSpot) {
                        OutlinedButton(
                            onClick = {
                                onDeleteCustom()
                                onClose()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Data converter utils for XML / CSV Backup Exports
fun picnicSpotsToJson(spots: List<PicnicSpot>): String {
    val sb = StringBuilder()
    sb.append("[\n")
    spots.forEachIndexed { i, s ->
        sb.append("  {\n")
        sb.append("    \"id\": ${s.id},\n")
        sb.append("    \"name\": \"${s.name.replace("\"", "\\\"")}\",\n")
        sb.append("    \"location\": \"${s.location.replace("\"", "\\\"")}\",\n")
        sb.append("    \"region\": \"${s.region}\",\n")
        sb.append("    \"category\": \"${s.category}\",\n")
        sb.append("    \"isHiddenGem\": ${s.isHiddenGem},\n")
        sb.append("    \"isBookmarked\": ${s.isBookmarked},\n")
        sb.append("    \"isVisited\": ${s.isVisited},\n")
        sb.append("    \"rating\": ${s.rating},\n")
        sb.append("    \"bestTimeToVisit\": \"${s.bestTimeToVisit}\"\n")
        sb.append("  }${if (i < spots.size - 1) "," else ""}\n")
    }
    sb.append("]")
    return sb.toString()
}

fun picnicSpotsToCsv(spots: List<PicnicSpot>): String {
    val sb = StringBuilder()
    sb.append("ID,Name,Location,Region,Category,IsHidden,IsBookmarked,IsVisited,Rating,BestSeason\n")
    spots.forEach { s ->
        val cleanName = s.name.replace("\"", "\"\"")
        val cleanLoc = s.location.replace("\"", "\"\"")
        sb.append("${s.id},\"$cleanName\",\"$cleanLoc\",\"${s.region}\",\"${s.category}\",${s.isHiddenGem},${s.isBookmarked},${s.isVisited},${s.rating},\"${s.bestTimeToVisit}\"\n")
    }
    return sb.toString()
}

// Dialog for Export & Share backups
@Composable
fun PicnicExportDialog(spots: List<PicnicSpot>, onClose: () -> Unit) {
    val context = LocalContext.current
    var formatType by remember { mutableStateOf("JSON") } // JSON or CSV

    val dataContent = remember(spots, formatType) {
        if (formatType == "JSON") picnicSpotsToJson(spots) else picnicSpotsToCsv(spots)
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("export_dialog_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Export Picnic Footprints 🚗", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Text(
                    text = "Backup your discovered places list and travel ticks to share with fellow explorers.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 14.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(3.dp)
                ) {
                    listOf("JSON", "CSV").forEach { format ->
                        val active = formatType == format
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { formatType = format }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = format,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Scroll Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(text = dataContent, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Picnic Backup", dataContent)
                            manager.setPrimaryClip(clip)
                            Toast.makeText(context, "$formatType backup copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "My Picnic PK Backup")
                                putExtra(Intent.EXTRA_TEXT, dataContent)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Picnic backup via"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PicnicAdminDialog(
    viewModel: PicnicViewModel,
    onClose: () -> Unit,
    isDark: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val isAdminModeActive by viewModel.isAdminModeActive.collectAsStateWithLifecycle()
    val allSpotsList by viewModel.allSpots.collectAsStateWithLifecycle()

    var passcodeEntry by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF131A16) else Color(0xFFF3F8F5)
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top header of dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdminModeActive) Icons.Default.Security else Icons.Default.Lock,
                            contentDescription = "Admin Area",
                            tint = if (isAdminModeActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isAdminModeActive) "Admin Control Panel ⚙️" else "Admin Lock (ایڈمن لاگ ان)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close admin panel",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                if (!isAdminModeActive) {
                    // Password Login Flow
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "To access developer settings and download logs, enter the admin passcode.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        // Bilingual hint
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Yellow.copy(alpha = 0.15f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 Urdu: ایڈمن لاگ ان کرنے کے لیے خفیہ پن کوڈ درج کریں۔\n👉 Default Passcode is '2026' or '1122'",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedTextField(
                            value = passcodeEntry,
                            onValueChange = {
                                passcodeEntry = it
                                passcodeError = false
                            },
                            label = { Text("Enter Passcode (پن کوڈ)", fontSize = 11.sp) },
                            singleLine = true,
                            isError = passcodeError,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )

                        if (passcodeError) {
                            Text(
                                text = "❌ Incorrect Passcode! (غلط پن کوڈ)",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (passcodeEntry == "2026" || passcodeEntry == "1122" || passcodeEntry.lowercase() == "admin") {
                                    viewModel.toggleAdminMode(true)
                                    Toast.makeText(context, "Welcome Administrator! Admin mode active ✅", Toast.LENGTH_SHORT).show()
                                } else {
                                    passcodeError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unlock Admin Features", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Logged in: Administrative Controls and Guides
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Success header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32))
                                Column {
                                    Text("Status: Authorized Developer Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                    Text("You possess root sandbox configuration rights.", fontSize = 10.sp, color = Color(0xFF1B5E20).copy(alpha = 0.8f))
                                }
                            }
                        }

                        // Sandbox simulation controls
                        Text("🛠️ Sandbox DB Controller", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.resetAndReseedDatabase()
                                    Toast.makeText(context, "Initial Seeds reloaded! Sports and gaming grounds loaded successfully ✅", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Load Seeds (لوڈ کریں)", fontSize = 10.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.clearAllSpots()
                                    Toast.makeText(context, "Database cleared! All spots deleted successfully 🗑️", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Wipe Database (ڈیلیٹ)", fontSize = 10.sp)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Bilingual Complete Blueprint
                        Text("📋 Administrator Setup Blueprint (اردو اور انگریزی گائیڈ)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        // Urdu Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🇵🇰 اس ایپ کا مالک اور مستقل ایڈمن کیسے بنیں؟",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )

                                Text(
                                    text = "جناب! اگر آپ اس ایپ کو اپنی ملکیت بنانا چاہتے ہیں تاکہ آپ کا اپنا نام، اپنے بنائے تفریحی مقامات، اور آپ کی ذاتی ایڈ ایڈ یونٹس سے ڈالر براہِ راست آپ کے اکاؤنٹ میں آئیں، تو درج ذیل مراحل مکمل کریں:\n\n" +
                                           "1️⃣ **اپنا برانڈ اور پیکیج تبدیل کریں**:\n" +
                                           "آئی ٹی اسٹوڈیو کے پروجیکٹ میں `build.gradle.kts` کھولیں۔ وہاں پر `applicationId` کو تبدیل کر کے اپنی مرضی کا یونیک پیکیج نام رکھیں (مثال کے طور پر: `com.paktravels.app`)۔\n\n" +
                                           "2️⃣ **آن لائن فائر بیس ڈیٹا بیس (Firebase) کا الحاق**:\n" +
                                           "ابھی یہ ایپ آف لائن Room SQLite ڈیٹا بیس استعمال کرتی ہے۔ آن لائن ورلڈ وائڈ سنکرونائزیشن لانے کے لیے:\n" +
                                           "- گوگل فائر بیس پر پروجیکٹ بنائیں۔\n" +
                                           "- `google-services.json` فائل ڈاؤن لوڈ کر کے `app` فولڈر میں رکھ دیں۔\n" +
                                           "- فائر بیس ریئل ٹائم ڈیٹا بیس ایکٹیو کر کے یوزر ڈیلیٹ/اپروول رولز سیٹ کریں۔\n\n" +
                                           "3️⃣ **گوگل ایڈموب سے ڈالر کمائیں**:\n" +
                                           "گوگل ایڈموب اکاؤنٹ بنائیں اور اپنی ایڈسینس پبلشر آئی ڈی (`ca-pub-xxxx`) حاصل کریں۔ اپنے بینر اور انٹرسٹیشل ایڈ یونٹس بنا کر 'Earn PK' اسکرین کے پینل کے ذریعے عارضی یا Strings.xml میں مستقل لکھ دیں، تاکہ تمام کلکس اور امپریشنز پر آپ کے اکاؤنٹ میں ڈالرز کریڈٹ ہوں۔\n\n" +
                                           "4️⃣ **پلے اسٹور پبلشنگ**:\n" +
                                           "فائل مینو سے 'Generate Signed APK' بنائیں۔ گوگل پلے کنسول ($25 فیس) خریدیں اور پلے اسٹور پر اپلوڈ کر دیں۔ آپ کے ایڈمن بننے کا سفر مکمل ہو جائے گا!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // English Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🇬🇧 Technical Administrator Walkthrough",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "To deploy this application as your personal property and monetize live traffic, adopt this configuration:\n\n" +
                                           "Step 1: Application Re-branding\n" +
                                           "Locate `app/build.gradle.kts` and rename both `applicationId` and `namespace` to separate the project from default templates on the device.\n\n" +
                                           "Step 2: Server-Sync Integration\n" +
                                           "Instead of storing user spots locally in Room, integrate the Google Firebase Android SDK. Setup standard reference collections to listen and auto-approve travel spots directly on a web console.\n\n" +
                                           "Step 3: Play Console deployment\n" +
                                           "Use Android Studio or Grade tool to execute `gradle assembleRelease` to generate a production-ready Bundle file (.aab) signed with your custom key store.\n\n" +
                                           "Step 4: Live AdMob Override\n" +
                                           "Update the `AndroidManifest.xml` meta-data value for `APPLICATION_ID` with your real ID, and ensure Proguard is not removing the Play integrity symbols.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.toggleAdminMode(false)
                                Toast.makeText(context, "Logged out of admin console", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Disable Admin Mode", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

