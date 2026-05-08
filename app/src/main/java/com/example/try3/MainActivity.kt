package com.example.try3

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.try3.data.ai.AiGeneratedContent
import com.example.try3.data.ai.AiHelper
import com.example.try3.data.location.LocationHelper
import com.example.try3.data.model.Comment
import com.example.try3.data.model.Record
import com.example.try3.data.repository.RecordRepository
import com.example.try3.data.supabase.CommentApi
import com.example.try3.data.supabase.SupabaseStorage
import com.example.try3.ui.theme.*
import com.example.try3.viewmodel.RecordViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Try3Theme {
                val viewModel: RecordViewModel = viewModel(
                    factory = RecordViewModelFactory(RecordRepository())
                )
                var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
                var selectedRecord by remember { mutableStateOf<Record?>(null) }
                val storage = remember { SupabaseStorage() }
                val context = LocalContext.current
                val locationHelper = remember { LocationHelper(context) }
                val aiHelper = remember { AiHelper() }

                when (currentScreen) {
                    is Screen.List -> MainScreen(
                        viewModel = viewModel,
                        onRecordClick = { record ->
                            selectedRecord = record
                            currentScreen = Screen.Detail
                        },
                        onAddClick = { currentScreen = Screen.Add }
                    )
                    is Screen.Detail -> RecordDetailScreen(
                        record = selectedRecord!!,
                        onBack = { currentScreen = Screen.List },
                        onEdit = { record ->
                            selectedRecord = record
                            currentScreen = Screen.Edit
                        },
                        onDelete = { record ->
                            viewModel.deleteRecord(record)
                            currentScreen = Screen.List
                        }
                    )
                    is Screen.Add -> AddEditRecordScreen(
                        isEdit = false,
                        storage = storage,
                        locationHelper = locationHelper,
                        aiHelper = aiHelper,
                        onBack = { currentScreen = Screen.List },
                        onSave = { record ->
                            viewModel.addRecord(record)
                            currentScreen = Screen.List
                        }
                    )
                    is Screen.Edit -> AddEditRecordScreen(
                        isEdit = true,
                        existingRecord = selectedRecord,
                        storage = storage,
                        locationHelper = locationHelper,
                        aiHelper = aiHelper,
                        onBack = { currentScreen = Screen.List },
                        onSave = { record ->
                            viewModel.updateRecord(record)
                            viewModel.loadRecords()
                            currentScreen = Screen.List
                        }
                    )
                }
            }
        }
    }
}

sealed class Screen {
    object List : Screen()
    object Detail : Screen()
    object Add : Screen()
    object Edit : Screen()
}

class RecordViewModelFactory(private val repository: RecordRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val AvailableTags = listOf("日常", "旅行", "美食", "工作", "学习", "运动", "心情", "其他")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: RecordViewModel,
    onRecordClick: (Record) -> Unit,
    onAddClick: () -> Unit
) {
    val records by viewModel.records.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val filteredRecords = records.filter { record ->
        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            (record.title?.contains(searchQuery, ignoreCase = true) == true) ||
                (record.content.contains(searchQuery, ignoreCase = true)) ||
                (record.location?.contains(searchQuery, ignoreCase = true) == true) ||
                (record.tag?.contains(searchQuery, ignoreCase = true) == true)
        }
        val matchesTag = if (selectedTag == null) {
            true
        } else {
            record.tag == selectedTag
        }
        matchesSearch && matchesTag
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                isSearchActive = isSearchActive,
                onSearchToggle = { isSearchActive = !isSearchActive },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
        },
        floatingActionButton = {
            ModernFAB(onClick = onAddClick)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF5FF),
                            Color(0xFFFFF0F7),
                            Color(0xFFFFEBF5)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTag == null,
                    onClick = { selectedTag = null },
                    label = { Text("全部", fontSize = 13.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DreamPurple,
                        selectedLabelColor = Color.White
                    )
                )
                AvailableTags.forEach { tag ->
                    val tagColor = TagColors[AvailableTags.indexOf(tag) % TagColors.size]
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag, fontSize = 13.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tagColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        )
                    )
                }
            }

            if (filteredRecords.isEmpty()) {
                ModernEmptyState(isSearching = searchQuery.isNotBlank() || selectedTag != null)
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    items(filteredRecords, key = { it.id ?: it.hashCode() }) { record ->
                        ModernRecordCard(
                            record = record,
                            onClick = { onRecordClick(record) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    isSearchActive: Boolean,
    onSearchToggle: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Surface(
        color = Color.Transparent,
        shadowElevation = if (isSearchActive) 0.dp else 2.dp,
        modifier = Modifier.statusBarsPadding()
    ) {
        if (isSearchActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("搜索记录...", fontSize = 15.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "搜索", tint = DreamPurple)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "清除", tint = DreamPurple)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DreamPurple,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                TextButton(onClick = onSearchToggle) {
                    Text("取消", color = DreamPurple)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp, 20.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "tilo",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = DreamPurple
                    )
                    Text(
                        text = "记录美好时刻",
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }
                IconButton(
                    onClick = onSearchToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DreamPurple, SakuraPink)
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernFAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(DreamPurple, SakuraPink)
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "添加记录",
            modifier = Modifier.size(32.dp),
            tint = Color.White
        )
    }
}

@Composable
fun ModernEmptyState(isSearching: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DreamPurpleLight, SakuraPinkLight)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSearching) "🔍" else "✨",
                    fontSize = 48.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isSearching) "没有找到相关记录" else "还没有记录哦",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF444444)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isSearching) "试试其他关键词或标签" else "点击右下角按钮添加第一条记录",
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
fun ModernRecordCard(record: Record, onClick: () -> Unit) {
    val recordId = record.id ?: record.hashCode()
    val tagColor = if (record.tag != null) {
        val tagIndex = AvailableTags.indexOf(record.tag)
        if (tagIndex >= 0) TagColors[tagIndex % TagColors.size] else DreamPurple
    } else {
        TagColors[recordId % TagColors.size]
    }
    val cardBg = CardColors[recordId % CardColors.size]
    val imageUris = record.getImageUriList()
    val hasTitle = !record.title.isNullOrBlank()

    val imageHeight = when {
        hasTitle && imageUris.isNotEmpty() -> 220.dp
        imageUris.isNotEmpty() -> 280.dp
        hasTitle -> 200.dp
        else -> 240.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (imageUris.isNotEmpty()) {
                    AsyncImage(
                        model = imageUris.first(),
                        contentDescription = "记录图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (imageUris.size > 1) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp, 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📷 ${imageUris.size}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        cardBg,
                                        cardBg.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasTitle) {
                            Text(
                                text = record.title?.take(1) ?: "✨",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = record.content.take(1),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (record.location != null) {
                    ModernSmallChip(
                        text = "📍 ${record.location}",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }

                if (record.tag != null) {
                    ModernTagChip(
                        text = "#${record.tag}",
                        color = tagColor,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (hasTitle) {
                    Text(
                        text = record.title ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(record.getDate()),
                    fontSize = 11.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
fun ModernSmallChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 1.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp, 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF444444)
        )
    }
}

@Composable
fun ModernTagChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 1.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp, 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordDetailScreen(
    record: Record,
    onBack: () -> Unit,
    onEdit: (Record) -> Unit,
    onDelete: (Record) -> Unit
) {
    val imageUris = record.getImageUriList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录详情", color = Color(0xFF333333)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = DreamPurple)
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(record) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = DreamPurple)
                    }
                    IconButton(onClick = { onDelete(record) }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = SakuraPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF5FF), Color(0xFFFFF0F7))
                    )
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (imageUris.isNotEmpty()) {
                Column {
                    imageUris.forEachIndexed { index, uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "图片 ${index + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                        if (index < imageUris.size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!record.title.isNullOrBlank()) {
                    Text(
                        text = record.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        lineHeight = 30.sp
                    )
                }

                Text(
                    text = record.content,
                    fontSize = 16.sp,
                    color = Color(0xFF444444),
                    lineHeight = 26.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    record.tag?.let { tag ->
                        val tagColor = TagColors[AvailableTags.indexOf(tag) % TagColors.size]
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tagColor
                        ) {
                            Text(
                                text = "#$tag",
                                modifier = Modifier.padding(12.dp, 6.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    record.location?.let { location ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = "📍 $location",
                                modifier = Modifier.padding(12.dp, 6.dp),
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Text(
                    text = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()).format(record.getDate()),
                    fontSize = 13.sp,
                    color = Color(0xFF999999)
                )

                HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                record.id?.let { CommentSection(recordId = it.toLong()) }
            }
        }
    }
}

@Composable
fun CommentSection(recordId: Long) {
    val commentApi = remember { CommentApi() }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(recordId) {
        isLoading = true
        comments = commentApi.getComments(recordId)
        isLoading = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "评论 (${comments.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DreamPurple, modifier = Modifier.size(24.dp))
            }
        } else if (comments.isEmpty()) {
            Text(
                text = "暂无评论，来添加第一条吧~",
                fontSize = 14.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            comments.forEach { comment ->
                CommentItem(comment = comment)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newComment,
                onValueChange = { newComment = it },
                placeholder = { Text("添加评论...", color = Color(0xFF999999)) },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DreamPurple,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            IconButton(
                onClick = {
                    if (newComment.isNotBlank() && !isSending) {
                        isSending = true
                        kotlinx.coroutines.MainScope().launch {
                            val added = commentApi.addComment(recordId, newComment.trim())
                            if (added != null) {
                                comments = comments + added
                                newComment = ""
                            }
                            isSending = false
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(DreamPurple, CircleShape)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = comment.author,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DreamPurple
            )
            Text(
                text = comment.createdAt?.take(16)?.replace("T", " ") ?: "",
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = comment.content,
            fontSize = 14.sp,
            color = Color(0xFF444444),
            lineHeight = 22.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditRecordScreen(
    isEdit: Boolean,
    existingRecord: Record? = null,
    storage: SupabaseStorage,
    locationHelper: LocationHelper,
    aiHelper: AiHelper,
    onBack: () -> Unit,
    onSave: (Record) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(existingRecord?.title ?: "") }
    var content by remember { mutableStateOf(existingRecord?.content ?: "") }
    var location by remember { mutableStateOf(existingRecord?.location ?: "") }
    var latitude by remember { mutableStateOf(existingRecord?.latitude) }
    var longitude by remember { mutableStateOf(existingRecord?.longitude) }
    var selectedTag by remember { mutableStateOf(existingRecord?.tag) }
    var imageUris by remember { mutableStateOf(existingRecord?.getImageUriList() ?: emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }
    var isAiGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineLocationGranted || coarseLocationGranted) {
            coroutineScope.launch {
                isGettingLocation = true
                val coords = locationHelper.getCurrentLocation()
                isGettingLocation = false
                if (coords != null) {
                    latitude = coords.first
                    longitude = coords.second
                }
            }
        } else {
            isGettingLocation = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                isUploading = true
                uris.forEach { uri ->
                    val publicUrl = storage.uploadImage(uri, context.contentResolver)
                    publicUrl?.let { url ->
                        imageUris = imageUris + url
                    }
                }
                isUploading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "编辑记录" else "添加记录",
                        color = Color(0xFF333333)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = DreamPurple)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val imageUrisStr = if (imageUris.isNotEmpty()) imageUris.joinToString(",") else null
                            val record = if (isEdit && existingRecord != null) {
                                existingRecord.copy(
                                    title = title.ifBlank { null },
                                    content = content,
                                    location = location.ifBlank { null },
                                    latitude = latitude,
                                    longitude = longitude,
                                    tag = selectedTag,
                                    imageUri = imageUrisStr
                                )
                            } else {
                                Record(
                                    title = title.ifBlank { null },
                                    content = content,
                                    location = location.ifBlank { null },
                                    latitude = latitude,
                                    longitude = longitude,
                                    tag = selectedTag,
                                    imageUri = imageUrisStr
                                )
                            }
                            onSave(record)
                        },
                        enabled = content.isNotBlank() && !isUploading && !isGettingLocation && !isAiGenerating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DreamPurple
                        )
                    ) {
                        if (isUploading || isGettingLocation || isAiGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when {
                                    isGettingLocation -> "定位中"
                                    isAiGenerating -> "生成中"
                                    else -> "上传中"
                                },
                                fontSize = 14.sp
                            )
                        } else {
                            Text("保存", fontSize = 14.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF5FF), Color(0xFFFFF0F7))
                    )
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题（可选）", color = Color(0xFF888888)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DreamPurple,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    if (isEdit) {
                        Text("内容", color = Color(0xFF888888))
                    } else {
                        Text("请说明你的关键词，包含：地点、天气/时间、物品、心情\n示例：午后 咖啡馆 阳光 咖啡香", color = Color(0xFF888888))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                minLines = 4,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DreamPurple,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            if (!isEdit) {
                Button(
                    onClick = {
                        if (content.isBlank()) return@Button
                        coroutineScope.launch {
                            isAiGenerating = true
                            val generated = aiHelper.generateRecordFromKeywords(content)
                            isAiGenerating = false
                            if (generated != null) {
                                title = generated.title
                                content = generated.content
                                if (selectedTag == null && generated.suggestedTag != null) {
                                    selectedTag = generated.suggestedTag
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = content.isNotBlank() && !isAiGenerating && !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DreamPurple
                    )
                ) {
                    if (isAiGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI生成中...", fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI生成记录", fontSize = 14.sp)
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("在哪里（可选）", color = Color(0xFF888888)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DreamPurple,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = DreamPurple)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        if (isGettingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = DreamPurple
                            )
                        } else {
                            Icon(Icons.Default.Star, contentDescription = "获取定位", tint = DreamPurple)
                        }
                    }
                }
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "选择标签",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvailableTags.forEach { tag ->
                        val tagColor = TagColors[AvailableTags.indexOf(tag) % TagColors.size]
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = {
                                selectedTag = if (selectedTag == tag) null else tag
                            },
                            label = { Text(tag, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tagColor,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "添加图片",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    if (imageUris.isNotEmpty()) {
                        Text(
                            text = "${imageUris.size} 张",
                            fontSize = 13.sp,
                            color = DreamPurple
                        )
                    }
                }

                if (imageUris.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        imageUris.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "图片 ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        imageUris = imageUris.toMutableList().also { it.removeAt(index) }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(26.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "移除",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isUploading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DreamPurple
                    )
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = DreamPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("上传中...", fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (imageUris.isEmpty()) "添加图片" else "继续添加", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
